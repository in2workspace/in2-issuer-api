package es.in2.issuer.backend.shared.domain.model.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("issuer.signature_configuration_audit")
public class SignatureConfigurationAudit {

    @Id
    @Column("id")
    private UUID id;

    @Column("signature_configuration_id")
    private String signatureConfigurationId;

    @Column("user_email")
    private String userEmail;

    @Column("organization_identifier")
    private String organizationIdentifier;

    @Column("instant")
    private Instant instant;

    @Column("old_values")
    private String oldValues;

    @Column("new_values")
    private String newValues;

    @Column("rationale")
    private String rationale;

    @Column("encrypted")
    private boolean encrypted;
}
