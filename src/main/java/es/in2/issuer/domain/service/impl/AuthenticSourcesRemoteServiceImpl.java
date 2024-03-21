package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.AuthenticSourcesUserParsingException;
import es.in2.issuer.domain.model.IDEPCommitCredential;
import es.in2.issuer.domain.model.SubjectDataResponse;
import es.in2.issuer.domain.service.AuthenticSourcesRemoteService;
import es.in2.issuer.domain.util.HttpUtils;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.config.properties.AuthenticSourcesProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AuthenticSourcesRemoteServiceImpl implements AuthenticSourcesRemoteService {

    private final String apiUsers;
    private final ObjectMapper objectMapper;
    private final HttpUtils httpUtils;
    @Value("classpath:credentials/LEARCredentialSubjectDataDemo.json")
    private Resource userLocalFileData;

    // todo: delete authenticSourcesBaseUrl
    private String authenticSourcesBaseUrl;

    public AuthenticSourcesRemoteServiceImpl(AuthenticSourcesProperties authenticSourcesProperties, AppConfiguration appConfiguration, ObjectMapper objectMapper, HttpUtils httpUtils) {
        this.authenticSourcesBaseUrl = appConfiguration.getAuthenticSourcesDomain();
        this.apiUsers = authenticSourcesProperties.getUser();
        this.objectMapper = objectMapper;
        this.httpUtils = httpUtils;

    }

    @Override
    public Mono<SubjectDataResponse> getUser(String token) {
        return getUserFromAuthenticSourceServer(token).flatMap(authenticSourcesResponse -> {
            try {
                return Mono.just(toAuthenticSourcesUserResponseDTO(authenticSourcesResponse));
            } catch (JsonProcessingException | AuthenticSourcesUserParsingException e) {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<SubjectDataResponse> getUserFromLocalFile() {
        return Mono.fromCallable(() -> {
                    // Read the content of the file
                    String jsonContent = new String(userLocalFileData.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    // Convert JSON content to AuthenticSourcesGetUserResponseDTO object
                    return objectMapper.readValue(jsonContent, SubjectDataResponse.class);
                })
                .doOnSuccess(result -> log.info("Successfully parsed user data from local file."))
                .onErrorMap(Exception.class, e -> new RuntimeException("Failed to parse user data from local file.", e));
    }

    @Override
    public Mono<Void> commitCredentialSourceData(IDEPCommitCredential idepCommitCredential, String token) {
        return Mono.defer(() -> {
            List<Map.Entry<String, String>> headers = prepareHeadersWithAuth(token);
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
        List<Map.Entry<String, String>> headers = prepareHeadersWithAuth(token);
        String authenticSourceUserApiEndpoint = authenticSourcesBaseUrl + apiUsers;
        // todo: refactorizar: debe implementarse la llamada aquí y no en el Utils
        return httpUtils.getRequest(authenticSourceUserApiEndpoint, headers);
    }

    private List<Map.Entry<String, String>> prepareHeadersWithAuth(String token) {
        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        return headers;
    }

    private SubjectDataResponse toAuthenticSourcesUserResponseDTO(String value) throws JsonProcessingException, AuthenticSourcesUserParsingException {
        try {
            return objectMapper.readValue(value, SubjectDataResponse.class);
        } catch (JsonProcessingException e) {
            log.warn("JsonProcessingException --> {}", e.getMessage());
            throw new AuthenticSourcesUserParsingException("Error parsing user data");
        }
    }

}
