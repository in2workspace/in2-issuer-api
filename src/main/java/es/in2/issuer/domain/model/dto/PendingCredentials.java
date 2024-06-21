package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record PendingCredentials(

        @JsonProperty("credentials") List<CredentialPayload> credentials
) {

    @Builder
    public record CredentialPayload(
            @JsonProperty("credential")
            String credential
    ){}

}
