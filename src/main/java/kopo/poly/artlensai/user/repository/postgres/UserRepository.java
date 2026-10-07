package kopo.poly.artlensai.user.repository.postgres;

import kopo.poly.artlensai.user.domain.postgres.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("""
        UPDATE UserEntity u
           SET u.nickname = :nickname
         WHERE u.email = :email
    """)
    int updateNicknameByEmail(
            String email,
            String nickname
    );
}
