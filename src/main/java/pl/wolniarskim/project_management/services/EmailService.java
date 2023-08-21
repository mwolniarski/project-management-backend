package pl.wolniarskim.project_management.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


@Service
@RequiredArgsConstructor
public class EmailService{

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    @Value("${spring.mail.from}")
    private String from;
    @Value("${confirmation.frontend.url}")
    private String confirmationFrontendUrl;
    @Value("${reset.password.frontend.url}")
    private String resetPasswordFrontendUrl;

    @Value("${enable-email-confirmation}")
    private boolean enableEmailConfirmation;

    private final EmailTemplateService emailTemplateService;

    public void sendConfirmation(String to, String token){
        if(enableEmailConfirmation){
            try{
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMultipart mimeMultipart = new MimeMultipart();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
                helper.setTo(to);
                helper.setSubject("Confirm your email");
                helper.setFrom(from);
                MimeBodyPart content = new MimeBodyPart();
                content.setContent(confirmationEmailTemplate(token), "text/html");
                mimeMultipart.addBodyPart(content);
                mimeMessage.setContent(mimeMultipart);
                mailSender.send(mimeMessage);
            } catch(MessagingException e){
                LOGGER.error("Failed to send email: " +  e);
                throw new IllegalStateException("Failed to send email");
            }
        }
    }

    public void sendResetPassword(String to, String token){
        if(enableEmailConfirmation) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMultipart mimeMultipart = new MimeMultipart();

                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
                helper.setTo(to);
                helper.setSubject("Reset password");
                helper.setFrom(from);
                MimeBodyPart content = new MimeBodyPart();
                content.setContent(resetPasswordEmailTemplate(token), "text/html");
                mimeMultipart.addBodyPart(content);
                mimeMessage.setContent(mimeMultipart);
                mailSender.send(mimeMessage);
            } catch (MessagingException e) {
                LOGGER.error("Failed to send email: " + e);
                throw new IllegalStateException("Failed to send email");
            }
        }
    }


    private String confirmationEmailTemplate(String token){
        String template = emailTemplateService.getConfirmationEmailTemplate()
                .replaceAll("\\{link\\}", confirmationFrontendUrl + token);
        return template;
    }

    private String resetPasswordEmailTemplate(String token){
        String template = emailTemplateService.getResetPasswordTemplate()
                .replaceAll("\\{link\\}", resetPasswordFrontendUrl + token);
        return template;
    }
}
