package es.in2.issuer.api.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GenericResponseError {

    @Schema(
            description = "Generic response when an unexpected error occurs"
    )
    @JsonProperty("timestamp")
    private final LocalDateTime timestamp;

    @JsonProperty("status")
    private final int status;

    @JsonProperty("error")
    private final String error;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("path")
    private final String path;
}
