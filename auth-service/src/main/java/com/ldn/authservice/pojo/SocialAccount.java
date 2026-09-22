package com.ldn.authservice.pojo;
import com.ldn.authservice.enums.SocialAccountProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "social_accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_provider_user", columnNames = {"provider", "provider_account_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private SocialAccountProvider provider;

    @Column(name = "provider_account_id", nullable = false)
    private String providerAccountId;
}
