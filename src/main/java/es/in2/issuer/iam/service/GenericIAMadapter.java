package es.in2.issuer.iam.service;

public interface GenericIAMadapter {
    String getJwtDecoder();
    String getJwtDecoderLocal();
    String getPreAuthCodeUri();
    String getTokenUri();
}
