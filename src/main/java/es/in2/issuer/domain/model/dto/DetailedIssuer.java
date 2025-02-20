package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;


@Builder
public record DetailedIssuer(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("organizationIdentifier") String organizationIdentifier,
        @JsonProperty("organization") String organization,
        @JsonProperty("country") String country,
        @JsonProperty("commonName") String commonName,
        @JsonProperty("emailAddress") String emailAddress,
        @JsonProperty("serialNumber") String serialNumber,
        @JsonProperty("description") String description
) implements Issuer {
    @Override
    public String getId() {
        return id;
    }
}

