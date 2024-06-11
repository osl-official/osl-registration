package org.bot.scripts;

import lombok.Cleanup;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.bot.converters.Config;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CommandLogger {
    private final String PATH = new Config().getCommandLogPath();
    private final char SEPARATOR = ',';

    public void recordNewRequest(long discordId, String requestType, long epochTime) {
        try {
            @Cleanup FileWriter fileWriter = new FileWriter(PATH, true);

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
            @Cleanup BufferedReader bufferedReader = new BufferedReader(new FileReader(PATH));
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
            @Cleanup FileWriter fileWriter = new FileWriter(PATH, true);

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
            @Cleanup FileWriter fileWriter = new FileWriter(PATH, false);

            fileWriter.append("DiscordId, RequestType, EpochTime").append(System.lineSeparator());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
