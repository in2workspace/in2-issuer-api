package es.in2.issuer.backend.shared.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record CredentialProcedures(

        @JsonProperty("credential_procedures") List<CredentialProcedures.CredentialProcedure> credentialProcedures
) {

    @Builder
    public record CredentialProcedure(
            @JsonProperty("credential_procedure")
            ProcedureBasicInfo credentialProcedure
    ){}

}