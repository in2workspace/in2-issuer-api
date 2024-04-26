package es.in2.issuer.domain.entity;

import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("credentials.credential_managment")
public class CredentialManagment {
}
