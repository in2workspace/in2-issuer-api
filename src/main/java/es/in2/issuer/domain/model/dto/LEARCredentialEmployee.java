package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record LEARCredentialEmployee(
        @JsonProperty("@context") List<String> context,
        @JsonProperty("id") String id,
        @JsonProperty("type") List<String> type,
        @JsonProperty("credentialSubject") CredentialSubject credentialSubject,
        @JsonProperty("issuer") String issuer,
        @JsonProperty("validFrom") String validFrom,
        @JsonProperty("validUntil") String validUntil
) {

    @Builder
    public record CredentialSubject(@JsonProperty("mandate") Mandate mandate) {

        @Builder
        public record Mandate(
                @JsonProperty("id") String id,
                @JsonProperty("life_span") LifeSpan lifeSpan,
                @JsonProperty("mandatee") Mandatee mandatee,
                @JsonProperty("mandator") Mandator mandator,
                @JsonProperty("power") List<Power> power,
                @JsonProperty("signer") Signer signer
        ) {

            @Builder
            public record LifeSpan(
                    @JsonProperty("end_date_time") String endDateTime,
                    @JsonProperty("start_date_time") String startDateTime
            ) {
            }

            @Builder
            public record Mandatee(
                    @JsonProperty("id") String id,
                    @JsonProperty("email") String email,
                    @JsonProperty("first_name") String firstName,
                    @JsonProperty("last_name") String lastName,
                    @JsonProperty("mobile_phone") String mobilePhone
            ) {
            }

            @Builder
            public record Mandator(
                    @JsonProperty("commonName") String commonName,
                    @JsonProperty("country") String country,
                    @JsonProperty("emailAddress") String emailAddress,
                    @JsonProperty("organization") String organization,
                    @JsonProperty("organizationIdentifier") String organizationIdentifier,
                    @JsonProperty("serialNumber") String serialNumber
            ) {
            }

            @Builder
            public record Power(
                    @JsonProperty("id") String id,
                    @JsonProperty("tmf_action") Object tmfAction,
                    @JsonProperty("tmf_domain") String tmfDomain,
                    @JsonProperty("tmf_function") String tmfFunction,
                    @JsonProperty("tmf_type") String tmfType
            ) {
            }

            @Builder
            public record Signer(
                    @JsonProperty("commonName") String commonName,
                    @JsonProperty("country") String country,
                    @JsonProperty("emailAddress") String emailAddress,
                    @JsonProperty("organization") String organization,
                    @JsonProperty("organizationIdentifier") String organizationIdentifier,
                    @JsonProperty("serialNumber") String serialNumber
            ) {
            }

        }
    }
}
