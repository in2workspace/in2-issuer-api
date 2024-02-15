package es.in2.issuer.api.service.impl;

import es.in2.issuer.api.service.IssuerVcTemplateService;
import id.walt.credentials.w3c.templates.VcTemplate;
import id.walt.credentials.w3c.templates.VcTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssuerVcTemplateServiceImpl implements IssuerVcTemplateService {

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
    @Override
    public Mono<List<VcTemplate>> getAllVcTemplates() {
        return Flux.fromIterable(vcTemplateNames)
                .concatMap(vc -> Mono.fromCallable(() -> new VcTemplate(vc,null,false)))
                .collectList();
    }

    @Override
    public Mono<List<VcTemplate>> getAllDetailedVcTemplates() {
        return Flux.fromIterable(vcTemplateNames)
                .concatMap(name -> Mono.fromCallable(() -> VcTemplateService.Companion.getService().getTemplate(name, true, VcTemplateService.SAVED_VC_TEMPLATES_KEY)))
                .collectList();
    }

    @Override
    public Mono<VcTemplate> getTemplate(String templateName) {
        return Mono.fromCallable(() -> VcTemplateService.Companion.getService().getTemplate(templateName,true,VcTemplateService.SAVED_VC_TEMPLATES_KEY))
                .onErrorMap(IllegalArgumentException.class, e -> new IllegalArgumentException("Error getting template: " + e.getMessage(), e));
    }
}
