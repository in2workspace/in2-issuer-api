<div align="center">

<h1>Credential Issuer</h1>
<span>by </span><a href="https://in2.es">in2.es</a>
<p><p>

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_credential-issuer&metric=alert_status)](https://sonarcloud.io/dashboard?id=in2workspace_credential-issuer)

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_credential-issuer&metric=bugs)](https://sonarcloud.io/summary/new_code?in2workspace_credential-issuer)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_credential-issuer&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=in2workspace_credential-issuer)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_credential-issuer&metric=security_rating)](https://sonarcloud.io/dashboard?id=in2workspace_credential-issuer)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_credential-issuer&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=in2workspace_credential-issuer)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_credential-issuer&metric=ncloc)](https://sonarcloud.io/dashboard?id=in2workspace_credential-issuer)

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_credential-issuer&metric=coverage)](https://sonarcloud.io/summary/new_code?id=in2workspace_credential-issuer)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_credential-issuer&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=in2workspace_credential-issuer)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_credential-issuer&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=in2workspace_credential-issuer)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_credential-issuer&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=in2workspace_credential-issuer)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_credential-issuer&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=in2workspace_credential-issuer)

</div>

# Introduction
Credential Issuer is a service that allows to generate verifiable credentials. It is designed to be used in a decentralized identity ecosystem, where users can generate the emission of verifiable credentials and store them using the Wallet Server service.

# Architecture
![Architecture](docs/images/issuer-architecture.png)
The Issuer solution includes the requested features described in the technical specification [OpenID4VCI DOME profile](https://dome-marketplace.github.io/OpenID4VCI-DOMEprofile/openid-4-verifiable-credential-issuance-wg-draft.html) (Issuer-initiated flow)
it integrates the following key components and dependencies:
- Issuer-API: [Official Docker Image](https://hub.docker.com/r/in2workspace/issuer-api/tags)
- Posgres Data Base [Official Docker Image](postgres:16.3)
- Issuer-UI [Official Docker Image](https://hub.docker.com/r/in2workspace/issuer-ui)
- IAM: a custom implementation of Quay.io Keycloak Docker image [Official Docker Image](https://hub.docker.com/r/in2workspace/dome-issuer-keycloak)


# Functionalities
- Issuance of LEAR Credential Employee
- Deferred credential emission
- pre-authorized code flow with PIN
- Persistence of emitted credentials in ddb
- Retrieval and management of emitted credentials
- Sign Credentials using a Remote DSS
# Installation

We offer Docker images of the necessary components to run the solution.
You can follow the instruction to instance the necessary components and the necessary configurations of each one to sucessfully run/deploy the Issuer.

## Running the application
Before running the Issuer service you need to configure and run the IAM solution:
### Keycloak
```bash
docker run -d \
  --name issuer-keycloak \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -e KC_HOSTNAME_URL=https://localhost:8443 \
  -e KC_HOSTNAME_ADMIN_URL=https://localhost:8443 \
  -e KC_HTTPS_CLIENT_AUTH=request \
  -e KC_DB=postgres \
  -e KC_DB_USERNAME=postgres \
  -e KC_DB_PASSWORD=postgres \
  -e KC_DB_URL=jdbc:postgresql://issuer-keycloak-postgres/cred \
  -e DB_PORT=5432 \
  -e ISSUER_API_URL=http://issuer-api:8080 \
  -e ISSUER_API_EXTERNAL_URL=http://issuer-api-external.com \
  -e PRE_AUTH_LIFESPAN=10 \
  -e PRE_AUTH_LIFESPAN_TIME_UNIT=MINUTES \
  -e TX_CODE_SIZE=4 \
  -e TX_CODE_DESCRIPTION="Enter the PIN code" \
  -e TOKEN_EXPIRATION=2592000 \
  -p 7001:8080 \
  -p 8443:8443 \
  in2workspace/dome-issuer-keycloak:v1.0.0-SNAPSHOT
```
### Keycloak Postgres
```bash
docker run -d \
  --name issuer-keycloak-postgres \
  -e POSTGRES_DB=cred \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5433:5432 \
  -v postgres_data:/var/lib/postgresql/data \
  postgres:16.3
```
Ensure you have the volume postgres_data created before running the postgres container:
```bash
docker volume create postgres_data
```
And then the functional components of the Issuer service as follows:
### Issuer API
```bash
docker run -d \
  --name issuer-api \
  -e SPRING_R2DBC_URL=r2dbc:postgresql://issuer-postgres:5432/issuer \
  -e SPRING_R2DBC_USERNAME=postgres \
  -e SPRING_R2DBC_PASSWORD=postgres \
  -e SPRING_FLYWAY_URL=jdbc:postgresql://issuer-postgres:5432/issuer \
  -e SPRING_MAIL_HOST=smtp.example.com \
  -e SPRING_MAIL_PORT=1025 \
  -e SPRING_MAIL_USERNAME=example@example.com \
  -e SPRING_MAIL_PASSWORD=password \
  -e SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true \
  -e SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true \
  -e SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_TRUST=smtp.example.com \
  -e API_EXTERNAL_DOMAIN=http://issuer-api-external.com \
  -e API_CONFIG_SOURCE=yaml \
  -e API_CACHE_LIFETIME_CREDENTIAL_OFFER=10 \
  -e API_CACHE_LIFETIME_VERIFIABLE_CREDENTIAL=10 \
  -e AUTH_SERVER_PROVIDER=keycloak \
  -e AUTH_SERVER_EXTERNAL_DOMAIN=https://keycloak-external.com \
  -e AUTH_SERVER_INTERNAL_DOMAIN=http://issuer-keycloak:8080 \
  -e AUTH_SERVER_REALM=CredentialIssuer \
  -e AUTH_SERVER_CLIENT_CLIENT_ID=oidc4vci-wallet-client \
  -e AUTH_SERVER_CLIENT_CLIENT_SECRET=qVB2taQhqWmVndVIG5QR1INH8rfsbTrS \
  -e AUTH_SERVER_CLIENT_USERNAME=user \
  -e AUTH_SERVER_CLIENT_PASSWORD=user \
  -e ISSUER_UI_EXTERNAL_DOMAIN=http://localhost:4201 \
  -e REMOTE_SIGNATURE_EXTERNAL_DOMAIN=http://remote-dss.com \
  -p 8081:8080 \
  in2workspace/issuer-api:v1.1.0-SNAPSHOT
```
### Issuer Postgres
```bash
docker run -d \
  --name issuer-postgres \
  -e POSTGRES_DB=issuer \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5434:5432 \
  -v postgres_data:/var/lib/postgresql/issuer-api-data \
  postgres:16.3
```

### Issuer UI
```bash
docker run -d \
  --name issuer-ui \
  -e LOGIN_URL=http://keycloak-external.org/realms/CredentialIssuer \
  -e CLIENT_ID=account-console \
  -e SCOPE="openid profile email offline_access" \
  -e GRANT_TYPE=code \
  -e BASE_URL=http://issuer-api.com/ \
  -e WALLET_URL=http://wallet.com/ \
  -e PROCEDURES=/api/v1/procedures \
  -e SAVE_CREDENTIAL="/api/v1/credentials?type=LEARCredentialEmployee" \
  -e CREDENTIAL_OFFER_URL=/api/v1/credential-offer \
  -e NOTIFICATION=/api/v1/notifications \
  -p 4201:8080 \
  in2workspace/issuer-ui:v1.0.0
```


## Understanding the Configuration

## Contribution

### How to contribute
If you want to contribute to this project, please read the [CONTRIBUTING.md](CONTRIBUTING.md) file.

## License
This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Project/Component Status
This project is currently in development.

## Contact
For any inquiries or further information, feel free to reach out to us:

- **Email:** [Oriol Canadés](mailto:oriol.canades@in2.es)
- **Name:** IN2, Ingeniería de la Información
- **Website:** [https://in2.es](https://in2.es)

## Acknowledgments
This project is part of the IN2 strategic R&D, which has received funding from the [DOME](https://dome-marketplace.eu/) project within the European Union’s Horizon Europe Research and Innovation programme under the Grant Agreement No 101084071.
