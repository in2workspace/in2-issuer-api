package es.in2.issuer.domain.model.entities;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeferredCredentialMetadataTest {

    @Test
    void testDeferredCredentialMetadata() {
        UUID uuid = UUID.randomUUID();
        String transactionCode = "testTransactionCode";
        String authServerNonce = "testAuthServerNonce";
        String transactionId = "testTransactionId";
        UUID procedureId = UUID.randomUUID();
        String vc = "testVc";
        String vcFormat = "testVcFormat";

        DeferredCredentialMetadata deferredCredentialMetadata = DeferredCredentialMetadata.builder()
                .id(uuid)
                .transactionCode(transactionCode)
                .authServerNonce(authServerNonce)
                .transactionId(transactionId)
                .procedureId(procedureId)
                .vc(vc)
                .vcFormat(vcFormat)
                .build();

        assertEquals(uuid, deferredCredentialMetadata.getId());
        assertEquals(transactionCode, deferredCredentialMetadata.getTransactionCode());
        assertEquals(authServerNonce, deferredCredentialMetadata.getAuthServerNonce());
        assertEquals(transactionId, deferredCredentialMetadata.getTransactionId());
        assertEquals(procedureId, deferredCredentialMetadata.getProcedureId());
        assertEquals(vc, deferredCredentialMetadata.getVc());
        assertEquals(vcFormat, deferredCredentialMetadata.getVcFormat());
    }
}