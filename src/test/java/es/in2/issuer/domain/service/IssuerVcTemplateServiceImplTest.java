package es.in2.issuer.domain.service;

import es.in2.issuer.domain.service.impl.IssuerVcTemplateServiceImpl;
import es.in2.issuer.domain.model.VcTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class IssuerVcTemplateServiceImplTest {

    @InjectMocks
    private IssuerVcTemplateServiceImpl issuerVcTemplateService;
    private final List<String> vcTemplateNames = Arrays.asList(
            "LegalPerson", "Email", "Europass", "KybCredential", "UniversityDegree",
            "NEOM/StudentCard", "AmletCredential", "EbsiVerifiableAttestationPerson", "DataConsortium",
            "VerifiableAuthorization", "EbsiAccreditedVerifiableAttestation", "OpenBadgeCredential",
            "EuropeanBankIdentity", "Iso27001Certificate", "GaiaxCredential", "EbsiVerifiableAttestationLegal",
            "PermanentResidentCard", "VerifiablePresentation", "VerifiableId", "ProofOfResidence",
            "DeqarReport", "KybMonoCredential", "VerifiableDiploma", "ServiceOfferingCredential",
            "EbsiVerifiableAttestationGeneric", "VerifiableVaccinationCertificate", "DataServiceOffering",
            "DataSelfDescription", "EbsiVerifiableAccreditationToAccredit", "KycCredential", "EbsiEuropass",
            "EbsiDiplomaVerifiableAccreditation", "VerifiableMandate", "PeerReview", "ParticipantCredential",
            "VerifiableAttestation", "CustomerCredential", "LEARCredential"
    );

    @Test
    void getAllVcTemplates() {
        Mono<List<VcTemplate>> resultMono = issuerVcTemplateService.getAllVcTemplates();
        List<VcTemplate> result = resultMono.block();

        assertEquals(vcTemplateNames.size(), Objects.requireNonNull(result).size());
    }

    @Test
    void getAllDetailedVcTemplates() {
        Mono<List<VcTemplate>> resultMono = issuerVcTemplateService.getAllDetailedVcTemplates();
        List<VcTemplate> result = resultMono.block();

        assertEquals(vcTemplateNames.size(), Objects.requireNonNull(result).size());
    }

    @Test
    void getTemplate() {
        Mono<VcTemplate> resultMono = issuerVcTemplateService.getTemplate("LegalPerson");
        VcTemplate result = resultMono.block();

        assertEquals("LegalPerson", Objects.requireNonNull(result).name());
    }

}
