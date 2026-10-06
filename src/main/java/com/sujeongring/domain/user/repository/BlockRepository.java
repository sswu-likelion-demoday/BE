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
}
