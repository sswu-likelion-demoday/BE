package com.sujeongring.domain.user.entity;

import com.sujeongring.domain.user.enums.HobbyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_hobbies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_hobbies_user_hobby",
                        columnNames = {"user_id", "hobby"}
                )
        }
)
public class UserHobby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_hobby_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "hobby", nullable = false, length = 40)
    private HobbyType hobby;

    public UserHobby(
            User user,
            HobbyType hobby
    ) {
        this.user = user;
        this.hobby = hobby;
    }
}
