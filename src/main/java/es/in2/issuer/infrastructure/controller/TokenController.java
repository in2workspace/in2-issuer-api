package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import es.in2.issuer.domain.service.M2MTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @deprecated This class is obsolete and will be removed in future versions.
 * @since 1.0.0
 */
@Deprecated(since = "1.0.0", forRemoval = true)
@Slf4j
@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final M2MTokenService m2MTokenService;

    @PostMapping("/m2m")
    public Mono<VerifierOauth2AccessToken> getToken() {
        return m2MTokenService.getM2MToken();
    }

    @GetMapping("/verify")
    public Mono<Void> verify() {
        String token = "eyJraWQiOiJkaWQ6a2V5OnpEbmFla2l3a1djWG5IYVc2YXUzQnBtZldmcnRWVEpackEzRUhnTHZjYm02RVpudXAiLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJodHRwczovL3ZlcmlmaWVyLmRvbWUtbWFya2V0cGxhY2UtbGNsLm9yZyIsInN1YiI6ImRpZDprZXk6ekRuYWV6TGhTRldSWjF6cllRUndMeFY4bkRXbWIyUkVIb1VmN04zcXZuYTFURmluYSIsInNjb3BlIjoibWFjaGluZSBsZWFyY3JlZCIsImlzcyI6ImRpZDprZXk6ekRuYWVraXdrV2NYbkhhVzZhdTNCcG1mV2ZydFZUSlpyQTNFSGdMdmNibTZFWm51cCIsImV4cCI6MTczMDM4NzQ3MCwiaWF0IjoxNzI3Nzk1NDcwLCJqdGkiOiJhNzVjYjdlNy1kZDIyLTRmNDktOWI2NS0xZGNjNmE2NTZiNzMiLCJjbGllbnRfaWQiOiJkaWQ6a2V5OnpEbmFla2l3a1djWG5IYVc2YXUzQnBtZldmcnRWVEpackEzRUhnTHZjYm02RVpudXAiLCJ2ZXJpZmlhYmxlQ3JlZGVudGlhbCI6eyJjb250ZXh0IjpbImh0dHBzOi8vd3d3LnczLm9yZy9ucy9jcmVkZW50aWFscy92MiIsImh0dHBzOi8vd3d3LmV2aWRlbmNlbGVkZ2VyLmV1LzIwMjIvY3JlZGVudGlhbHMvbWFjaGluZS92MSJdLCJpZCI6IjhjN2E2MjEzLTU0NGQtNDUwZC04ZTNkLWI0MWZhOTAwOTE5OCIsInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJMRUFSQ3JlZGVudGlhbE1hY2hpbmUiXSwiaXNzdWVyIjp7ImlkIjoiZGlkOmVsc2k6VkFURVMtUTAwMDAwMDBKIn0sImlzc3VhbmNlRGF0ZSI6IjIwMjQtMDEtMDFUMDg6MDA6MDAuMDAwMDAwMDAwWiIsInZhbGlkRnJvbSI6IjIwMjQtMDEtMDFUMDg6MDA6MDAuMDAwMDAwMDAwWiIsImV4cGlyYXRpb25EYXRlIjoiMjAyNC0xMi0zMVQyMzo1OTowMC4wMDAwMDAwMDBaIiwiY3JlZGVudGlhbFN1YmplY3RMQ01hY2hpbmUiOnsibWFuZGF0ZUxDTWFjaGluZSI6eyJpZCI6IjdiZjU1ZDJlLTUyNDctNDcxNC05MWQxLThlMmY4Y2I3MzBkMSIsImxpZmVTcGFuIjp7ImVuZERhdGVUaW1lIjpudWxsLCJzdGFydERhdGVUaW1lIjpudWxsfSwibWFuZGF0ZWVMQ01hY2hpbmUiOnsiaWQiOiJkaWQ6a2V5OnpEbmFlekxoU0ZXUloxenJZUVJ3THhWOG5EV21iMlJFSG9VZjdOM3F2bmExVEZpbmEiLCJzZXJ2aWNlTmFtZSI6Iklzc3VlckFQSSIsInNlcnZpY2VUeXBlIjoiQVBJIFNlcnZlciIsInZlcnNpb24iOiJ2MS4wIiwiZG9tYWluIjoiaHR0cHM6Ly9pc3N1ZXIuZG9tZS1tYXJrZXRwbGFjZS5vcmciLCJpcEFkZHJlc3MiOiIxMjcuMC4wLjEiLCJkZXNjcmlwdGlvbiI6IkFQSSB0byBpc3N1ZSBWZXJpZmlhYmxlIENyZWRlbnRpYWxzIiwiY29udGFjdCI6eyJlbWFpbCI6ImRvbWVzdXBwb3J0QGluMi5lcyIsIm1vYmlsZVBob25lIjoiKzM0OTk5OTk5OTk5In19LCJtYW5kYXRvciI6eyJjb21tb25OYW1lIjoiNTY1NjU2NTZQIEplc3VzIFJ1aXoiLCJjb3VudHJ5IjoiRVMiLCJlbWFpbEFkZHJlc3MiOiJqZXN1cy5ydWl6QGluMi5lcyIsIm9yZ2FuaXphdGlvbiI6IklOMiwgSW5nZW5pZXLDrWEgZGUgbGEgSW5mb3JtYWNpw7NuLCBTLkwuIiwib3JnYW5pemF0aW9uSWRlbnRpZmllciI6IlZBVEVTLVEwMDAwMDAwSiIsInNlcmlhbE51bWJlciI6IklEQ0VTLTU2NTY1NjU2UCJ9LCJwb3dlckxDTWFjaGluZSI6W3siaWQiOiIxYTI2Njg2NS05Y2RhLTQyYzQtODg0Zi1iZDE4YTc5ZThiZmQiLCJkb21haW4iOiJET01FIiwiZnVuY3Rpb24iOiJMb2dpbiIsImFjdGlvbiI6Im9pZGNfbTJtIn0seyJpZCI6ImE0ZmQ0MmZlLWNlZGUtNGU0OC04MWExLTQ5NjZkZTIyMWM2MCIsImRvbWFpbiI6IkRPTUUiLCJmdW5jdGlvbiI6IkNlcnRpZmljYXRpb24iLCJhY3Rpb24iOiJwb3N0X3ZlcmlmaWFibGVfY2VydGlmaWNhdGlvbiJ9LHsiaWQiOiI3ZjUyNDg2Ny00YWJkLTRhOTUtYTY4NC0zZDJkM2E4ZTJiMmMiLCJkb21haW4iOiJET01FIiwiZnVuY3Rpb24iOiJJc3N1YW5jZSIsImFjdGlvbiI6Imlzc3VlX3ZjIn1dLCJzaWduZXIiOnsiY29tbW9uTmFtZSI6IjU2NTY1NjU2UCBKZXN1cyBSdWl6IiwiY291bnRyeSI6IkVTIiwiZW1haWxBZGRyZXNzIjoiamVzdXMucnVpekBpbjIuZXMiLCJvcmdhbml6YXRpb24iOiJJTjIsIEluZ2VuaWVyw61hIGRlIGxhIEluZm9ybWFjacOzbiwgUy5MLiIsIm9yZ2FuaXphdGlvbklkZW50aWZpZXIiOiJWQVRFUy1RMDAwMDAwMEoiLCJzZXJpYWxOdW1iZXIiOiJJRENFUy01NjU2NTY1NlAifX19fX0.XdfIEfW0sL_Xe-pW41LOkt9xBQiPuLhLC2DlUPBh9xWqxEFQ1xMlbPc2oVl8u4PJRwpwHpUQ7G0nDF0OvJMT9w";
        return m2MTokenService.verifyM2MToken(token);
    }


    @PostMapping
    public Mono<Object> handleData(ServerWebExchange exchange) {
        Mono<MultiValueMap<String, String>> formDataMono = exchange.getFormData();

        log.info("Get formDataMono. [Exchange:{}]", exchange);

        return formDataMono.flatMap(formData -> {
            log.info("\n==================================\n");
            log.info("[TokenFormData:{}]", formData);
            log.info("\n==================================\n");

            var client = WebClient.builder()
                    .baseUrl("http://dome-issuer-keycloak:8080")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .build();

            return client.post()
                    .uri("issuer-keycloak/realms/CredentialIssuer/verifiable-credential/did:key:z6MkqmaCT2JqdUtLeKah7tEVfNXtDXtQyj4yxEgV11Y5CqUa/token")
                    .body(BodyInserters.fromFormData("grant_type", Objects.requireNonNull(formData.getFirst("grant_type")))
                            .with("pre-authorized_code", Objects.requireNonNull(formData.getFirst("pre-authorized_code")))
                            .with("tx_code", Objects.requireNonNull(formData.getFirst("tx_code"))))
                    .retrieve()
                    .bodyToMono(Object.class)
                    .onErrorResume(error -> {
                        log.info("\n==================================\n");
                        log.error("[WebClientException:{}]", error.getMessage());
                        log.info("\n==================================\n");

                        return Mono.error(error);
                    });
        });
    }
}
