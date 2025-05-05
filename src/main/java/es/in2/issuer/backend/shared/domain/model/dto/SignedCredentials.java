package es.in2.issuer.backend.shared.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
@Builder
public record SignedCredentials(
        @JsonProperty("credentials") List<SignedCredential> credentials
) {

    @Builder
    public record SignedCredential(
            @JsonProperty("credential")
            String credential
    ){}

}
