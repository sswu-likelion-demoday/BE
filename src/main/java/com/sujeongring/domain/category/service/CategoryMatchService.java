package com.sujeongring.domain.category.service;

import com.sujeongring.domain.category.dto.request.CategoryMatchCreateRequest;
import com.sujeongring.domain.category.dto.response.CategoryMatchCreateResponse;
import com.sujeongring.domain.category.dto.response.CategoryMatchResultResponse;
import com.sujeongring.domain.category.dto.response.CategoryMatchStatusResponse;
import com.sujeongring.domain.category.entity.CategoryMatchGroup;
import com.sujeongring.domain.category.entity.CategoryMatchMember;
import com.sujeongring.domain.category.entity.CategoryMatchRequest;
import com.sujeongring.domain.category.enums.CategoryMatchState;
import com.sujeongring.domain.category.enums.CategoryMatchStatus;
import com.sujeongring.domain.category.enums.CategoryType;
import com.sujeongring.domain.category.exception.CategoryMatchErrorCode;
import com.sujeongring.domain.category.exception.CategoryMatchException;
import com.sujeongring.domain.category.repository.CategoryMatchGroupRepository;
import com.sujeongring.domain.category.repository.CategoryMatchMemberRepository;
import com.sujeongring.domain.category.repository.CategoryMatchRequestRepository;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.enums.MbtiType;
import com.sujeongring.domain.user.repository.BlockRepository;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.global.error.CommonErrorCode;
import com.sujeongring.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryMatchService {

    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final CategoryMatchRequestRepository categoryMatchRequestRepository;
    private final CategoryMatchGroupRepository categoryMatchGroupRepository;
    private final CategoryMatchMemberRepository categoryMatchMemberRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public CategoryMatchStatusResponse getStatus(Long userId) {
        List<CategoryMatchStatusResponse.CategoryStateItem> categories = new ArrayList<>();

        for (CategoryType categoryType : CategoryType.values()) {
            CategoryMatchRequest latest = categoryMatchRequestRepository
                    .findTopByUserIdAndCategoryOrderByCreatedAtDesc(userId, categoryType)
                    .orElse(null);

            if (latest == null) {
                categories.add(new CategoryMatchStatusResponse.CategoryStateItem(
                        categoryType,
                        CategoryMatchState.NONE,
                        null,
                        false
                ));
                continue;
            }

            if (latest.getStatus() == CategoryMatchStatus.WAITING) {
                categories.add(new CategoryMatchStatusResponse.CategoryStateItem(
                        categoryType,
                        CategoryMatchState.WAITING,
                        latest.getId(),
                        false
                ));
                continue;
            }

            if (latest.getStatus() == CategoryMatchStatus.MATCHED && latest.getResultViewedAt() == null) {
                categories.add(new CategoryMatchStatusResponse.CategoryStateItem(
                        categoryType,
                        CategoryMatchState.RESULT_PENDING,
                        latest.getId(),
                        true
                ));
                continue;
            }

            categories.add(new CategoryMatchStatusResponse.CategoryStateItem(
                    categoryType,
                    CategoryMatchState.NONE,
                    null,
                    false
            ));
        }

        return new CategoryMatchStatusResponse(categories);
    }

    @Transactional
    public CategoryMatchCreateResponse create(
            Long userId,
            CategoryMatchCreateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CommonErrorCode.NOT_FOUND));

        if (user.getContact() == null || user.getContact().isBlank()) {
            throw new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_CONTACT_REQUIRED);
        }

        if (request.conditions() != null
                && request.conditions().admissionYearMin() != null
                && request.conditions().admissionYearMax() != null
                && request.conditions().admissionYearMin() > request.conditions().admissionYearMax()) {
            throw new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_INVALID_CONDITION);
        }

        boolean hasActiveRequest = categoryMatchRequestRepository.existsActiveRequestByUserAndCategory(
                userId,
                request.category(),
                CategoryMatchStatus.WAITING,
                CategoryMatchStatus.MATCHED
        );

        if (hasActiveRequest) {
            throw new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_ALREADY_ACTIVE);
        }

        String conditionsJson = toConditionsJson(request.conditions());

        CategoryMatchRequest saved = categoryMatchRequestRepository.save(
                new CategoryMatchRequest(
                        user,
                        request.category(),
                        request.memberCount(),
                        null,
                        conditionsJson,
                        request.allowPartialMatch()
                )
        );

        tryMatch(request.category());

        CategoryMatchRequest latest = categoryMatchRequestRepository.findById(saved.getId())
                .orElseThrow(() -> new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_REQUEST_NOT_FOUND));

        return new CategoryMatchCreateResponse(
                latest.getId(),
                latest.getCategory(),
                latest.getStatus(),
                latest.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public CategoryMatchResultResponse getResult(
            Long userId,
            Long requestId
    ) {
        CategoryMatchRequest request = categoryMatchRequestRepository.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_REQUEST_NOT_FOUND));

        if (request.getStatus() != CategoryMatchStatus.MATCHED) {
            throw new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_RESULT_NOT_READY);
        }

        CategoryMatchMember member = categoryMatchMemberRepository.findByRequestId(requestId)
                .orElseThrow(() -> new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_RESULT_NOT_READY));

        Long groupId = member.getGroup().getId();

        List<CategoryMatchResultResponse.Member> members = categoryMatchMemberRepository
                .findAllByGroupIdWithUser(groupId)
                .stream()
                .filter(groupMember -> !groupMember.getRequest().getUser().getId().equals(userId))
                .map(groupMember -> {
                    User matchedUser = groupMember.getRequest().getUser();
                    return new CategoryMatchResultResponse.Member(
                            matchedUser.getId(),
                            matchedUser.getNickname(),
                            matchedUser.getDepartmentName(),
                            extractAdmissionYear(matchedUser.getStudentNumber()),
                            matchedUser.getProfileImageUrl(),
                            matchedUser.getMbti(),
                            matchedUser.getContact()
                    );
                })
                .toList();

        return new CategoryMatchResultResponse(
                request.getId(),
                request.getCategory(),
                request.getStatus(),
                groupId,
                members
        );
    }

    @Transactional
    public void confirmResult(
            Long userId,
            Long requestId
    ) {
        CategoryMatchRequest request = categoryMatchRequestRepository.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_REQUEST_NOT_FOUND));

        if (request.getStatus() != CategoryMatchStatus.MATCHED || request.getResultViewedAt() != null) {
            throw new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_RESULT_NOT_READY);
        }

        request.confirmResult();
    }

    private void tryMatch(CategoryType categoryType) {
        List<CategoryMatchRequest> waitingRequests = categoryMatchRequestRepository
                .findAllByCategoryAndStatusOrderByCreatedAtAsc(categoryType, CategoryMatchStatus.WAITING);

        Set<Long> processedRequestIds = new HashSet<>();

        for (CategoryMatchRequest seed : waitingRequests) {
            if (processedRequestIds.contains(seed.getId())) {
                continue;
            }

            List<CategoryMatchRequest> candidates = waitingRequests.stream()
                    .filter(candidate -> !candidate.getId().equals(seed.getId()))
                    .filter(candidate -> !processedRequestIds.contains(candidate.getId()))
                    .filter(candidate -> canBeGrouped(seed, candidate))
                    .toList();

            List<CategoryMatchRequest> selected = new ArrayList<>();
            selected.add(seed);

            int needed = seed.getMemberCount() - 1;
            for (CategoryMatchRequest candidate : candidates) {
                if (needed == 0) {
                    break;
                }
                if (hasBlockedUserInGroup(selected, candidate)) {
                    continue;
                }
                selected.add(candidate);
                needed--;
            }

            if (selected.size() < seed.getMemberCount()) {
                if (!(seed.isAllowPartialMatch() && selected.size() >= 2)) {
                    continue;
                }
            }

            int groupSize = selected.size();
            boolean acceptedByAll = selected.stream()
                    .allMatch(request -> request.isAllowPartialMatch() || request.getMemberCount() == groupSize);

            if (!acceptedByAll) {
                continue;
            }

            CategoryMatchGroup group = categoryMatchGroupRepository.save(
                    new CategoryMatchGroup(categoryType)
            );

            for (CategoryMatchRequest request : selected) {
                request.match();
                categoryMatchMemberRepository.save(
                        new CategoryMatchMember(group, request)
                );
                processedRequestIds.add(request.getId());
            }
        }
    }

    private boolean canBeGrouped(
            CategoryMatchRequest seed,
            CategoryMatchRequest candidate
    ) {
        if (isBlockedBetween(seed.getUser().getId(), candidate.getUser().getId())) {
            return false;
        }

        if (!isMutuallyCompatible(seed, candidate)) {
            return false;
        }

        if (seed.isAllowPartialMatch() || candidate.isAllowPartialMatch()) {
            return true;
        }

        return seed.getMemberCount() == candidate.getMemberCount();
    }

    private boolean hasBlockedUserInGroup(
            List<CategoryMatchRequest> selected,
            CategoryMatchRequest candidate
    ) {
        Long candidateUserId = candidate.getUser().getId();

        return selected.stream()
                .map(request -> request.getUser().getId())
                .anyMatch(selectedUserId -> isBlockedBetween(selectedUserId, candidateUserId));
    }

    private boolean isBlockedBetween(Long userAId, Long userBId) {
        return blockRepository.existsByBlockerIdAndBlockedId(userAId, userBId)
                || blockRepository.existsByBlockerIdAndBlockedId(userBId, userAId);
    }

    private boolean isMutuallyCompatible(
            CategoryMatchRequest first,
            CategoryMatchRequest second
    ) {
        MatchConditions firstConditions = fromConditionsJson(first.getConditions());
        MatchConditions secondConditions = fromConditionsJson(second.getConditions());

        return isUserCompatible(second.getUser(), firstConditions)
                && isUserCompatible(first.getUser(), secondConditions);
    }

    private boolean isUserCompatible(
            User user,
            MatchConditions conditions
    ) {
        if (conditions == null) {
            return true;
        }

        Integer admissionYear = extractFullAdmissionYear(user.getStudentNumber());

        if (conditions.admissionYearMin() != null
                && (admissionYear == null || admissionYear < conditions.admissionYearMin())) {
            return false;
        }

        if (conditions.admissionYearMax() != null
                && (admissionYear == null || admissionYear > conditions.admissionYearMax())) {
            return false;
        }

        if (conditions.department() != null
                && !conditions.department().isBlank()
                && !conditions.department().equals(user.getDepartmentName())) {
            return false;
        }

        if (conditions.mbti() != null && conditions.mbti() != user.getMbti()) {
            return false;
        }

        return true;
    }

    private String toConditionsJson(CategoryMatchCreateRequest.Conditions conditions) {
        if (conditions == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(
                    new MatchConditions(
                            conditions.admissionYearMin(),
                            conditions.admissionYearMax(),
                            conditions.department(),
                            conditions.mbti()
                    )
            );
        } catch (JacksonException e) {
            throw new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_INVALID_CONDITION);
        }
    }

    private MatchConditions fromConditionsJson(String conditionsJson) {
        if (conditionsJson == null || conditionsJson.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(conditionsJson, MatchConditions.class);
        } catch (JacksonException e) {
            throw new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_INVALID_CONDITION);
        }
    }

    private Integer extractAdmissionYear(String studentNumber) {
        if (studentNumber == null || studentNumber.length() < 2) {
            return null;
        }

        String firstTwo = studentNumber.substring(0, 2);
        if (!firstTwo.matches("\\d{2}")) {
            return null;
        }
        return Integer.parseInt(firstTwo);
    }

    private Integer extractFullAdmissionYear(String studentNumber) {
        Integer shortYear = extractAdmissionYear(studentNumber);
        if (shortYear == null) {
            return null;
        }
        return 2000 + shortYear;
    }

    private record MatchConditions(
            Integer admissionYearMin,
            Integer admissionYearMax,
            String department,
            MbtiType mbti
    ) {
    }
}
