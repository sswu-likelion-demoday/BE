package com.sujeongring.domain.user.repository;

import com.sujeongring.domain.user.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockerIdAndBlockedId(
            Long blockerId,
            Long blockedId
    );

    List<Block> findAllByBlockerId(Long blockerId);

    // 내가 차단했거나 나를 차단한 기록 조회
    List<Block> findAllByBlockerIdOrBlockedId(
            Long blockerId,
            Long blockedId
    );

    boolean existsByBlockerIdAndBlockedIdOrBlockerIdAndBlockedId(
            Long blockerId,
            Long blockedId,
            Long reverseBlockerId,
            Long reverseBlockedId
    );
}
