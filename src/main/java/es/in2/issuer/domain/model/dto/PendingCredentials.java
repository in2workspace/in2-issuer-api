package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.Map;

public record PendingCredentials(

        @JsonProperty("credentials") List<CredentialPayload> credentials
) {

    @Builder
    public record CredentialPayload(
            @JsonProperty("credential")
            Map<String, Object> credential
    ){}

}
