package dkachurin.scentbird.xogamebot;

import dkachurin.scentbird.xogamebot.configs.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class XOGameServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(XOGameServerApplication.class, args);
	}

}
