package dkachurin.scentbird.xogamebot;

import dkachurin.scentbird.xogamebot.service.PlayerBotService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class XOGameBotApplicationRunnerImpl implements ApplicationRunner {

    private final PlayerBotService playerBotService;

    public XOGameBotApplicationRunnerImpl(final PlayerBotService playerBotService) {
        this.playerBotService = playerBotService;
    }

    @Override
    public void run(final ApplicationArguments args) throws InterruptedException {
        for (final String arg : args.getSourceArgs()) {
            final String[] keyValuePair = arg.split("=");
            if ("startRobotsCount".equals(keyValuePair[0])) {
                final String robotsCountString = keyValuePair[1];
                final int robotsCount = Integer.parseInt(robotsCountString);
                for (int i = 0; i < robotsCount; i++) {
                    Thread.sleep(300);
                    playerBotService.startPlayerBot();
                }
            }
        }
    }

}
