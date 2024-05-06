package es.in2.issuer.domain.util;

public class EndpointsConstants {
    public static final String CREDENTIAL_OFFER = "/api/v1/credential-offer/";
    public static final String LEAR_CREDENTIAL_URI = "/api/v1/credential-offer?credential_offer_uri=";
    public static final String CREDENTIAL = "/api/v1/vc/credential";
    public static final String CREDENTIAL_BATCH = "/api/v1/vc/batch_credential";
    public static final String CREDENTIAL_DEFERRED = "/api/v1/vc/deferred_credential";
    public static final String PUBLIC_HEALTH = "/health";
    public static final String PUBLIC_CREDENTIAL_OFFER = "/api/v1/credential-offer/**";
    public static final String PUBLIC_DISCOVERY_ISSUER = "/.well-known/openid-credential-issuer";
    public static final String PUBLIC_DISCOVERY_AUTH_SERVER = "/.well-known/openid-configuration";
    public static final String LOCAL_ISSUER_UI = "http://localhost:4201";
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String SWAGGER_RESOURCES = "/swagger-resources/**";
    public static final String SWAGGER_API_DOCS = "/api-docs/**";
    public static final String SWAGGER_SPRING_UI = "/spring-ui/**";
    public static final String SWAGGER_WEBJARS = "/webjars/swagger-ui/**";
}
