package es.in2.issuer.domain.model.dto;

import lombok.Builder;

@Builder
public record VcTemplate(Boolean mutable, String name, String template) {
}
