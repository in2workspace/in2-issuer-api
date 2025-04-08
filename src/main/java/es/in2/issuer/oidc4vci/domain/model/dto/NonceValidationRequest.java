package es.in2.issuer.oidc4vci.domain.model.dto;

import org.springframework.web.bind.annotation.BindParam;

public record NonceValidationRequest(@BindParam("nonce") String nonce) {
}
