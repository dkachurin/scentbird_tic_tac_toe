package dkachurin.scentbird.xogamebot.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** @noinspection FieldCanBeLocal*/
@ConfigurationProperties("application")
public class ApplicationProperties {

    private final int activeGamesLimit = 30;

    public int getActiveGamesLimit() {
        return activeGamesLimit;
    }
}