package kopo.poly.artlensai.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.artlensai.global.auth.AuthInfo;
import kopo.poly.artlensai.global.util.CmmUtil;
import kopo.poly.artlensai.global.util.EncryptUtil;
import kopo.poly.artlensai.user.domain.postgres.entity.UserEntity;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.repository.postgres.UserRepository;
import kopo.poly.artlensai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    /**
     * 조회 전용 트랜잭션
     * - 영속성 컨텍스트의 Dirty Checking/Flush 생략 -> 불필요한 쓰기 방지
     * - 일부 DB/드라이버에 read-only 힌트 전달(최적화 가능)
     */
    @Transactional(readOnly = true)
    @Override
    public UserDTO getEmailExists(UserDTO pDTO) throws Exception {

        // Repository에서 email로 조회
        // - 반환 타입이 Optional<UserDTO>라서 null 대신 "값이 있음/없음"을 표현
        return userRepository.findByEmail(pDTO.email())

                // [값이 있는 경우] -> 해당 아이디가 존재하므로 existsYn = "Y" 인 DTO를 만들어 ㅂ반환
                // - 엔티티의 상세필드는 이 시나리오에 필요 없어서 만들지 않음(불필요한 매핑 비용 절감)
                .map(e -> UserDTO.builder()
                        .existsYn("Y")
                        .build())

                // [값이 없는 경우] -> 해당 아이디가 존재하지 않음 -> existsYn = "N"인 DTO 생성
                // - orElseGet (Supplier)은 "없을 때만" 람다를 실행함
                // (orElse(...)는 항상 인자를 먼저 만들어 성능상 손해가 될 수 있음)
                .orElseGet(() -> UserDTO.builder()
                        .existsYn("N")
                        .build())
                ;
    }

    /**
     * 회원가입
     * @param pDTO 유저정보
     */
    @Override
    public int insertUser(UserDTO pDTO) {

        log.info("{}.insertUser Start!", this.getClass().getName());

        // 반환 코드: 1 = 개인 설정, 2 = 아이디 중복, 0 = 기타 예외
        int res;

        log.info("pDTO: {}", pDTO);

        try {
            // 1. 외부 아이디 중복 여부 확인
            boolean exists = userRepository.existsByEmail(pDTO.email());

            if (exists) {
                // 이미 같은 이메일이 존재 -> 중복 가입 방지
                res = 2;
            } else {
                // 2. DTO -> Entity 반환
                UserEntity pEntity = UserDTO.of(pDTO);

                // 3. DB 저장
                userRepository.save(pEntity);

                res = 1;
            }
        } catch (Exception e){
            log.error("insertUser Error!", e);
            res = 0; // 예외 발생시 0 반환
        }

        log.info("{}.insertUser End!", this.getClass().getName());

        return res;
    }

    @Override
    public UserDTO getUserByEmail(String email) {

        log.info("{}.getUserByEmail Start!", this.getClass().getName());

        log.info("email: {}", email);

        UserDTO rDTO = UserDTO.from(userRepository.findByEmail(email).orElseThrow());

        log.info("{}.getUserByEmail End!", this.getClass().getName());

        return rDTO;
    }

    @Transactional
    @Override
    public void updatePassword(
            String email,
            String newPassword
    ) {

        log.info("{}.updatePassword Start!", this.getClass().getName());

        log.info("email : {}", email);

        // 1. 사용자 조회
        UserEntity pEntity = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("사용자 정보가 존재하지 않습니다.")
                );

        // 2. 비밀번호 변경
        pEntity.changePassword(passwordEncoder.encode(newPassword));

        log.info("{}.updatePassword End!", this.getClass().getName());

    }


    /**
     * Spring Security에서 로그인 처리를 하기 위해 실행하는 함수
     * Spring Security의 인증 기능을 사용하기 위해선 반드시 만들어야 하는 함수
     * <p>
     * Controller로부터 호출되지 않고, Spring Security가 바로 호출함
     * <p>
     * 아이디로 검색하고, 검색한 결과를 기반으로 Spring Security가 비밀번호가 같은지 판단함
     * <p>
     * 아이디와 패스워드가 일치하지 않으면 자동으로 UsernameNotFoundException 발생시킴
     *
     * @param username 사용자 이름
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername Start!: {}", this.getClass().getName());


        log.info("userId : {}", username);


        // 로그인 요청한 사용자의 아이디를 검색함
        // SELECT * FROM USER_INFO WHERE USER_ID = "kimyega@naver.com"
        UserEntity rEntity = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + "Not Found User"));

        // rEntity 데이터를 DTO로 변환하기
        UserDTO rDTO = UserDTO.from(rEntity);

        // 비밀번호가 맞는지 체크 및 권한 부여를 위해 rDTO를 UserDetails를 구현한 AuthInfo에 넣어주기
        return new AuthInfo(rDTO);
    }
}