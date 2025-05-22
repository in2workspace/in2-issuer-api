package es.in2.issuer.backend.backoffice.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record CredentialOfferUriResponse(
        @JsonProperty("credential_offer_uri") String credentialOfferUri,
        // TODO: També al Front -> activation_code
        @JsonProperty("c_transaction_code") String cTransactionCode,
        // TODO: També al Front -> activation_code_expires_in
        @JsonProperty("c_transaction_code_expires_in") int cTransactionCodeExpiresIn
) {
}
