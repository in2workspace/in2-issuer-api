package es.in2.issuer.api.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CredentialResponseError {

    @JsonProperty("error")
    private final String error;

    @JsonProperty("description")
    private final String description;
}
