package kopo.poly.artlensai.user.dto;

import lombok.Builder;

@Builder
public record PasswordResetDTO (
        String token,
        String newPassword
) {
}
