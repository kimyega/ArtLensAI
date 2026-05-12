package kopo.poly.artlensai.user.service;

import kopo.poly.artlensai.user.dto.UserDTO;

import java.util.Optional;

public interface IUserService {
    /**
     * 이메일로 유저 조회
     */
    public Optional<UserDTO> getUserLogin(UserDTO pDTO);

    /**
     * 회원가입
     */
    void insertUser(UserDTO pDTO);

    boolean existsEmail(String email);

    UserDTO getUserByEmail(String email);

    void updatePassword(String email, String newPassword);
}
