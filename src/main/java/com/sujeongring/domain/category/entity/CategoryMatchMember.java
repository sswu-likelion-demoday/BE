package com.sujeongring.domain.category.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "category_match_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_match_members_request",
                        columnNames = "category_match_request_id"
                )
        }
)
public class CategoryMatchMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_match_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_match_group_id", nullable = false)
    private CategoryMatchGroup group;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "category_match_request_id",
            nullable = false,
            unique = true
    )
    private CategoryMatchRequest request;

    public CategoryMatchMember(
            CategoryMatchGroup group,
            CategoryMatchRequest request
    ) {
        this.group = group;
        this.request = request;
    }
}
