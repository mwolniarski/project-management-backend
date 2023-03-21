package pl.wolniarskim.project_management.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;


@Service
@AllArgsConstructor
public class EmailService{

    private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;

    public void sendConfirmation(String to, String token){
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMultipart mimeMultipart = new MimeMultipart();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(to);
            helper.setSubject("Confirm your email");
            helper.setFrom("no-reply@instacloud.biz");
            //
            MimeBodyPart imagePart = new MimeBodyPart();
            imagePart.setHeader("Content-ID", "<instacloud_logo>");
            imagePart.setDisposition(MimeBodyPart.INLINE);
            imagePart.attachFile("src/main/resources/emails/images/instacloud_logo.png");
            //
            MimeBodyPart content = new MimeBodyPart();
            content.setContent(confirmationEmailTemplate(token), "text/html");
            //
            mimeMultipart.addBodyPart(imagePart);
            mimeMultipart.addBodyPart(content);
            //
            mimeMessage.setContent(mimeMultipart);
            mailSender.send(mimeMessage);
        } catch(MessagingException e){
            LOGGER.error("Failed to send email: " +  e);
            throw new IllegalStateException("Failed to send email");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String confirmationEmailTemplate(String token){
        String template = emailTemplateService.getConfirmationEmailTemplate()
                .replaceAll("\\{link\\}", "http://localhost:8080/api/registration/confirm?token=" + token);
        return template;
    }
}
