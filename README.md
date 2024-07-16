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

## Dependencies
To utilize the Credential Issuer, you will need the following components:

- **Issuer-UI**
- **Issuer-API**
- **Postgres Database**
- **Our Custom Keycloak Solution**
- **SMTP Email Server**

For each dependency, you can refer to their respective repositories for detailed setup instructions.

### Issuer UI
Issuer UI is the user interface for the Credential Issuer.
Refer to the [Issuer UI Documentation](https://github.com/in2workspace/issuer-ui) for more information on configuration variables.

### Issuer API
The Server application of the Issuer needs key environment variables to be configured
##### Database
- SPRING_R2DBC_URL
- SPRING_R2DBC_USERNAME
- SPRING_R2DBC_PASSWORD
- SPRING_FLYWAY_URL
##### SMTP Email Server
- SPRING_MAIL_PORT
- SPRING_MAIL_USERNAME
- SPRING_MAIL_PASSWORD
- SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH
- SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE
- SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_TRUST
##### Authorization Server (Keycloak)
- AUTH_SERVER_EXTERNAL_DOMAIN
- AUTH_SERVER_INTERNAL_DOMAIN
- AUTH_SERVER_REALM: name of the keycloak realm
- AUTH_SERVER_CLIENT_CLIENT_ID: client of the dedicated user for M2M communication
- AUTH_SERVER_CLIENT_USERNAME: dedicated user for M2M communication
- AUTH_SERVER_CLIENT_PASSWORD
##### API
- API_EXTERNAL_DOMAIN
- API_CACHE_LIFETIME_CREDENTIAL_OFFER: duration in minutes of the Credential Offer
- REMOTE_SIGNATURE_EXTERNAL_DOMAIN: 
#### Example of a typical configuration:
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
  -e API_CACHE_LIFETIME_CREDENTIAL_OFFER=10 \
  -e AUTH_SERVER_EXTERNAL_DOMAIN=https://keycloak-external.com \
  -e AUTH_SERVER_INTERNAL_DOMAIN=http://issuer-keycloak:8080 \
  -e AUTH_SERVER_REALM=CredentialIssuer \
  -e AUTH_SERVER_CLIENT_CLIENT_ID=oidc4vci-wallet-client \
  -e AUTH_SERVER_CLIENT_USERNAME=user \
  -e AUTH_SERVER_CLIENT_PASSWORD=user \
  -e ISSUER_UI_EXTERNAL_DOMAIN=http://localhost:4201 \
  -e REMOTE_SIGNATURE_EXTERNAL_DOMAIN=http://remote-dss.com \
  -p 8081:8080 \
  in2workspace/issuer-api:v1.1.0-SNAPSHOT
```

### Postgres Database
Postgres is used as the database for the Issuer API.
You can find more information in the [official documentation](https://www.postgresql.org/docs/).
#### Example of a typical configuration:
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

### Custom Keycloak
Keycloak is used for identity and access management, as well as for other OpenID4VCI DOME profile requirements.

It's an implementation of the official quay.io keycloak image with a custom plugin.
Refer to the [Keycloak Plugin Documentation](https://github.com/in2workspace/issuer-keycloak-plugin) for more information on setup and configuration variables.

### SMTP Email Server
An SMTP Email Server of your choice. It must support StartTLS for a secure connection.

## Understanding the Configuration
Each component has its own set of environment variables that need to be configured to run the service successfully. The key variables are highlighted in their respective sections above or in the linked documentation.

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
