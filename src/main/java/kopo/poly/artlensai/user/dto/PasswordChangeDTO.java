package kopo.poly.artlensai.user.dto;

import lombok.Builder;

@Builder
public record PasswordChangeDTO(
        String currentPassword,
        String newPassword
) {
}
