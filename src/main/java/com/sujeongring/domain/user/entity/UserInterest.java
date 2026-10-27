package com.sujeongring.domain.user.entity;

import com.sujeongring.domain.user.enums.InterestType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_interests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_interests_user_interest",
                        columnNames = {"user_id", "interest"}
                )
        }
)
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_interest_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest", nullable = false, length = 40)
    private InterestType interest;

    public UserInterest(
            User user,
            InterestType interest
    ) {
        this.user = user;
        this.interest = interest;
    }
}
