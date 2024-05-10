package es.in2.issuer.infrastructure.iam.service;

public interface GenericIamAdapter {
    String getJwtDecoder();
    String getJwtDecoderLocal();
    String getPreAuthCodeUri();
    String getTokenUri();
    String getJwtValidator();
}
