package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record SimpleIssuer(@JsonProperty("id") String id) implements Issuer {
    @Override
    public String getId() {
        return id;
    }
}
