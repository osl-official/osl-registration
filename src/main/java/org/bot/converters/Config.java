package org.bot.converters;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
public class Config {
    private final String CONFIG_FILE = "settings.yml";

    private Map<String, Object> getYaml() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            Yaml yaml = new Yaml();
            return yaml.load(inputStream);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    public String getToken() {
        return String.valueOf(getYaml().get("token"));
    }
    public String getFaRegistrationChannel() {
        return String.valueOf(getYaml().get("fa-registration"));
    }
    public String getTeamRegistrationChannel() {
        return String.valueOf(getYaml().get("team-registration"));
    }
}
