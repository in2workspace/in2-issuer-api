package es.in2.issuer.iam.service;

public interface GenericIamAdapter {
    String getJwtDecoder();
    String getJwtDecoderLocal();
    String getPreAuthCodeUri();
    String getTokenUri();
}
