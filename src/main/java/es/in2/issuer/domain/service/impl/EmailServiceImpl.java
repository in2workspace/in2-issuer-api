package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

import static es.in2.issuer.domain.util.Constants.FROM_EMAIL;
import static es.in2.issuer.domain.util.Constants.UTF_8;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Override
    public Mono<Void> sendPin(String to, String subject, String pin) {
        return Mono.fromCallable(() -> {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8);
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("pin", pin);
            String htmlContent = templateEngine.process("pin-email", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> sendTransactionCodeForCredentialOffer(String to, String subject, String link, String knowledgebaseUrl, String user, String organization) {
        log.info("EmailServiceImpl --> sendTransactionCodeForCredentialOffer() --> INIT");
        return Mono.fromCallable(() -> {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8);
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();

            context.setVariable("link", link);
            context.setVariable("user", user);
            context.setVariable("organization", organization);
            context.setVariable("knowledgebaseUrl", knowledgebaseUrl);
            context.setVariable("qrImage", getEncodedImageIntoBase64());

            String htmlContent = templateEngine.process("activate-credential-email", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);

            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> sendPendingCredentialNotification(String to, String subject) {
        return Mono.fromCallable(() -> {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8);
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            String htmlContent = templateEngine.process("credential-pending-notification", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> sendCredentialSignedNotification(String to, String subject, String firstName) {
        firstName = firstName.replace("\"", "");
        final String finalName = firstName;

        return Mono.fromCallable(() -> {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("name", finalName);
            String htmlContent = templateEngine.process("credential-signed-notification", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private String getEncodedImageIntoBase64() {
        File file = new File("static/img/qr-wallet.png");
        FileInputStream imageFile = null;
        String encodedImage = null;

        try {
            imageFile = new FileInputStream(file);
            byte[] imageData = new byte[(int) file.length()];
            encodedImage = Base64.getEncoder().encodeToString(imageData);

        } catch (IOException e) {
            log.error("Error while reading image from file", e);
        } finally {
            try {
                if (imageFile != null) {
                    imageFile.close();
                }
            } catch (IOException e) {
                log.error("Error while closing image file", e);
            }
        }
        return encodedImage;
    }
}
