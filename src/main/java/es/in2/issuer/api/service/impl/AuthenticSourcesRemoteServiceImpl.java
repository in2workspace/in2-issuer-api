package es.in2.issuer.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.api.config.provider.ConfigProvider;
import es.in2.issuer.api.model.dto.AuthenticSourcesGetUserResponseDTO;
import es.in2.issuer.api.model.dto.CommitCredentialDTO;
import es.in2.issuer.api.exception.UserDoesNotExistException;
import es.in2.issuer.api.service.AuthenticSourcesRemoteService;
import es.in2.issuer.api.util.HttpUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticSourcesRemoteServiceImpl implements AuthenticSourcesRemoteService {

    @Value("${authentic-sources.routes.get-user}")
    private String apiUsers;

    private final ConfigProvider configProvider;
    private final ObjectMapper objectMapper;
    private final HttpUtils httpUtils;

    private String authenticSourcesBaseUrl;
    @PostConstruct
    private void initializeAuthenticSourcesBaseUrl() {
        authenticSourcesBaseUrl = configProvider.getAuthenticSourcesDomain();
    }

    @Override
    public Mono<AuthenticSourcesGetUserResponseDTO> getUser(String token) {
        return Mono.defer(() ->
                getUserFromAuthenticSourceServer(token)
                        .flatMap(authenticSourcesServerResponse ->
                                Mono.fromCallable(() -> toAuthenticSourcesUserResponseDTO(authenticSourcesServerResponse))
                                        .doOnSuccess(result -> log.info("User token: {}", token))
                                        .onErrorMap(NoSuchElementException.class, e -> new UserDoesNotExistException(token))
                                        .onErrorMap(Exception.class, e -> new RuntimeException(e.getMessage(), e))
                        )
        );
    }

    @Override
    public Mono<Void> commitCredentialSourceData(CommitCredentialDTO commitCredentialDTO, String token) {
        return Mono.defer(() -> {
            List<Map.Entry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
            headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            String authenticSourceUserApiEndpoint = authenticSourcesBaseUrl + apiUsers;
            String commitCredentialJsonString;

            try {
                commitCredentialJsonString = objectMapper.writeValueAsString(commitCredentialDTO);
            } catch (JsonProcessingException e) {
                return Mono.error(e);
            }

            return httpUtils.postRequest(authenticSourceUserApiEndpoint, headers, commitCredentialJsonString)
                    .then();
        });
    }

    private Mono<String> getUserFromAuthenticSourceServer(String token) {

        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));

        String authenticSourceUserApiEndpoint = authenticSourcesBaseUrl + apiUsers;

        return httpUtils.getRequest(authenticSourceUserApiEndpoint, headers);
    }

    private AuthenticSourcesGetUserResponseDTO toAuthenticSourcesUserResponseDTO(String value){
        try {
            return objectMapper.readValue(value, AuthenticSourcesGetUserResponseDTO.class);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException --> {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
