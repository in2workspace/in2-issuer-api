package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.service.VerifiableCredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class VerifiableCredentialServiceImpl implements VerifiableCredentialService {
    @Override
    public Mono<String> generateVcPayLoad(String vcTemplate, String subjectDid, String issuerDid, Map<String, Object> userData, Instant expiration) throws JSONException {
        return Mono.fromCallable(()->{
            // Parse vcTemplate to a JSON object
            JSONObject vcTemplateObject = new JSONObject(vcTemplate);

            // Generate a unique UUID for jti and vc.id
            String uuid = "urn:uuid:" + UUID.randomUUID().toString();

            // Calculate timestamps
            Instant nowInstant = Instant.now();
            long nowTimestamp = nowInstant.getEpochSecond();
            long expTimestamp = expiration.getEpochSecond();


            // Update vcTemplateObject with dynamic values
            vcTemplateObject.put("id", uuid);
            vcTemplateObject.put("issuer", new JSONObject().put("id", issuerDid));
            // Update issuanceDate, issued, validFrom, expirationDate in vcTemplateObject using ISO 8601 format
            String nowDateStr = nowInstant.toString();
            String expirationDateStr = expiration.toString();
            vcTemplateObject.put("issuanceDate", nowDateStr);
            vcTemplateObject.put("issued", nowDateStr);
            vcTemplateObject.put("validFrom", nowDateStr);
            vcTemplateObject.put("expirationDate", expirationDateStr);

            // Convert userData map contents to Object and set as credentialSubject
            Object credentialSubjectValue = userData.get("credentialSubject");
            vcTemplateObject.put("credentialSubject", new JSONObject((Map) credentialSubjectValue));

            // Construct final JSON Object
            JSONObject finalObject = new JSONObject();
            finalObject.put("sub", subjectDid);
            finalObject.put("nbf", nowTimestamp);
            finalObject.put("iss", issuerDid);
            finalObject.put("exp", expTimestamp);
            finalObject.put("iat", nowTimestamp);
            finalObject.put("jti", uuid);
            finalObject.put("vc", vcTemplateObject);

            // Return final object as String
            return finalObject.toString();
        });
    }
}
