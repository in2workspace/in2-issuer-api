package es.in2.issuer.domain.model;

import lombok.Builder;

@Builder
public record VcTemplate(Boolean mutable, String name, String template) {
}
