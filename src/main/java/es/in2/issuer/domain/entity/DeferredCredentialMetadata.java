package es.in2.issuer.domain.entity;

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
@Table("credentials.deferred_credential_metadata")
public class DeferredCredentialMetadata {
    @Id
    @Column("id")
    private UUID id;

    @Column("transaction_code")
    private String transactionCode;

    @Column("auth_server_nonce")
    private String authServerNonce;

    @Column("transaction_id")
    private String transactionId;

    @Column("procedure_id")
    private UUID procedureId;

    @Column("vc")
    private String vc;

    @Column("vc_format")
    private String vcFormat;
}
