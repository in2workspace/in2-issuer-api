package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface Issuer {

    @JsonProperty("id")
    String getId();

    default String getIssuerId() {
        return getId();
    }
}

