package es.in2.issuer.domain.service;

import es.in2.issuer.domain.service.impl.EmailServiceImpl;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void testSendPin() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("pin-email"), any(Context.class))).thenReturn("htmlContent");

        Mono<Void> result = emailService.sendPin("to@example.com", "subject", "1234");

        StepVerifier.create(result)
                .verifyComplete();

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendTransactionCodeForCredentialOffer() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("transaction-code-email"), any(Context.class))).thenReturn("htmlContent");

        Mono<Void> result = emailService.sendTransactionCodeForCredentialOffer("to@example.com", "subject", "link", "\"John\"","walletUrl");

        StepVerifier.create(result)
                .verifyComplete();

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendPendingCredentialNotification() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("credential-pending-notification"), any(Context.class))).thenReturn("htmlContent");

        Mono<Void> result = emailService.sendPendingCredentialNotification("to@example.com", "subject");

        StepVerifier.create(result)
                .verifyComplete();

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendCredentialSignedNotification() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("credential-signed-notification"), any(Context.class))).thenReturn("htmlContent");

        Mono<Void> result = emailService.sendCredentialSignedNotification("to@example.com", "subject", "\"John\"");

        StepVerifier.create(result)
                .verifyComplete();

        verify(javaMailSender).send(mimeMessage);
    }
}