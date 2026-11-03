
package com.sujeongring.domain.matching.service;

import com.sujeongring.domain.chat.repository.ChatRoomRepository;
import com.sujeongring.domain.chat.repository.ChatReadStatusRepository;
import com.sujeongring.domain.matching.dto.response.matchrequest.CreateMatchRequestResponse;
import com.sujeongring.domain.matching.entity.MatchRecommendation;
import com.sujeongring.domain.matching.entity.MatchRequest;
import com.sujeongring.domain.matching.exception.MatchRequestErrorCode;
import com.sujeongring.domain.matching.repository.MatchRecommendationRepository;
import com.sujeongring.domain.matching.repository.MatchRequestRepository;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.relationship.repository.RelationshipRepository;
import com.sujeongring.domain.relationship.service.RelationshipSlotService;
import com.sujeongring.domain.user.repository.BlockRepository;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.global.error.CommonErrorCode;
import com.sujeongring.global.error.exception.BaseException;
import com.sujeongring.domain.chat.entity.ChatRoom;
import com.sujeongring.domain.chat.entity.ChatReadStatus;
import com.sujeongring.domain.matching.dto.response.matchrequest.MatchRequestItemResponse;
import com.sujeongring.domain.matching.dto.response.matchrequest.MatchRequestListResponse;
import com.sujeongring.domain.matching.dto.response.matchrequest.MatchRequestUserResponse;
import com.sujeongring.domain.matching.dto.response.RelationshipSlotsResponse;
import com.sujeongring.domain.matching.enums.MatchRequestStatus;
import com.sujeongring.domain.matching.dto.response.matchrequest.MatchRequestDetailResponse;
import com.sujeongring.domain.matching.enums.MatchingType;
import com.sujeongring.domain.matching.dto.response.matchrequest.AcceptMatchRequestResponse;
import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.quest.service.QuestInitializationService;


