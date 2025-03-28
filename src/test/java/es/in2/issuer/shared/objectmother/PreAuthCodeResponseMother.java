package es.in2.issuer.shared.objectmother;

import es.in2.issuer.shared.domain.model.dto.Grant;
import es.in2.issuer.shared.domain.model.dto.PreAuthCodeResponse;

import static es.in2.issuer.authserver.domain.utils.Constants.*;

public final class PreAuthCodeResponseMother {

    private PreAuthCodeResponseMother() {
    }

    public static PreAuthCodeResponse dummy() {
        return new PreAuthCodeResponse(
                new Grant("preAuthorizedCode",
                        new Grant.TxCode(5, "inputMode", "description")),
                "pin"
        );
    }

    public static PreAuthCodeResponse withPreAuthCodeAndPin(String preAuthCode, String pin) {
        return new PreAuthCodeResponse(
                new Grant(preAuthCode,
                        new Grant.TxCode(TX_CODE_SIZE, TX_INPUT_MODE, TX_CODE_DESCRIPTION)),
                pin
        );
    }
}
