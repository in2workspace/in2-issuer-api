package es.in2.issuer.domain.model.dto.credential;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface Issuer {

    @JsonProperty("id")
    String getId();
}

