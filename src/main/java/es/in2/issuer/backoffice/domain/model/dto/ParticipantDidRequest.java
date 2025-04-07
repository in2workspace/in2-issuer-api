package es.in2.issuer.backoffice.domain.model.dto;

import lombok.Builder;

import java.util.List;
@Builder
public record ParticipantDidRequest(
        String did,
        List<Credential> credentials
) {
    public record Credential(
            String credentialsType,
            List<String> claims
    ) {}
}
