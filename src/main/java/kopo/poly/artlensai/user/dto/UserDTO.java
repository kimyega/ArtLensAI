package kopo.poly.artlensai.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import kopo.poly.artlensai.user.domain.postgres.entity.UserEntity;
import lombok.Builder;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
        LocalDateTime createdAt,

        String existsYn
) {
        /**
         * 비밀번호, 권한 등 회원 가입을 위한 정보 만들기
         */
        public static UserDTO createUser(UserDTO pDTO, String password) throws Exception {

                return UserDTO.builder()
                        .userId(pDTO.userId())
                        .email(pDTO.email())
                        .nickname(pDTO.nickname())
                        .password(password)
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .build();

        }

        /**
         * DTO 결과를 entity 변환하기
         * Jackson 객체를 통해 처리도 가능함
         */
        public static UserEntity of(UserDTO dto) {

                return UserEntity.builder()
                        .userId(dto.userId())
                        .email(dto.email())
                        .nickname(dto.nickname())
                        .password(dto.password())
                        .isActive(dto.isActive())
                        .createdAt(dto.createdAt())
                        .build();
        }

        /**
         * JPA로 전달받은 entity 결과를 DTO로 반환하기
         * Jackson 객체를 통해 처리도 가능함
         */
        public static UserDTO from(UserEntity entity) {

                return UserDTO.builder()
                        .userId(entity.getUserId())
                        .email(entity.getEmail())
                        .nickname(entity.getNickname())
                        .password(entity.getPassword())
                        .isActive(entity.getIsActive())
                        .createdAt(entity.getCreatedAt())
                        .build();
        }
}