package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.IDEPCommitCredential;
import es.in2.issuer.domain.model.SubjectDataResponse;
import es.in2.issuer.domain.service.AuthenticSourcesRemoteService;
import es.in2.issuer.domain.util.HttpUtils;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticSourcesRemoteServiceImpl implements AuthenticSourcesRemoteService {

    // fixme: debe ir en un archivo Properties
    @Value("${authentic-sources.routes.get-user}")
    private String apiUsers;

    private final AppConfiguration appConfiguration;
    private final ObjectMapper objectMapper;
    private final HttpUtils httpUtils;

    // todo: delete authenticSourcesBaseUrl
    private String authenticSourcesBaseUrl;

    @PostConstruct
    private void initializeAuthenticSourcesBaseUrl() {
        authenticSourcesBaseUrl = appConfiguration.getAuthenticSourcesDomain();
    }

    @Override
    public Mono<SubjectDataResponse> getUser(String token) {
        return getUserFromAuthenticSourceServer(token).flatMap(authenticSourcesResponse -> {
            try {
                return Mono.just(toAuthenticSourcesUserResponseDTO(authenticSourcesResponse));
            } catch (JsonProcessingException e) {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<Void> commitCredentialSourceData(IDEPCommitCredential idepCommitCredential, String token) {
        return Mono.defer(() -> {
            // fixme: refactorizar - código duplicado
            List<Map.Entry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
            headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
            String authenticSourceUserApiEndpoint = authenticSourcesBaseUrl + apiUsers;
            String commitCredentialJsonString;
            try {
                commitCredentialJsonString = objectMapper.writeValueAsString(idepCommitCredential);
            } catch (JsonProcessingException e) {
                return Mono.error(e);
            }
            // todo: refactorizar: debe implementarse la llamada aquí y no en el Utils
            return httpUtils.postRequest(authenticSourceUserApiEndpoint, headers, commitCredentialJsonString)
                    .then();
        });
    }

    private Mono<String> getUserFromAuthenticSourceServer(String token) {
        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        String authenticSourceUserApiEndpoint = authenticSourcesBaseUrl + apiUsers;
        // todo: refactorizar: debe implementarse la llamada aquí y no en el Utils
        return httpUtils.getRequest(authenticSourceUserApiEndpoint, headers);
    }

    private SubjectDataResponse toAuthenticSourcesUserResponseDTO(String value) throws JsonProcessingException {
        try {
            return objectMapper.readValue(value, SubjectDataResponse.class);
        } catch (JsonProcessingException e) {
            // todo: este log es de tipo warn y el que se atrapa en el GlobalAdvice de tipo error
            log.error("JsonProcessingException --> {}", e.getMessage());
            // todo: añadir custom exception
            throw e;
        }
    }

}
