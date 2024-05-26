package dkachurin.scentbird.xogamebot;

import dkachurin.scentbird.xogamebot.client.GameClient;
import dkachurin.scentbird.xogamebot.client.RoomClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;

@SpringBootApplication
@EnableFeignClients(clients = {RoomClient.class, GameClient.class})
@ImportAutoConfiguration(FeignAutoConfiguration.class)
public class XOGameBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(XOGameBotApplication.class, args);
	}

}
