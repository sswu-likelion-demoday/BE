package com.sujeongring.domain.category.repository;

import com.sujeongring.domain.category.entity.CategoryMatchRequest;
import com.sujeongring.domain.category.enums.CategoryMatchStatus;
import com.sujeongring.domain.category.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryMatchRequestRepository extends JpaRepository<CategoryMatchRequest, Long> {

    Optional<CategoryMatchRequest> findTopByUserIdAndCategoryOrderByCreatedAtDesc(
            Long userId,
            CategoryType category
    );

    Optional<CategoryMatchRequest> findByIdAndUserId(Long id, Long userId);

    List<CategoryMatchRequest> findAllByCategoryAndStatusOrderByCreatedAtAsc(
            CategoryType category,
            CategoryMatchStatus status
    );

    @Query("""
            select count(cmr) > 0
            from CategoryMatchRequest cmr
            where cmr.user.id = :userId
              and cmr.category = :category
              and (cmr.status = :waiting
                   or (cmr.status = :matched and cmr.resultViewedAt is null))
            """)
    boolean existsActiveRequestByUserAndCategory(
            @Param("userId") Long userId,
            @Param("category") CategoryType category,
            @Param("waiting") CategoryMatchStatus waiting,
            @Param("matched") CategoryMatchStatus matched
    );
}
