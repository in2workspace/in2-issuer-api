package es.in2.issuer.backoffice.domain.model.dto.credential.lear;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record LifeSpan(
        @JsonProperty("end_date_time") String endDateTime,
        @JsonProperty("start_date_time") String startDateTime
) {}

