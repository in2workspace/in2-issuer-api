package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.service.IssuerVcTemplateService;
import es.in2.issuer.domain.model.VcTemplate;
//import id.walt.credentials.w3c.templates.VcTemplate;
//import id.walt.credentials.w3c.templates.VcTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

// fixme: esta clase la debemos eliminar y buscar un sistema de configuración para los templates
@Slf4j
@Service
@RequiredArgsConstructor
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
                .concatMap(vc -> Mono.fromCallable(() -> new VcTemplate(false, vc,null)))
                .collectList();
    }

    @Override
    public Mono<List<VcTemplate>> getAllDetailedVcTemplates() {
        return Flux.fromIterable(vcTemplateNames)
                .concatMap(name -> Mono.fromCallable(() -> new VcTemplate(false, name,null)))
                .collectList();
    }

    @Override
    public Mono<VcTemplate> getTemplate(String templateName) {
        return Mono.fromCallable(() -> new VcTemplate(false, templateName,null))
                .onErrorMap(IllegalArgumentException.class, e -> new IllegalArgumentException("Error getting template: " + e.getMessage(), e));
    }

}
