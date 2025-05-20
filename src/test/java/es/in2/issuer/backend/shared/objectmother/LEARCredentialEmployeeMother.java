package es.in2.issuer.backend.shared.objectmother;

import es.in2.issuer.backend.shared.domain.model.dto.credential.lear.Mandator;
import es.in2.issuer.backend.shared.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;

public final class LEARCredentialEmployeeMother {
    private LEARCredentialEmployeeMother() {
    }

    public static LEARCredentialEmployee withIdAndMandatorOrganizationIdentifier() {
        return LEARCredentialEmployee
                .builder()
                .id("lear")
                .credentialSubject(
                        LEARCredentialEmployee.CredentialSubject
                                .builder()
                                .mandate(
                                        LEARCredentialEmployee.CredentialSubject.Mandate.
                                                builder()
                                                .mandator(
                                                        Mandator
                                                                .builder()
                                                                .organizationIdentifier("orgId")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();
    }
}
