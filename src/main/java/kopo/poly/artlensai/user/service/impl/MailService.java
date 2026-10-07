package kopo.poly.artlensai.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    /**
     * 발신 이메일 주소
     */
    @Value("${spring.mail.from}")
    private String fromEmail;

    /**
     * Spring Mail Sender
     */
    private final JavaMailSender mailSender;

    /**
     * 인증번호 메일 발송
     *
     * @param email 수신 이메일
     * @param code  인증번호
     */
    public void sendVerificationCode(
            String email,
            String code
    ) {

        log.info("sendVerificationCode Start! email={}", email);

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);
        message.setFrom(fromEmail);
        message.setSubject("ArtLens 인증번호");
        message.setText(
                """
                인증번호는 아래와 같습니다.

                %s

                인증번호 입력 후 회원가입을 진행해주세요.
                """.formatted(code)
        );

        mailSender.send(message);

        log.info("sendVerificationCode End!");
    }

    /**
     * 비밀번호 재설정 메일 발송
     *
     * @param email     수신 이메일
     * @param resetLink 비밀번호 변경 링크
     */
    public void sendPasswordResetMail(
            String email,
            String resetLink
    ) {

        log.info("sendPasswordResetMail Start! email={}", email);

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);
        message.setFrom(fromEmail);
        message.setSubject("ArtLensAI 비밀번호 재설정");

        message.setText(
                """
                비밀번호 재설정을 요청하셨습니다.

                아래 링크를 클릭하여 비밀번호를 변경해주세요.

                %s

                본 링크는 10분 동안만 유효합니다.

                요청하지 않았다면 본 메일을 무시해주세요.
                """.formatted(resetLink)
        );

        mailSender.send(message);

        log.info("sendPasswordResetMail End!");
    }
}