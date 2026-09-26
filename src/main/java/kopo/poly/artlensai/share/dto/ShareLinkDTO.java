package kopo.poly.artlensai.share.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ShareLinkDTO(

        Long shareId,

        String token,

        String shareUrl,

        boolean isPublic,

        LocalDateTime expiresAt,

        long viewCount,

        LocalDateTime createdAt

) {
}
