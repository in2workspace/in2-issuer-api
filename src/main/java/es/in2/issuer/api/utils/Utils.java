package es.in2.issuer.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.api.exceptions.InvalidTokenException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public Utils() {
        throw new IllegalStateException("Utility class");
    }


    public static final String SERVICE_MATRIX_PATH = "service-matrix.properties";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";


    /**
     * Converts the input to a String representing a JSON.
     *
     * @param data   data class instance with only JsonProperties attributes of either primitive types or other data
     *               classes types that display this same characteristics recursively
     * @return a String representing a JSON
     */
    public static String toJsonString(Object data, PropertyNamingStrategy naming) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(naming);
        return objectMapper.writeValueAsString(data);
    }

    public static String toJsonString(Object data) throws Exception {
        return toJsonString(data, PropertyNamingStrategies.SNAKE_CASE);
    }

    /**
     * Parses a JWT token from an "Authorization" header string.
     * This method extracts a JWT token from an "Authorization" header string and parses it into a [SignedJWT] object.
     * It verifies that the input string starts with "Bearer " and attempts to parse the JWT token.
     * If the token is not a valid JWT or the input is malformed, it raises an [InvalidToken] exception.
     *
     * @param exchange The HttpServletRequest containing the "Authorization" header.
     * @return A [SignedJWT] object representing the parsed JWT token.
     * @throws InvalidTokenException if the input is not a valid JWT token or the string is malformed.
     */
    public static SignedJWT getToken(ServerWebExchange exchange) throws InvalidTokenException {
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        return getToken(authorizationHeader);
    }

    private static SignedJWT getToken(String authorizationHeader) throws InvalidTokenException {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }

        try {
            String jwtString = authorizationHeader.substring(7).trim();

            return SignedJWT.parse(jwtString);
        } catch (Exception e) {
            throw new InvalidTokenException();
        }
    }

    public static Date createDate(String dateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return dateFormat.parse(dateString);
    }
    public static boolean isNullOrBlank(String string) {
        return string == null || string.isBlank();
    }

}
