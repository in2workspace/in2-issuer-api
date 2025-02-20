package es.in2.issuer.domain.model.dto;

import java.util.List;

public interface W3CVerifiableCredential<T> {
    List<String> getContext();
    String getId();
    List<String> getType();
    String getDescription();
    Issuer getIssuer();
    String getValidFrom();
    String getValidUntil();
    T getCredentialSubject();
}
