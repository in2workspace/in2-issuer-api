package es.in2.issuer.domain.model.dto.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;

@Builder
public record SimpleIssuer(@JsonProperty("id") String id) implements Issuer {

    @JsonValue
    public String toJson() {
        return id;
    }

    @Override
    public String getId() {
        return id;
    }
}
