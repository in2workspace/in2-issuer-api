package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record CredentialOfferUriResponse(
        @JsonProperty("credential_offer_uri")
        String credentialOfferUri,
        @JsonProperty("c_transaction_code")
        String cTransactionCode,
        @JsonProperty("c_transaction_code_expires_in")
        int cTransactionCodeExpiresIn
) {
}
