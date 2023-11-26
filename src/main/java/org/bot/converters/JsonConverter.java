package org.bot.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.utils.FileUpload;
import org.bot.models.Player;
import org.bot.models.PlayerDTO;
import org.bot.models.Team;
import org.bot.models.TeamDTO;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.IntSupplier;

public class JsonConverter {
    private final String TEMPLATE_NAME = "team-template.json";

    public Team jsonToTeam(String json) throws IOException {
        Gson gson = new Gson();

        TeamDTO teamDTO = gson.fromJson(json, TeamDTO.class);

        List<Player> players = new ArrayList<>();

        for (PlayerDTO playerDTO : teamDTO.getPlayers()) {
            players.add(new Player(playerDTO.getDiscordId(), playerDTO.getSlapId()));
        }

        Player captain = new Player(teamDTO.getCaptain().getDiscordId(), teamDTO.getCaptain().getSlapId());

        return new Team(captain, players, teamDTO.getTeamName(), teamDTO.getTeamId());
    }

    public FileUpload generateTemplateJson() {
        PlayerDTO playerDTO = new PlayerDTO(0L,0);

        List<PlayerDTO> playerDTOs = new ArrayList<>();
        playerDTOs.add(playerDTO);
        playerDTOs.add(playerDTO);

        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setTeamName("Demo Team Name");
        teamDTO.setTeamId("DTN");
        teamDTO.setCaptain(playerDTO);
        teamDTO.setPlayers(playerDTOs);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(teamDTO);

        return FileUpload.fromData(new ByteArrayInputStream(json.getBytes()), TEMPLATE_NAME);
    }

    public FileUpload getTeamsToJson() {
        try {
            Database database = new Database();

            Iterable<Team> teams = database.getTakenTeamModels();
            List<TeamDTO> teamDTOS = new ArrayList<>();

            for (Team team : teams) {
                TeamDTO teamDTO = new TeamDTO();
                teamDTO.setTeamName(team.name());
                teamDTO.setTeamId(team.nameAbbr());
                PlayerDTO captain = new PlayerDTO(team.captain().discordId(),
                        team.captain().slapId().orElse(0)
                );
                teamDTO.setCaptain(captain);
                teamDTO.setLeague(team.league().label);

                List<PlayerDTO> players = playerListToPlayerDtoList(team.players());
                teamDTO.setPlayers(players);
                teamDTOS.add(teamDTO);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(teamDTOS);

            return FileUpload.fromData(new ByteArrayInputStream(json.getBytes()), "teams.json");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public FileUpload getFreeAgentsToJson() {
        try {
            Database database = new Database();

            List<Player> freeAgents = database.getFreeAgents();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(playerListToPlayerDtoList(freeAgents));

            return FileUpload.fromData(new ByteArrayInputStream(json.getBytes()), "freeAgents.json");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<PlayerDTO> playerListToPlayerDtoList(List<Player> players) {
        List<PlayerDTO> playerDtos = new ArrayList<>();
        for (Player player : players) {
            PlayerDTO playerDTO = new PlayerDTO();
            if (player.league() != null) {
                playerDTO.setLeague(player.league().label);
            }
            playerDTO.setDiscordId(player.discordId());
            playerDTO.setSlapId(player.slapId().orElse(0));
            playerDtos.add(playerDTO);
        }

        return playerDtos;
    }
}
