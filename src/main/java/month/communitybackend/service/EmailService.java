package month.communitybackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 텍스트 형태의 메세지 구성
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
    // 메세지 내용
    public void sendPasswordResetEmail(String userEmail, String token) {
        String subject = "비밀번호 재설정 링크입니다.";
        String resetUrl = "http://localhost:3000/reset-password?token=" + token; // 프론트엔드 URL
        String text = "비밀번호를 재설정하려면 다음 링크를 클릭하세요: " + resetUrl +
                      "\n\n이 링크는 1시간 동안 유효합니다.";
        sendEmail(userEmail, subject, text);
    }
}