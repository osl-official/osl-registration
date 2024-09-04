package org.bot.converters;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bot.enums.League;
import org.bot.models.entity.Team;
import org.bot.models.entity.Player;
import org.bot.models.entity.TeamPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class EmbedConverter {
    private MessageEmbed messageEmbed;

    public Team getTeamFromEmbed() {
        Team team = new Team();

        for (MessageEmbed.Field field : messageEmbed.getFields()) {
            if (Objects.requireNonNull(field.getName()).equalsIgnoreCase("Team Name")) {
                team.setTeamName(field.getValue());
            } else if (field.getName().equalsIgnoreCase("Team ID")) {
                team.setTeamID(field.getValue());
            }
        }

        return team;
    }

    public List<TeamPlayer> getPlayersFromEmbed() {
        List<TeamPlayer> teamPlayers = new ArrayList<>();

        Team team = getTeamFromEmbed();

        for (MessageEmbed.Field field : messageEmbed.getFields()) {
            if (field.getName().equalsIgnoreCase("Team")) {
                List<String> playerIds = Arrays.stream(Objects.requireNonNull(field.getValue()).replace("Captain: ", "")
                                .replace("\nPlayers: ", " ")
                                .split(" ")).toList();

                for (int i = 0; i < playerIds.size(); i++) {
                    TeamPlayer teamPlayer = new TeamPlayer();
                    teamPlayer.setPlayer(new Player(Long.parseLong(playerIds.get(i).replaceAll("[<@>]", ""))));
                    teamPlayer.setTeam(team);
                    teamPlayer.setCaptain(i == 0);
                }
            }
        }

        return teamPlayers;
    }
}
