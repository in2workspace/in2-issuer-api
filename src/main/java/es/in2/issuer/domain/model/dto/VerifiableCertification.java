package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Builder
public record VerifiableCertification(
        @JsonProperty("@context") List<String> context,
        @JsonProperty("id") String id,
        @NotNull
        @JsonProperty("type") List<String> type,
        @JsonProperty("issuer") Issuer issuer,
        @NotNull
        @JsonProperty("credentialSubject") CredentialSubject credentialSubject,
        @NotNull
        @JsonProperty("validFrom") String validFrom,

        @NotNull
        @JsonProperty("validUntil") String validUntil,
        @JsonProperty("signer") Signer signer
) {
    @Builder
    public record Issuer(
            @JsonProperty("commonName") String commonName,
            @JsonProperty("country") String country,
            @JsonProperty("id") String id,
            @JsonProperty("organization") String organization
    ) {
    }
    @Builder
    public record CredentialSubject(
            @JsonProperty("company") Company company,
            @JsonProperty("compliance") List<Compliance> compliance,
            @JsonProperty("product") Product product
    ) {
        @Builder
        public record Company(
                @JsonProperty("address") String address,
                @JsonProperty("commonName") String commonName,
                @JsonProperty("country") String country,
                @JsonProperty("email") String email,
                @JsonProperty("id") String id,
                @JsonProperty("organization") String organization
        ) {}
        @Builder
        public record Compliance(
                @JsonProperty("id") String id,
                @JsonProperty("scope") String scope,
                @JsonProperty("standard") String standard
        ) {}
        @Builder
        public record Product(
                @JsonProperty("productId") String productId,
                @JsonProperty("productName") String productName,
                @JsonProperty("productVersion") String productVersion
        ) {}
    }
    @Builder
    public record Signer(
            @JsonProperty("commonName") String commonName,
            @JsonProperty("country") String country,
            @JsonProperty("emailAddress") String emailAddress,
            @JsonProperty("organization") String organization,
            @JsonProperty("organizationIdentifier") String organizationIdentifier,
            @JsonProperty("serialNumber") String serialNumber
    ) {}
}
