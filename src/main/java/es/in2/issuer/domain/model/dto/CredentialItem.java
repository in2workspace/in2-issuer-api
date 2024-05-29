package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

@Builder
public record CredentialItem(
        @JsonProperty("procedure_id") UUID procedureId,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("status") String status,
        @JsonProperty("updated") Timestamp updated
) {
}
