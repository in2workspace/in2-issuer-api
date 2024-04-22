package es.in2.issuer.domain.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import java.util.UUID;
import java.sql.Timestamp;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("credentials.credentials")
public class Credential {

    @Id
    @Column("id")
    private UUID id;

    @Column("user_id")
    private String userId;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("email")
    private String email;

    @Column("phone_number")
    private String phoneNumber;

    @Column("credential_data")
    private String credentialData;

    @Column("status")
    private String status;

    @Column("transaction_id")
    private String transactionId;

    @Column("created_at")
    private Timestamp createdAt;

    @Column("modified_at")
    private Timestamp modifiedAt;
}