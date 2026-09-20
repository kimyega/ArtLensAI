package kopo.poly.artlensai.analysis.domain.postgres.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "upload_images")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UploadImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upload_id")
    private Long uploadId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "img_key", nullable = false)
    private String imgKey;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "format")
    private String format;

    @Column(name = "img_hash")
    private String imgHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
