package com.sujeongring.domain.category.service;

import com.sujeongring.domain.category.dto.request.CategoryMatchCreateRequest;
import com.sujeongring.domain.category.dto.response.CategoryMatchCreateResponse;
import com.sujeongring.domain.category.dto.response.CategoryMatchResultResponse;
import com.sujeongring.domain.category.dto.response.CategoryMatchStatusResponse;
import com.sujeongring.domain.category.entity.CategoryMatchMember;
import com.sujeongring.domain.category.entity.CategoryMatchRequest;
import com.sujeongring.domain.category.enums.CategoryMatchState;
import com.sujeongring.domain.category.enums.CategoryMatchStatus;
import com.sujeongring.domain.category.enums.CategoryType;
import com.sujeongring.domain.category.exception.CategoryMatchErrorCode;
import com.sujeongring.domain.category.exception.CategoryMatchException;
import com.sujeongring.domain.category.repository.CategoryMatchMemberRepository;
import com.sujeongring.domain.category.repository.CategoryMatchRequestRepository;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.enums.MbtiType;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.global.error.CommonErrorCode;
import com.sujeongring.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryMatchService {

    private final UserRepository userRepository;
    private final CategoryMatchRequestRepository categoryMatchRequestRepository;
    private final CategoryMatchMemberRepository categoryMatchMemberRepository;
    private final CategoryMatchProcessor categoryMatchProcessor;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

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

    public CategoryMatchCreateResponse create(
            Long userId,
            CategoryMatchCreateRequest request
    ) {
        if (request.conditions() != null
                && request.conditions().admissionYearMin() != null
                && request.conditions().admissionYearMax() != null
                && request.conditions().admissionYearMin() > request.conditions().admissionYearMax()) {
            throw new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_INVALID_CONDITION);
        }

        if (request.conditions() == null
                || request.conditions().admissionYearMin() == null
                || request.conditions().admissionYearMax() == null) {
            throw new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_INVALID_CONDITION);
        }

        String conditionsJson = toConditionsJson(request.conditions());

        Long savedRequestId = transactionTemplate.execute(status -> {
            User user = userRepository.findByIdForUpdate(userId)
                    .orElseThrow(() -> new BaseException(CommonErrorCode.NOT_FOUND));

            if (user.getContact() == null || user.getContact().isBlank()) {
                throw new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_CONTACT_REQUIRED);
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

            return saved.getId();
        });

        if (savedRequestId == null) {
            throw new CategoryMatchException(CategoryMatchErrorCode.CATEGORY_MATCH_REQUEST_NOT_FOUND);
        }

        try {
            categoryMatchProcessor.tryMatch(request.category());
        } catch (DataIntegrityViolationException
                 | PessimisticLockingFailureException e) {
            log.warn("category match deferred by concurrent update. category={}, requestId={}",
                    request.category(), savedRequestId, e);
        }

        CategoryMatchRequest latest = categoryMatchRequestRepository.findById(savedRequestId)
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
                            matchedUser.getName(),
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

    public void retryWaitingMatches() {
        for (CategoryType categoryType : CategoryType.values()) {
            categoryMatchProcessor.tryMatch(categoryType);
        }
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

    private Integer extractAdmissionYear(String studentNumber) {
        if (studentNumber == null || studentNumber.length() < 4) {
            return null;
        }

        String year = studentNumber.substring(2, 4);

        if (!year.matches("\\d{2}")) {
            return null;
        }

        return Integer.parseInt(year);
    }

    private record MatchConditions(
            Integer admissionYearMin,
            Integer admissionYearMax,
            String department,
            MbtiType mbti
    ) {
    }
}
