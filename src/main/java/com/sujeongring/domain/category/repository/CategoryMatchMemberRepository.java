package com.sujeongring.domain.category.repository;

import com.sujeongring.domain.category.entity.CategoryMatchMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryMatchMemberRepository extends JpaRepository<CategoryMatchMember, Long> {

    Optional<CategoryMatchMember> findByRequestId(Long requestId);

    @Query("""
            select cmm
            from CategoryMatchMember cmm
            join fetch cmm.request cmr
            join fetch cmr.user u
            where cmm.group.id = :groupId
            order by cmm.id asc
            """)
    List<CategoryMatchMember> findAllByGroupIdWithUser(
            @Param("groupId") Long groupId
    );
}
