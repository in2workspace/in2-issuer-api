package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
            log.info("EmailServiceImpl --> sendTransactionCodeForCredentialOffer() --> link = " + link);
            context.setVariable("user", user);
            log.info("EmailServiceImpl --> sendTransactionCodeForCredentialOffer() --> user: " + user);
            context.setVariable("organization", organization);
            log.info("EmailServiceImpl --> sendTransactionCodeForCredentialOffer() --> organization: " + organization);
            context.setVariable("knowledgebaseUrl", knowledgebaseUrl);
            log.info("EmailServiceImpl --> sendTransactionCodeForCredentialOffer() --> knowledgebaseUrl: " + knowledgebaseUrl);
            String htmlContent = templateEngine.process("activate-credential-email", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            log.info("EmailServiceImpl --> sendTransactionCodeForCredentialOffer() --> EMAIL SENT");

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

            // Crear el contexto sin variables adicionales
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
}
