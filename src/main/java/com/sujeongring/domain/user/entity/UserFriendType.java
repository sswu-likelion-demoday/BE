package com.sujeongring.domain.user.entity;

import com.sujeongring.domain.user.enums.FriendType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_friend_types",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_friend_types_user_type",
                        columnNames = {"user_id", "friend_type"}
                )
        }
)
public class UserFriendType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_friend_type_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "friend_type", nullable = false, length = 40)
    private FriendType friendType;

    public UserFriendType(
            User user,
            FriendType friendType
    ) {
        this.user = user;
        this.friendType = friendType;
    }
}
