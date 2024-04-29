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
@Table("credentials.credential_deferred")
public class CredentialDeferred {
    @Id
    @Column("id")
    private UUID id;

    @Column("transaction_id")
    private String transactionId;

    @Column("credential_id")
    private UUID credentialId;

    @Column("credential_signed")
    private String credentialSigned;
}
