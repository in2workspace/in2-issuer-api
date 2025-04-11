package es.in2.issuer.oidc4vci.domain.model.dto;

import lombok.Builder;
import org.springframework.web.bind.annotation.BindParam;

@Builder
public record TokenRequest(
        @BindParam("grant_type") String grantType,
        @BindParam("pre-authorized_code") String preAuthorizedCode,
        @BindParam("tx_code") String txCode) {
}
