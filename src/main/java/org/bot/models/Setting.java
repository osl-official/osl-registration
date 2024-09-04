package org.bot.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "setting")
public class Setting {

    private long faRegistration;
    private long teamRegistration;
    private long rosterChannel;
    private String commandLogPath;
    private String databasePath;
    private String databaseBaseUrl;
    private Season season;
}
