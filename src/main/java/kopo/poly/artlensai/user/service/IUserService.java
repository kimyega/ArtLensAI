package kopo.poly.artlensai.user.service;

import kopo.poly.artlensai.user.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public interface IUserService extends UserDetailsService {
    /**
     * 이메일 중복체크
     */
    UserDTO getEmailExists(UserDTO pDTO) throws Exception;

    /**
     * 회원가입
     */
    int insertUser(UserDTO pDTO);

    /**
     * 본인 회원 정보 조회
     */
    UserDTO getUserByEmail(String email);

    /**
     * 비밀번호 변경
     */
    void updatePassword(String email, String newPassword);
}
