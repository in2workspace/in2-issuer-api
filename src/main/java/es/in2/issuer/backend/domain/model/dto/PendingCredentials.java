package es.in2.issuer.backend.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

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
