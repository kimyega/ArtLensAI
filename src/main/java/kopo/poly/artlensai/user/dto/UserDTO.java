package kopo.poly.artlensai.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Builder
public record UserDTO (

        Long userId,

        @NotBlank(message = "이메일은 필수 입력 사항입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "닉네임은 필수 입력 사항입니다.")
        String nickname,

        @NotBlank(message = "비밀번호는 필수 입력 사항입니다.")
        String password,

        Boolean isActive,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime createdAt
) {}