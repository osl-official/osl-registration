package org.bot.converters;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bot.models.Player;
import org.bot.models.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class EmbedConverter {
    private MessageEmbed messageEmbed;

    public Team getTeamFromEmbed() {
        String teamName = "";
        String teamId = "";
        List<Player> players = new ArrayList<>();

        for (MessageEmbed.Field field : messageEmbed.getFields()) {
            if (Objects.requireNonNull(field.getName()).equalsIgnoreCase("Team Name")) {
                teamName = field.getValue();
            } else if (field.getName().equalsIgnoreCase("Team ID")) {
                teamId = field.getValue();
            } else if (field.getName().equalsIgnoreCase("Team")) {
                Arrays.stream(Objects.requireNonNull(field.getValue()).replace("Captain: ", "")
                        .replace("\nPlayers: ", " ")
                        .split(" ")).toList()
                        .forEach(id -> players.add(new Player(Long.parseLong(id.replaceAll("[<@>]", "")))));
            }
        }

        assert teamId != null;
        return new Team(players.get(0), players.subList(1, players.size()), teamName, teamId);
    }
}
