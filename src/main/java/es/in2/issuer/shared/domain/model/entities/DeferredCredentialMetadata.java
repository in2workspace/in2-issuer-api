package es.in2.issuer.shared.domain.model.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("issuer.deferred_credential_metadata")
// todo: CredentialOfferRecord
public class DeferredCredentialMetadata {

    // Este id es el id del /credential-offer/{id}
    @Id
    @Column("id")
    private UUID id;

    // todo: Este es el activation_code
//    @Column("activation_code")
//    private String activationCode;

    // Este quedará deprecado por el activation_code
    @Deprecated()
    @Column("transaction_code")
    private String transactionCode;

    // fixme: Quedará deprecado cuando se use el pre-authorized_code field
    @Column("auth_server_nonce")
    private String authServerNonce;

    // pre-authorized_code field
    // se guarda en la generación del pre-authorized_code
    // todo: no implemetar ahora y seguir usando el authServerNonce porque es
    //  el mismo valor

    // tx_code field
    // se guarda en la generación del pre-authorized_code

    // access_token field



    // transaction_id es el ID que devuelve el Credential Endpoint en el flujo Deferred Credential
    @Column("transaction_id")
    private String transactionId;

    // Este campo es el ID del IssuanceRecord del Backoffice
    @Column("procedure_id")
    private UUID procedureId;

    // Credential Owner email field

    // Credential Offer object


    // Estos campos implican en el deferred
    @Column("vc")
    private String vc;

    @Column("vc_format")
    private String vcFormat;

    @Column("operation_mode")
    private String operationMode;

    @Column("response_uri")
    private String responseUri;

}
