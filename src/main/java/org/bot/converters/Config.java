package org.bot.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public String getFaRegistrationChannel() {
        return String.valueOf(getYaml().get("fa-registration"));
    }
    public String getTeamRegistrationChannel() {
        return String.valueOf(getYaml().get("team-registration"));
    }
    public String getRosterChannel() {
        return String.valueOf(getYaml().get("roster-channel"));
    }
    public String getCommandLogPath() {
        return String.valueOf(getYaml().get("command-log-path"));
    }
    public String getDatabasePath() {
        return String.valueOf(getYaml().get("database-path"));
    }
    public int getSeasonNumber() {
        return (int) objectToMap(getYaml().get("season")).get("number");
    }
    public Date getStartDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return simpleDateFormat.parse(
                    (String) objectToMap(objectToMap(getYaml().get("season")).get("dates")).get("start"));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public void setStartDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            simpleDateFormat.parse(date.toString());


        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public Date getEndDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return simpleDateFormat.parse(
                    (String) objectToMap(objectToMap(getYaml().get("season")).get("dates")).get("end"));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    private Map objectToMap(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(obj, Map.class);
    }
}
