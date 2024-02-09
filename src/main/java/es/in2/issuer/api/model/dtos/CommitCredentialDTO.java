package es.in2.issuer.api.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CommitCredentialDTO {

    @JsonProperty("credential_id")
    private final UUID credentialId;

    @JsonProperty("gicar_id")
    private final String gicarId;

    @JsonProperty("expiration_date")
    private final Date expirationDate;
}
