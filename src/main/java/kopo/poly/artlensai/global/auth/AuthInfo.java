package kopo.poly.artlensai.global.auth;

import kopo.poly.artlensai.global.util.CmmUtil;
import kopo.poly.artlensai.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @param userDTO  로그인된 사용자 정보 UserRepository로부터 조회된 정보를 저장하기 위한 객체
 */
@Slf4j
public record AuthInfo(UserDTO userDTO) implements UserDetails {

    /**
     * 로그인한 사용자의 권한 부여하기
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<GrantedAuthority> pSet = new HashSet<>();

        /*
        String roles = CmmUtil.nvl(userDTO.roles());

        if (!roles.isEmpty()) {
            for (String role : roles.split(",")) {
                pSet.add(new SimpleGrantedAuthority(role));
            }
        }

         */

        return Set.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * 사용자의 id를 반환 (unique한 값)
     */
    @Override
    public String getPassword() {
        return CmmUtil.nvl(userDTO.password());
    }

    // 사용자의 password를 반환
    @Override
    public String getUsername() {
        return CmmUtil.nvl(userDTO.email());
    }

    // 계정 완료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        return true; // true -> 완료되지 않았음
    }

    // 계정 잠금 여부 반환
    @Override
    public boolean isAccountNonLocked() {
        return true; // true -> 잠금되지 않았음
    }

    // 비밀번호의 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // true -> 만료되지 않았음
    }

    // 계정 사용 가능 여부 반환
    @Override
    public boolean isEnabled() {
        return true; // true -> 사용 가능
    }
}
