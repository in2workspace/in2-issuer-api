package es.in2.issuer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IssuerApiApplication {

	private static final ObjectMapper OBJECT_MAPPER =
			JsonMapper.builder()
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
					.build();

	public static void main(String[] args) {
		SpringApplication.run(IssuerApiApplication.class, args);
	}
	@Bean
	public ObjectMapper objectMapper() {
		return OBJECT_MAPPER;
	}

}