import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchRequestService {

    private final UserRepository userRepository;
    private final MatchRecommendationRepository recommendationRepository;
    private final MatchRequestRepository matchRequestRepository;
    private final RelationshipRepository relationshipRepository;
    private final BlockRepository blockRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final QuestInitializationService questInitializationService;
    private final RelationshipSlotService relationshipSlotService;

    @Transactional
    public CreateMatchRequestResponse create(
            Long userId,
            Long recommendationId
    ) {
        // 사용자 조회 및 동시 요청 직렬화
        User sender = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new BaseException(CommonErrorCode.NOT_FOUND)
                );

        // 추천 결과 조회
        MatchRecommendation recommendation =
                recommendationRepository.findById(recommendationId)
                        .orElseThrow(() ->
                                new BaseException(
                                        MatchRequestErrorCode.RECOMMENDATION_NOT_FOUND
                                )
                        );

        if (!recommendation.getUser().getId().equals(userId)) {
            throw new BaseException(
                    MatchRequestErrorCode.RECOMMENDATION_ACCESS_DENIED
            );
        }

        User receiver = recommendation.getCandidate();
        Long receiverId = receiver.getId();

        // 자기 자신에게 요청 방지
        if (userId.equals(receiverId)) {
            throw new BaseException(
                    MatchRequestErrorCode.SELF_REQUEST_NOT_ALLOWED
            );
        }

        //동일 추천 결과 중복 요청 방지
        if (matchRequestRepository
                .existsByMatchRecommendationId(recommendationId)) {
            throw new BaseException(
                    MatchRequestErrorCode.DUPLICATE_MATCH_REQUEST
            );
        }

        // ACTIVE 관계 확인
        if (relationshipRepository.existsActiveRelationshipBetween(
                userId,
                receiverId,
                RelationshipStatus.ACTIVE
        )) {
            throw new BaseException(
                    MatchRequestErrorCode.ACTIVE_RELATIONSHIP_EXISTS
            );
        }

        // 양방향 차단 관계 확인
        boolean blocked =
                blockRepository.existsByBlockerIdAndBlockedId(
                        userId, receiverId
                )
                        || blockRepository.existsByBlockerIdAndBlockedId(
                        receiverId, userId
                );

        if (blocked) {
            throw new BaseException(
                    MatchRequestErrorCode.BLOCKED_USER
            );
        }

        //  채팅 요청 생성
        MatchRequest matchRequest = new MatchRequest(
                recommendation,
                sender,
                receiver
        );

        MatchRequest saved = matchRequestRepository.save(matchRequest);

        return CreateMatchRequestResponse.from(saved);
    }


    @Transactional(readOnly = true)
    public MatchRequestListResponse getMatchRequests(Long userId) {

        LocalDateTime now = LocalDateTime.now();

        // 관계 슬롯 계산
        long activeCount =
                relationshipSlotService.getActiveRelationshipCount(userId);

        int used = Math.toIntExact(activeCount);
        int total = RelationshipSlotService.MAX_ACTIVE_RELATIONSHIPS;
        int available = Math.max(0, total - used);

        RelationshipSlotsResponse slots =
                new RelationshipSlotsResponse(used, available, total);

        // 받은 요청 조회 (PENDING + 미만료)
        List<MatchRequest> received =
                matchRequestRepository.findVisibleReceivedRequests(
                        userId,
                        MatchRequestStatus.PENDING,
                        now
                );

        // 보낸 요청 조회
        // PENDING + 미만료
        // ACCEPTED + 미확인 + 수락 후 24시간 이내
        List<MatchRequest> sent =
                matchRequestRepository.findVisibleSentRequests(
                        userId,
                        MatchRequestStatus.PENDING,
                        MatchRequestStatus.ACCEPTED,
                        now,
                        now.minusDays(1)
                );

        // 받은 요청 DTO 변환
        List<MatchRequestItemResponse> receivedResponses =
                received.stream()
                        .map(request -> new MatchRequestItemResponse(
                                request.getId(),
                                new MatchRequestUserResponse(
                                        request.getSender().getId(),
                                        request.getSender().getNickname()
                                ),
                                request.getStatus(),
                                request.getExpiresAt(),
                                null
                        ))
                        .toList();

        // 보낸 요청 DTO 변환
        List<MatchRequestItemResponse> sentResponses =
                sent.stream()
                        .map(this::toSentRequestResponse)
                        .toList();

        // 최종 응답 반환
        return MatchRequestListResponse.of(
                slots,
                receivedResponses,
                sentResponses
        );
    }

    private MatchRequestItemResponse toSentRequestResponse(
            MatchRequest request
    ) {
        Long chatRoomId = null;

        if (request.getStatus() == MatchRequestStatus.ACCEPTED) {
            chatRoomId = chatRoomRepository
                    .findByMatchRequestId(request.getId())
                    .map(ChatRoom::getId)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "수락된 요청에 연결된 채팅방이 없습니다."
                            )
                    );
        }

        return new MatchRequestItemResponse(
                request.getId(),
                new MatchRequestUserResponse(
                        request.getReceiver().getId(),
                        request.getReceiver().getNickname()
                ),
                request.getStatus(),
                null,
                chatRoomId
        );
    }


    @Transactional(readOnly = true)
    public MatchRequestDetailResponse getMatchRequestDetail(
            Long userId,
            Long requestId
    ) {
        //  채팅 요청 조회
        MatchRequest request = matchRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new BaseException(CommonErrorCode.NOT_FOUND)
                );

        // 로그인한 사용자가 보낸/받은 사람인지 확인
        boolean isSender = request.getSender().getId().equals(userId);
        boolean isReceiver = request.getReceiver().getId().equals(userId);

        //요청 당사자가 아니면 접근 차단
        if (!isSender && !isReceiver) {
            throw new BaseException(
                    MatchRequestErrorCode.MATCH_REQUEST_ACCESS_DENIED
            );
        }

        // 요청 방향과 상대방 결정
        String direction = isSender ? "SENT" : "RECEIVED";

        User otherUser = isSender
                ? request.getReceiver()
                : request.getSender();

        //  연결된 추천 결과 조회
        MatchRecommendation recommendation =
                request.getMatchRecommendation();

        //  사주 매칭일 때만 궁합 점수 반환
        Integer compatibilityScore = null;

        if (recommendation.getMatchingType() == MatchingType.SAJU) {
            compatibilityScore = recommendation.getCompatibilityScore();
        }

        //  학번 앞 두 자리 추출
        String studentNumber = otherUser.getStudentNumber();

        Integer admissionYear = null;

        if (studentNumber != null
                && studentNumber.length() >= 2
                && studentNumber.substring(0, 2).matches("\\d{2}")) {
            admissionYear = Integer.parseInt(
                    studentNumber.substring(0, 2)
            );
        }

        // 상대방 프로필 DTO 생성
        MatchRequestDetailResponse.UserProfile profile =
                new MatchRequestDetailResponse.UserProfile(
                        otherUser.getId(),
                        otherUser.getNickname(),
                        admissionYear,
                        otherUser.getProfileImageUrl()
                );

        //  최종 응답 반환
        return new MatchRequestDetailResponse(
                request.getId(),
                direction,
                request.getStatus(),
                recommendation.getMatchingType(),
                compatibilityScore,
                profile,
                request.getExpiresAt()
        );
    }


    //수락 메서드

    @Transactional
    public AcceptMatchRequestResponse acceptMatchRequest(
            Long userId,
            Long requestId
    ) {
        MatchRequest requestSnapshot = matchRequestRepository.findById(requestId)
                .orElseThrow(() -> new BaseException(
                        MatchRequestErrorCode.MATCH_REQUEST_NOT_FOUND
                ));

        // 요청 수신자만 수락 가능
        if (!requestSnapshot.getReceiver().getId().equals(userId)) {
            throw new BaseException(
                    MatchRequestErrorCode.MATCH_REQUEST_ACCESS_DENIED
            );
        }

        Long senderId = requestSnapshot.getSender().getId();
        Long receiverId = requestSnapshot.getReceiver().getId();

        // 양쪽 사용자 행을 ID 오름차순으로 잠금
        Long firstId = Math.min(senderId, receiverId);
        Long secondId = Math.max(senderId, receiverId);

        userRepository.findByIdForUpdate(firstId)
                .orElseThrow(() ->
                        new BaseException(CommonErrorCode.NOT_FOUND));

        userRepository.findByIdForUpdate(secondId)
                .orElseThrow(() ->
                        new BaseException(CommonErrorCode.NOT_FOUND));

        MatchRequest request = matchRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new BaseException(
                        MatchRequestErrorCode.MATCH_REQUEST_NOT_FOUND
                ));

        if (!request.getReceiver().getId().equals(userId)) {
            throw new BaseException(
                    MatchRequestErrorCode.MATCH_REQUEST_ACCESS_DENIED
            );
        }

        if (request.getStatus() != MatchRequestStatus.PENDING) {
            throw new BaseException(
                    MatchRequestErrorCode.MATCH_REQUEST_INVALID_STATUS
            );
        }

        if (!request.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BaseException(
                    MatchRequestErrorCode.MATCH_REQUEST_EXPIRED
            );
        }

        boolean blocked =
                blockRepository.existsByBlockerIdAndBlockedId(senderId, receiverId)
                        || blockRepository.existsByBlockerIdAndBlockedId(receiverId, senderId);

        if (blocked) {
            throw new BaseException(
                    MatchRequestErrorCode.BLOCKED_USER
            );
        }

        //  양쪽 관계 슬롯 재검사
        relationshipSlotService.validateAvailableSlot(receiverId);
        relationshipSlotService.validateAvailableSlot(senderId);

        //기존 ACTIVE 관계 중복 방지
        if (relationshipSlotService.existsActiveRelationshipBetween(
                senderId, receiverId)) {
            throw new BaseException(
                    MatchRequestErrorCode.ACTIVE_RELATIONSHIP_EXISTS
            );
        }
        request.accept();

        //관계 생성
        Relationship relationship = relationshipRepository.save(
                new Relationship(
                        request.getSender(),
                        request.getReceiver(),
                        request
                )
        );

        // 채팅방 생성
        ChatRoom chatRoom = chatRoomRepository.save(
                new ChatRoom(relationship)
        );

        chatReadStatusRepository.saveAll(
                List.of(
                        new ChatReadStatus(chatRoom, relationship.getUserA()),
                        new ChatReadStatus(chatRoom, relationship.getUserB())
                )
        );

        questInitializationService.initializeQuests(relationship);

        return new AcceptMatchRequestResponse(
                request.getId(),
                relationship.getId(),
                chatRoom.getId()
        );
    }


    //거절 메서드
    @jakarta.transaction.Transactional
    public void rejectMatchRequest(Long userId, Long requestId) {

        //요청 조회 및 잠금
        MatchRequest request = matchRequestRepository
                .findByIdForUpdate(requestId)
                .orElseThrow(() -> new BaseException(
                        MatchRequestErrorCode.MATCH_REQUEST_NOT_FOUND
                ));

        // PENDING 상태인지 확인
        if (request.getStatus() != MatchRequestStatus.PENDING) {
            throw new BaseException(
                    MatchRequestErrorCode.MATCH_REQUEST_INVALID_STATUS
            );
        }

        //요청 수신자 본인인지 확인
        if (!request.getReceiver().getId().equals(userId)) {
            throw new BaseException(
                    MatchRequestErrorCode.MATCH_REQUEST_ACCESS_DENIED
            );
        }

        // 만료 여부 확인
        if (!request.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BaseException(
                    MatchRequestErrorCode.MATCH_REQUEST_EXPIRED
            );
        }

        request.reject();
    }
}
