package org.bot.scripts;

import lombok.Cleanup;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CommandLogger {
    private final String PATH = "src/main/resources/command-logs.txt";
    private final char SEPERATOR = ',';

    public void recordCommand(SlashCommandInteractionEvent event) {
        try {
            @Cleanup FileWriter fileWriter = new FileWriter(PATH, true);

            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(event.getName())
                    .append(SEPERATOR)
                    .append(event.getUser().getId())
                    .append(SEPERATOR)
                    .append(event.getTimeCreated().toEpochSecond())
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

    public boolean usedCommandWithinTimeFrame(SlashCommandInteractionEvent event, int interval, TimeUnit timeUnit) {
        return getCommandHistory().stream().filter(s -> s.contains(event.getUser().getId()))
                .filter(s -> s.contains(event.getName()))
                .anyMatch(s ->
                        Long.parseLong(s.split(String.valueOf(SEPERATOR))[2]) + timeUnit.toSeconds(interval) >
                                event.getTimeCreated().toEpochSecond());
    }

    public void removeOldLogs(int interval, TimeUnit timeUnit) {
        Collection<String> history = getCommandHistory();
        purgeLogs();

        try {
            @Cleanup FileWriter fileWriter = new FileWriter(PATH, true);

            StringBuilder stringBuilder = new StringBuilder();

            for (String line : history) {
                if (!line.equalsIgnoreCase("Command, DiscordID, EpochTime")) {
                    if (Long.parseLong(line.split(",")[2]) >
                            OffsetDateTime.now().toEpochSecond() - timeUnit.toSeconds(interval)) {
                        stringBuilder.append(line).append("\n");
                    }
                }
            }

            fileWriter.append(stringBuilder.toString());
            System.out.println("purged");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void purgeLogs() {
        try {
            @Cleanup FileWriter fileWriter = new FileWriter(PATH, false);

            fileWriter.append("Command, DiscordID, EpochTime").append(System.lineSeparator());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
