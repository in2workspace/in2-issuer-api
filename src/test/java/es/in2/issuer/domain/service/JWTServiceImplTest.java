//package es.in2.issuer.domain.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.nimbusds.jose.JWSObject;
//import com.nimbusds.jose.jwk.Curve;
//import com.nimbusds.jose.jwk.ECKey;
//import es.in2.issuer.domain.service.impl.JWTServiceImpl;
//import es.in2.issuer.infrastructure.crypto.CryptoComponent;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class JWTServiceImplTest {
//
//    @Mock
//    private ObjectMapper objectMapper;
//
//    @Mock
//    private CryptoComponent cryptoComponent;
//
//    @InjectMocks
//    private JWTServiceImpl jwtService;
//
//    @Test
//    void generateJWT() throws JsonProcessingException {
//        String payload = "{\"sub\":\"1234567890\",\"name\":\"John Doe\",\"iat\":1516239022}";
//
//        ECKey ecKey = mock(ECKey.class);
//        when(ecKey.getKeyID()).thenReturn("testKeyID");
//        when(ecKey.getCurve()).thenReturn(Curve.P_256);
//        when(cryptoComponent.getECKey()).thenReturn(ecKey);
//
//        // Mock the behavior for reading tree and converting to Map
//        JsonNode mockJsonNode = mock(JsonNode.class);
//        when(objectMapper.readTree(payload)).thenReturn(mockJsonNode);
//
//        // Create a sample map to return when converting the JSON node
//        Map<String, Object> claimsMap  = new HashMap<>();
//        claimsMap .put("sub", "1234567890");
//        claimsMap .put("name", "John Doe");
//        claimsMap .put("iat", 1516239022);
//        when(objectMapper.convertValue(any(JsonNode.class), any(TypeReference.class))).thenReturn(claimsMap);
//
//        // Generate the JWT
//        String jwt = jwtService.generateJWT(payload);
//
//        // Assert the JWT is not null and not empty
//        assertNotNull(jwt);
//        assertFalse(jwt.isEmpty());
//    }
//
//}
