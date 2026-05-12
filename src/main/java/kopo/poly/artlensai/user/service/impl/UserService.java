package kopo.poly.artlensai.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import kopo.poly.artlensai.global.util.CmmUtil;
import kopo.poly.artlensai.global.util.EncryptUtil;
import kopo.poly.artlensai.user.domain.postgres.entity.UserEntity;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.repository.postgres.UserRepository;
import kopo.poly.artlensai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    @Override
    public Optional<UserDTO> getUserLogin(UserDTO pDTO) {

        log.info("{}.getUserLogin() Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String password = CmmUtil.nvl(pDTO.password());

        String encPassword = EncryptUtil.encHashSHA256(password);

        log.info("{}.getUserLogin() End!", this.getClass().getName());

        return userRepository.findByEmailAndPassword(email, encPassword)
                .map(entity -> objectMapper.convertValue(entity, UserDTO.class));
    }

    @Transactional
    @Override
    public void insertUser(UserDTO pDTO) {
        log.info("{}.insertUser() Start!", this.getClass().getName());

        String email = CmmUtil.nvl(pDTO.email());
        String nickname = CmmUtil.nvl(pDTO.nickname());
        String password = CmmUtil.nvl(pDTO.password());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        String encPassword = EncryptUtil.encHashSHA256(password);

        UserEntity entity = UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .password(encPassword)
                .isActive(true)
                .build();


        userRepository.save(entity);

        log.info("{}.insertUser() End!", this.getClass().getName());
    }

    @Override
    public boolean existsEmail(String email) {
        log.info("{}.existsEmail Start! email: {}", this.getClass().getName(), email);

        boolean exists = userRepository.findByEmail(email).isPresent();

        log.info("{}.existsEmail End! exists: {}", this.getClass().getName(), exists);

        return exists;
    }

    @Override
    public UserDTO getUserByEmail(String email) {

        log.info("{}.getUserByEmail() Start! email: {}", this.getClass().getName(), email);

        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        log.info("{}.getUserByEmail() End! email: {}", this.getClass().getName(), email);

        return objectMapper.convertValue(entity, UserDTO.class);
    }

    @Transactional
    @Override
    public void updatePassword(String email, String newPassword) {

        log.info("{}.updatePassword Start! email: {}", this.getClass().getName(), email);

        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        // 🔥 비밀번호 암호화
        String encPassword = EncryptUtil.encHashSHA256(newPassword);

        // 🔥 엔티티 수정 (JPA dirty checking)
        entity.setPassword(encPassword);

        userRepository.save(entity);

        log.info("{}.updatePassword End!", this.getClass().getName());
    }
}