package es.in2.issuer.backend.shared.domain.model.entities;

import es.in2.issuer.backend.shared.domain.model.enums.SignatureMode;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import java.util.UUID;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("issuer.signature_configuration")
public class SignatureConfiguration implements Persistable<UUID> {
    @Id
    @Column("id")
    private UUID id;

    @Column("organization_identifier")
    private String organizationIdentifier;

    @Column("enable_remote_signature")
    private boolean enableRemoteSignature;

    @Column("signature_mode")
    private SignatureMode signatureMode;

    @Column("cloud_provider_id")
    private UUID cloudProviderId;

    @Column("client_id")
    private String clientId;

    @Column("secret_relative_path")
    private String secretRelativePath;

    @Column("credential_id")
    private String credentialId;

    @Column("credential_name")
    private String credentialName;

    @Transient
    @JsonIgnore
    private boolean newTransaction;

    @Override
    public UUID getId() {
        return this.id;
    }

    @JsonIgnore
    @Override
    public boolean isNew() {
        return this.newTransaction || id == null;
    }

}
