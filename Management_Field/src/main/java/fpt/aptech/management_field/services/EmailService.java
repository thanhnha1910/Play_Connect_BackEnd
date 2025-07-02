package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates HTML content
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    public void sendOwnerApprovalEmail(User user) {
        String recipientAddress = user.getEmail();
        String subject = "Chúc mừng! Tài khoản chủ sân của bạn trên PlayerConnect đã được duyệt!";

        String body = String.format(
            "Chào %s,\n\n" +
            "Chúc mừng! Tài khoản chủ sân của bạn đã được đăng ký thành công trên hệ thống PlayerConnect.\n\n" +
            "Thông tin tài khoản:\n" +
            "- Tên đăng nhập: %s\n" +
            "- Email: %s\n" +
            "- Họ tên: %s\n\n" +
            "Bạn có thể đăng nhập vào hệ thống tại: http://localhost:3000/sign-in\n\n" +
            "Cảm ơn bạn đã tham gia cộng đồng PlayerConnect!\n\n" +
            "Trân trọng,\n" +
            "Đội ngũ PlayerConnect",
            user.getFullName(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName()
        );

        sendEmail(recipientAddress, subject, body);
    }
}