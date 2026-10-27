package com.sujeongring.domain.user.repository;

import com.sujeongring.domain.user.entity.SajuProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SajuProfileRepository extends JpaRepository<SajuProfile, Long> {

    Optional<SajuProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
