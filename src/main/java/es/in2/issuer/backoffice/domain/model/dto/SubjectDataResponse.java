package es.in2.issuer.backoffice.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Map;

@Builder
public record SubjectDataResponse(
    @Schema(
            example = """
              {
                "LEARCredential": {
                "id": "did:key:zQ3shg2Mqz6NBj3afSySic9ynMrGk5Vgo9atHLXj4NWgxd7Xh",
                "first_name": "Francisco",
                "last_name": "Pérez García",
                "email": "francisco.perez@in2.es",
                "serialnumber": "IDCES-46521781J",
                "employeeType": "T2",
                "organizational_unit": "GDI010034",
                "organization": "GDI01"
                }
              }
        """,
            description = "The user's credential data"
    ) @JsonProperty("credentialSubjectData") Map<String, Map<String, String>> credentialSubjectData) {
}
