package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.SignedDataParsingException;
import es.in2.issuer.domain.model.SignatureRequest;
import es.in2.issuer.domain.model.SignedData;
import es.in2.issuer.domain.service.RemoteSignatureService;
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

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteSignatureServiceImpl implements RemoteSignatureService {

    // fixme: debe ir en un archivo Properties
    @Value("${remote-signature.routes.sign}")
    private String sign;

    private final AppConfiguration appConfiguration;
    private final ObjectMapper objectMapper;
    private final HttpUtils httpUtils;

    // todo: delete remoteSignatureBaseUrl
    private String remoteSignatureBaseUrl;
    @PostConstruct
    private void initializeRemoteSignatureBaseUrl() {
        remoteSignatureBaseUrl = appConfiguration.getRemoteSignatureDomain();
    }

    @Override
    public Mono<SignedData> sign(SignatureRequest signatureRequest, String token) {
        return getSignedSignature(signatureRequest, token)
                .flatMap(response -> {
                    try {
                        return Mono.just(toSignedData(response));
                    } catch (SignedDataParsingException ex) {
                        return Mono.error(ex);
                    }
                })
                .doOnSuccess(result -> log.info("Signature signed!"))
                .doOnError(throwable -> {
                    if (throwable instanceof SignedDataParsingException) {
                        log.error("Error parsing signed data: {}", throwable.getMessage());
                    } else {
                        log.error("Error: {}", throwable.getMessage());
                    }
                });
    }

    private Mono<String> getSignedSignature(SignatureRequest signatureRequest, String token) {
        String signatureRemoteServerEndpoint = remoteSignatureBaseUrl + "/api/v1" + sign;
        String signatureRequestJSON;
        try {
            signatureRequestJSON = objectMapper.writeValueAsString(signatureRequest);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        // todo: refactorizar: debe implementarse la llamada aquí y no en el Utils
        return httpUtils.postRequest(signatureRemoteServerEndpoint, headers, signatureRequestJSON);

    }

    private SignedData toSignedData(String signedSignatureResponse) throws SignedDataParsingException {
        try {
            return objectMapper.readValue(signedSignatureResponse, SignedData.class);
        } catch (IOException e) {
            log.error("Error: {}", e.getMessage());
            throw new SignedDataParsingException("Error parsing signed data");
        }
    }

}
