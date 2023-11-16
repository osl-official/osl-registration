package org.bot.scripts;

import lombok.Cleanup;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
}
