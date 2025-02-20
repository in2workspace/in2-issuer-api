package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;

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
        return Mono.fromCallable(() -> {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8);
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);

            ClassPathResource imgResource = new ClassPathResource("static/images/qr-wallet.png");
            String imageResourceName = imgResource.getFilename();

            InputStream imageStream = imgResource.getInputStream();
            byte[] imageBytes = StreamUtils.copyToByteArray(imageStream);

            Context context = new Context();
            context.setVariable("link", link);
            context.setVariable("user", user);
            context.setVariable("organization", organization);
            context.setVariable("knowledgebaseUrl", knowledgebaseUrl);
            context.setVariable("imageResourceName", "cid:" + imageResourceName);

            String htmlContent = templateEngine.process("activate-credential-email", context);
            helper.setText(htmlContent, true);

            final InputStreamSource imageSource = new ByteArrayResource(imageBytes);
            if (imageResourceName != null) {
                helper.addInline(imageResourceName, imageSource, MimeTypeUtils.IMAGE_PNG_VALUE);
            }

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
    public Mono<Void> sendPendingSignatureCredentialNotification(String to, String subject, String id, String domain){
        return Mono.fromCallable(() -> {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8);
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("id", id);
            context.setVariable("domain", domain);
            String htmlContent = templateEngine.process("credential-pending-signature-notification", context);
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

    @Override
    public Mono<Void> sendResponseUriFailed(String to, String productId, String guideUrl) {
        return Mono.fromCallable(() -> {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8);
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject("Certification Submission to Marketplace Unsuccessful");

            Context context = new Context();
            context.setVariable("productId", productId);
            context.setVariable("guideUrl", guideUrl);
            String htmlContent = templateEngine.process("response-uri-failed", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> sendResponseUriAcceptedWithHtml(String to, String productId, String htmlContent) {
        return Mono.fromCallable(() -> {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8);
            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject("Missing Documents for Certification: " + productId);

            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
