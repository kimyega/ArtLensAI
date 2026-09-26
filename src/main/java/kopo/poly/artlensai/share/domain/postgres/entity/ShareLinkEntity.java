package kopo.poly.artlensai.share.domain.postgres.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_shares")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_id")
    private Long shareId;

    @Column(name = "token", unique = true, nullable = false)
    private String token;

    @Column(name = "result_id", nullable = false)
    private String resultId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
