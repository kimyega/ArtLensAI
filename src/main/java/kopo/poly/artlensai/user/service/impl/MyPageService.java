package kopo.poly.artlensai.user.service.impl;

import kopo.poly.artlensai.analysis.dto.TagResultDTO;
import kopo.poly.artlensai.analysis.repository.mongo.AnalysisResultsRepository;
import kopo.poly.artlensai.analysis.repository.mongo.document.AnalysisResultsDocument;
import kopo.poly.artlensai.user.domain.postgres.entity.UserEntity;
import kopo.poly.artlensai.user.dto.MyPageStatDTO;
import kopo.poly.artlensai.user.dto.UserDTO;
import kopo.poly.artlensai.user.repository.postgres.UserRepository;
import kopo.poly.artlensai.user.service.IMyPageService;
import kopo.poly.artlensai.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageService implements IMyPageService {

    private final IUserService userService;

    private final UserRepository userRepository;

    private final AnalysisResultsRepository
            analysisResultsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MyPageStatDTO getStat(
            String email
    ) throws Exception {

        UserDTO user =
                userService.getUserByEmail(
                        email
                );

        long analysisCount =
                analysisResultsRepository
                        .countByUserId(
                                user.userId()
                        );

        List<AnalysisResultsDocument> list =
                analysisResultsRepository
                        .findByUserId(
                                user.userId()
                        );

        Map<String, Long> tagCountMap =
                list.stream()
                        .flatMap(doc ->
                                doc.getTags().stream()
                        )
                        .collect(
                                Collectors.groupingBy(
                                        TagResultDTO::tag,
                                        Collectors.counting()
                                )
                        );

        String mainTag =
                tagCountMap.entrySet()
                        .stream()
                        .max(
                                Map.Entry.comparingByValue()
                        )
                        .map(Map.Entry::getKey)
                        .orElse("");

        return MyPageStatDTO.builder()
                .analysisCount(analysisCount)
                .mainTag(mainTag)
                .build();
    }

    @Override
    public int updateNickname(
            String email,
            String nickname
    ) throws Exception {

        return userRepository.updateNicknameByEmail(
                email,
                nickname
        );
    }

    @Transactional
    @Override
    public int changePassword(
            String email,
            String currentPassword,
            String newPassword
    ) throws Exception {

        UserEntity user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        if (user == null) {
            return 0;
        }

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPassword()
        )) {
            return 0;
        }

        user.changePassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        userRepository.save(user);

        return 1;
    }

    private final RedisTokenService redisTokenService;

    @Override
    @Transactional
    public int withdraw(
            String email
    ) throws Exception {

        UserEntity user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        if (user == null) {
            return 0;
        }

        user.deactivate();

        redisTokenService.deleteRefreshToken(
                email
        );

        return 1;
    }

}
