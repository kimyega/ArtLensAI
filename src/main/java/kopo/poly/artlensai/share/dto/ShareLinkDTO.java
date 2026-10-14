package kopo.poly.artlensai.share.dto;

import kopo.poly.artlensai.share.domain.postgres.entity.ShareLinkEntity;
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

        /**
         * JPA로 전달받은 entity 결과를 DTO로 반환하기
         * viewCount는 호출 측에서 (DB 값 + Redis에 아직 안 반영된 값)을 합쳐서 넘겨준다
         */
        public static ShareLinkDTO from(ShareLinkEntity entity, String frontendUrl, long viewCount) {

                return ShareLinkDTO.builder()
                        .shareId(entity.getShareId())
                        .token(entity.getToken())
                        .shareUrl(frontendUrl + "/share/" + entity.getToken())
                        .isPublic(entity.getIsPublic())
                        .expiresAt(entity.getExpiresAt())
                        .viewCount(viewCount)
                        .createdAt(entity.getCreatedAt())
                        .build();
        }
}
