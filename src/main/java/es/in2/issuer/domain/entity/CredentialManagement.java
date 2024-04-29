package es.in2.issuer.domain.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("credentials.credential_management")
public class CredentialManagement {
    @Id
    @Column("id")
    private UUID id;

    @Column("user_id")
    private String userId;

    @Column("credential_format")
    private String credentialFormat;

    @Column("credential_decoded")
    private String credentialDecoded;

    @Column("credential_encoded")
    private String credentialEncoded;

    @Column("credential_status")
    private String credentialStatus;

    @Column("modified_at")
    private Timestamp modifiedAt;
}
