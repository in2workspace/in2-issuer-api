package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record LEARCredentialEmployee(
        @JsonProperty("@context") List<String> context,
        @JsonProperty("id") String id,
        @JsonProperty("type") List<String> type,
        @JsonProperty("description") String description,
        @JsonProperty("credentialSubject") CredentialSubject credentialSubject,
        @JsonProperty("issuer") Issuer issuer,
        @JsonProperty("validFrom") String validFrom,
        @JsonProperty("validUntil") String validUntil
) implements W3CVerifiableCredential<LEARCredentialEmployee.CredentialSubject> {

    @Override
    public List<String> getContext() {
        return context;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<String> getType() {
        return type;
    }

    @Override
    public String getDescription() {
        return description;
    }
    @Override
    public Issuer getIssuer() {
        return issuer;
    }

    @Override
    public String getValidFrom() {
        return validFrom;
    }

    @Override
    public String getValidUntil() {
        return validUntil;
    }

    @Override
    public CredentialSubject getCredentialSubject() {
        return credentialSubject;
    }

    @Builder
    public record CredentialSubject(
            @JsonProperty("mandate") Mandate mandate
    ) {
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
            ) { }

            @Builder
            public record Mandatee(
                    @JsonProperty("id") String id,
                    @JsonProperty("email") String email,
                    // To keep compatibility with the v1 credential we keep the old name
                    @JsonProperty("firstName") @JsonAlias("first_name") String firstName,
                    // To keep compatibility with the v1 credential we keep the old name
                    @JsonProperty("lastName") @JsonAlias("last_name") String lastName,
                    @JsonProperty("mobile_phone") String mobilePhone,
                    @JsonProperty("nationality") String nationality
            ) { }

            @Builder
            public record Mandator(
                    @JsonProperty("commonName") String commonName,
                    @JsonProperty("country") String country,
                    @JsonProperty("emailAddress") String emailAddress,
                    @JsonProperty("organization") String organization,
                    @JsonProperty("organizationIdentifier") String organizationIdentifier,
                    @JsonProperty("serialNumber") String serialNumber
            ) { }

            @Builder
            public record Power(
                    @JsonProperty("id") String id,
                    @JsonProperty("action") @JsonAlias("tmf_action") Object action,
                    @JsonProperty("domain") @JsonAlias("tmf_domain") String domain,
                    @JsonProperty("function") @JsonAlias("tmf_function") String function,
                    @JsonProperty("type") @JsonAlias("tmf_type") String type
            ) { }

            @Builder
            public record Signer(
                    @JsonProperty("commonName") String commonName,
                    @JsonProperty("country") String country,
                    @JsonProperty("emailAddress") String emailAddress,
                    @JsonProperty("organization") String organization,
                    @JsonProperty("organizationIdentifier") String organizationIdentifier,
                    @JsonProperty("serialNumber") String serialNumber
            ) { }
        }
    }
}


