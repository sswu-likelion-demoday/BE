package com.sujeongring.domain.user.service;

import com.sujeongring.domain.matching.entity.MatchRequest;
import com.sujeongring.domain.matching.enums.MatchRequestStatus;
import com.sujeongring.domain.matching.repository.MatchRequestRepository;
import com.sujeongring.domain.relationship.entity.Relationship;
import com.sujeongring.domain.relationship.enums.RelationshipStatus;
import com.sujeongring.domain.relationship.repository.RelationshipRepository;
import com.sujeongring.domain.user.dto.response.BlockListResponse;
import com.sujeongring.domain.user.dto.response.BlockedUserResponse;
import com.sujeongring.domain.user.entity.Block;
import com.sujeongring.domain.user.entity.User;
import com.sujeongring.domain.user.exception.UserErrorCode;
import com.sujeongring.domain.user.repository.BlockRepository;
import com.sujeongring.domain.user.repository.UserRepository;
import com.sujeongring.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final RelationshipRepository relationshipRepository;
    private final MatchRequestRepository matchRequestRepository;

    @Transactional
    public void blockUser(Long blockerId, Long targetUserId) {
        if (blockerId.equals(targetUserId)) {
            throw new BaseException(UserErrorCode.BLOCK_SELF_NOT_ALLOWED);
        }

        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, targetUserId)) {
            throw new BaseException(UserErrorCode.BLOCK_ALREADY_EXISTS);
        }

        Block block = new Block(blocker, target);
        blockRepository.save(block);

         List<Relationship> blockableRelationships = relationshipRepository
                .findPairRelationshipsByStatuses(
                        blockerId,
                        targetUserId,
                        List.of(RelationshipStatus.ACTIVE, RelationshipStatus.COMPLETED)
                );

        for (Relationship relationship : blockableRelationships) {
            relationship.endByBlock();
        }

        List<MatchRequest> pendingRequests = matchRequestRepository
                .findPairRequestsByStatus(
                        blockerId,
                        targetUserId,
                        MatchRequestStatus.PENDING
                );

        for (MatchRequest request : pendingRequests) {
            request.cancel();
        }
    }

    @Transactional(readOnly = true)
    public BlockListResponse getBlockedUsers(Long userId) {
        List<BlockedUserResponse> blockedUsers = blockRepository.findAllByBlockerId(userId)
                .stream()
                .map(block -> new BlockedUserResponse(
                        block.getBlocked().getId(),
                        block.getBlocked().getNickname(),
                        block.getCreatedAt()
                ))
                .toList();

        return new BlockListResponse(blockedUsers.size(), blockedUsers);
    }

    @Transactional
    public void unblockUser(Long blockerId, Long targetUserId) {
        userRepository.findById(blockerId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        userRepository.findById(targetUserId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        if (!blockRepository.existsByBlockerIdAndBlockedId(blockerId, targetUserId)) {
            throw new BaseException(UserErrorCode.BLOCK_NOT_FOUND);
        }

        blockRepository.deleteByBlockerIdAndBlockedId(blockerId, targetUserId);
    }

    @Transactional(readOnly = true)
    public boolean isBlockedBetween(Long userAId, Long userBId) {
        if (userAId.equals(userBId)) {
            return false;
        }

        return blockRepository.existsByBlockerIdAndBlockedId(userAId, userBId)
                || blockRepository.existsByBlockerIdAndBlockedId(userBId, userAId);
    }
}
