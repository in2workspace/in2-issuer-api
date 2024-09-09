package es.in2.issuer.domain.model.dto;

import lombok.Builder;

@Builder
public record UserDetails(
      String commonName,
      String country,
      String emailAddress,
      String serialNumber,
      String organizationIdentifier,
      String organization
) {
}
