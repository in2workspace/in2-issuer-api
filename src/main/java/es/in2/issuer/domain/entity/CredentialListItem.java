package es.in2.issuer.domain.entity;

import java.util.Date;
import java.util.UUID;

public interface CredentialListItem {
    UUID getId();
    String getFirstName();
    String getLastName();
    String getStatus();
    Date getModifiedAt();
}
