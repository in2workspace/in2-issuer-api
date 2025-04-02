package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GlobalErrorMessage(
        @Schema(description = "Generic response when an unexpected error occurs") @JsonProperty("timestamp") LocalDateTime timestamp,
        @JsonProperty("status") int status, @JsonProperty("error") String error,
        @JsonProperty("message") String message, @JsonProperty("path") String path) {
}
