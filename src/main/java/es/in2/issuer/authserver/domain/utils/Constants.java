package es.in2.issuer.authserver.domain.utils;

public final class Constants {

    private Constants() {
    }

    public static final int TX_CODE_SIZE = 4;
    public static final String TX_CODE_DESCRIPTION =
            "A PIN has been sent to your email. Check your inbox. Enter your PIN Code.";
    public static final String TX_INPUT_MODE = "numeric";

    public static final long PIN_BY_PRE_AUTH_CODE_CACHE_STORAGE_EXPIRY_DURATION_MINUTES = 5;
}
