package es.in2.issuer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IssuerApiApplication {

	// todo: añadir el ObjectMapper centralizado

	public static void main(String[] args) {
		SpringApplication.run(IssuerApiApplication.class, args);
	}

}
