package org.bot.scripts;

import lombok.Cleanup;
import org.bot.models.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.io.*;
import java.util.stream.Collectors;

@Component
public class CommandLogger {
    private final Setting setting;
    private final char SEPARATOR = ',';

    @Autowired
    public CommandLogger(Setting setting) {
        this.setting = setting;
    }

    public void recordNewRequest(long discordId, String requestType, long epochTime) {
        try {
            @Cleanup FileWriter fileWriter = new FileWriter(setting.getCommandLogPath(), true);

            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(discordId)
                    .append(SEPARATOR)
                    .append(requestType)
                    .append(SEPARATOR)
                    .append(epochTime)
                    .append(System.lineSeparator());

            fileWriter.append(stringBuilder.toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<String> getCommandHistory() {
        try {
            @Cleanup BufferedReader bufferedReader = new BufferedReader(new FileReader(setting.getCommandLogPath()));
            return bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkPendingRequest(long discordId, String requestType) {
        return getCommandHistory().stream().filter(s -> s.contains(String.valueOf(discordId)))
                .anyMatch(s -> s.contains(requestType));
    }

    public void removeRequest(String discordId, String requestType) {
        Collection<String> history = getCommandHistory();
        purgeLogs();

        try {
            @Cleanup FileWriter fileWriter = new FileWriter(setting.getCommandLogPath(), true);

            StringBuilder stringBuilder = new StringBuilder();

            for (String line : history) {
                if (!line.equalsIgnoreCase("DiscordId, RequestType, EpochTime")) {
                    if (!line.contains(discordId) || !line.contains(requestType)) {
                        stringBuilder.append(line).append(System.lineSeparator());
                    }
                }
            }

            fileWriter.append(stringBuilder.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void purgeLogs() {
        try {
            @Cleanup FileWriter fileWriter = new FileWriter(setting.getCommandLogPath(), false);

            fileWriter.append("DiscordId, RequestType, EpochTime").append(System.lineSeparator());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
