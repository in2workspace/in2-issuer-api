package es.in2.issuer.shared.objectmother;

import es.in2.issuer.shared.domain.model.dto.Grant;
import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;

import static es.in2.issuer.authserver.domain.utils.Constants.*;

public final class PreAuthorizedCodeResponseMother {

    private PreAuthorizedCodeResponseMother() {
    }

    public static PreAuthorizedCodeResponse dummy() {
        return new PreAuthorizedCodeResponse(
                new Grant("preAuthorizedCode",
                        new Grant.TxCode(5, "inputMode", "description")),
                "pin"
        );
    }

    public static PreAuthorizedCodeResponse withPreAuthorizedCodeAndPin(String preAuthorizedCode, String pin) {
        return new PreAuthorizedCodeResponse(
                new Grant(preAuthorizedCode,
                        new Grant.TxCode(TX_CODE_SIZE, TX_INPUT_MODE, TX_CODE_DESCRIPTION)),
                pin
        );
    }
}
