package kopo.poly.artlensai.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    @Value("${spring.mail.from}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    // 🔥 인증번호 저장
    private final Map<String, String> codeStore = new ConcurrentHashMap<>();

    // 🔥 인증 완료 여부 저장
    private final Map<String, Boolean> verifiedMap = new ConcurrentHashMap<>();

    private final Map<String, String> resetTokenMap = new ConcurrentHashMap<>();

    public void sendCode(String email) {

        log.info("sendCode Start! email={}", email);

        String code = String.valueOf((int)(Math.random() * 900000) + 100000);

        codeStore.put(email, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(fromEmail);
        message.setSubject("ArtLens 인증번호");
        message.setText("인증번호: " + code);

        log.info("{}.message : ", message);

        mailSender.send(message);
    }

    public boolean verifyCode(String email, String code) {

        log.info("verifyCode Start!");

        String saved = codeStore.get(email);

        boolean result = saved != null && saved.equals(code);

        if (result) {
            verifiedMap.put(email, true); // 🔥 인증 완료 저장
            codeStore.remove(email); // 보안용 삭제
        }

        log.info("verifyCode End! result={}", result);

        return result;
    }

    public boolean isVerified(String email) {
        return verifiedMap.getOrDefault(email, false);
    }

    public void saveResetToken(String email, String token) {
        resetTokenMap.put(email, token);
    }

    public void sendResetEmail(String email, String link) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(fromEmail);
        message.setSubject("ArtLens ai 비밀번호 재설정");
        message.setText("아래 링크로 비밀번호를 변경하세요:\n" + link);

        mailSender.send(message);
    }

    public String getEmailByToken(String token) {
        return resetTokenMap.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(token))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public void removeToken(String token) {
        resetTokenMap.values().remove(token);
    }
}