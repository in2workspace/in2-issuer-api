package es.in2.issuer.domain.model.dto.credential.lear;

import es.in2.issuer.domain.model.dto.credential.W3CVerifiableCredential;

import java.util.List;

public interface LEARCredential extends W3CVerifiableCredential {
    List<Power> getPowers();
    Mandator getMandator();
}
