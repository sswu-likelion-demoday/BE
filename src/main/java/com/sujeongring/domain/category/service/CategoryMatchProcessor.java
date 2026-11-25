package com.sujeongring.domain.category.service;

import com.sujeongring.domain.category.entity.CategoryMatchGroup;
import com.sujeongring.domain.category.entity.CategoryMatchMember;
import com.sujeongring.domain.category.entity.CategoryMatchRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryMatchProcessor {

    private final BlockRepository blockRepository;
    private final CategoryMatchRequestRepository categoryMatchRequestRepository;
    private final CategoryMatchGroupRepository categoryMatchGroupRepository;
    private final CategoryMatchMemberRepository categoryMatchMemberRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tryMatch(CategoryType categoryType) {
        List<CategoryMatchRequest> waitingRequests = categoryMatchRequestRepository
                .findAllByCategoryAndStatusOrderByCreatedAtAscForUpdate(
                        categoryType,
                        CategoryMatchStatus.WAITING
                );

        Set<Long> processedRequestIds = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();

        for (CategoryMatchRequest seed : waitingRequests) {
            if (processedRequestIds.contains(seed.getId())) {
                continue;
            }

            for (int targetGroupSize = seed.getMemberCount(); targetGroupSize >= 2; targetGroupSize--) {
                if (!acceptsGroupSize(seed, targetGroupSize, now)) {
                    continue;
                }

                List<CategoryMatchRequest> selected = new ArrayList<>();
                selected.add(seed);

                for (CategoryMatchRequest candidate : waitingRequests) {
                    if (selected.size() == targetGroupSize) {
                        break;
                    }

                    if (candidate.getId().equals(seed.getId())) {
                        continue;
                    }

                    if (processedRequestIds.contains(candidate.getId())) {
                        continue;
                    }

                    if (!acceptsGroupSize(candidate, targetGroupSize, now)) {
                        continue;
                    }

                    if (!isCompatibleWithGroup(selected, candidate)) {
                        continue;
                    }

                    selected.add(candidate);
                }

                if (selected.size() < targetGroupSize) {
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

                break;
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

        return isMutuallyCompatible(seed, candidate);
    }

    private boolean isCompatibleWithGroup(
            List<CategoryMatchRequest> selected,
            CategoryMatchRequest candidate
    ) {
        return selected.stream()
                .allMatch(request -> canBeGrouped(request, candidate));
    }

    private boolean acceptsGroupSize(
            CategoryMatchRequest request,
            int groupSize,
            LocalDateTime now
    ) {
        if (groupSize > request.getMemberCount()) {
            return false;
        }

        if (groupSize == request.getMemberCount()) {
            return true;
        }

        return isPartialMatchEligible(request, groupSize, now);
    }

    private boolean isPartialMatchEligible(
            CategoryMatchRequest request,
            int groupSize,
            LocalDateTime now
    ) {
        if (!request.isAllowPartialMatch() || groupSize < 2) {
            return false;
        }

        LocalDateTime createdAt = request.getCreatedAt();
        return createdAt != null && !createdAt.plusHours(24).isAfter(now);
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

        Integer admissionYear = extractAdmissionYear(user.getStudentNumber());

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
