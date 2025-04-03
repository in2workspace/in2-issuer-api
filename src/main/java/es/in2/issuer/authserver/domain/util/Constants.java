package es.in2.issuer.authserver.domain.util;

public final class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final int TX_CODE_SIZE = 4;
    public static final String TX_CODE_DESCRIPTION =
            "A PIN has been sent to your email. Check your inbox. Enter your PIN Code.";
    public static final String TX_INPUT_MODE = "numeric";
}
