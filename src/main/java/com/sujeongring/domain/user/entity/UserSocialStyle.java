package com.sujeongring.domain.user.entity;

import com.sujeongring.domain.user.enums.SocialStyleType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_social_styles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_social_styles_user_style",
                        columnNames = {"user_id", "social_style"}
                )
        }
)
public class UserSocialStyle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_social_style_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_style", nullable = false, length = 40)
    private SocialStyleType socialStyle;

    public UserSocialStyle(
            User user,
            SocialStyleType socialStyle
    ) {
        this.user = user;
        this.socialStyle = socialStyle;
    }
}
