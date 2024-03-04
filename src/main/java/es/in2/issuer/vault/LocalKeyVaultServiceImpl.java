package es.in2.issuer.vault;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class LocalKeyVaultServiceImpl implements AzureKeyVaultService {

    private final Environment env;

    @Override
    public Mono<String> getSecretByKey(String key) {
        return Mono.just(env.getProperty(key, ""));
    }
}
