package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static es.in2.issuer.domain.util.Constants.NO_REPLAY_EMAIL;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    @Override
    public Mono<Void> sendPin(String to, String subject, String pin) {
        return Mono.fromCallable(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(NO_REPLAY_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(pin);
            javaMailSender.send(message);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
