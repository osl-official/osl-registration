package org.bot.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.utils.FileUpload;
import org.bot.enums.League;
import org.bot.models.*;
import org.bot.models.entity.FreeAgent;
import org.bot.models.entity.Player;
import org.bot.models.entity.Team;
import org.bot.models.entity.TeamPlayer;
import org.bot.service.FreeAgentService;
import org.bot.service.TeamPlayerService;
import org.bot.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class JsonConverter {
    private final Setting setting;
    private final String TEMPLATE_NAME = "team-template.json";

    @Autowired
    public JsonConverter(Setting setting) {
        this.setting = setting;
    }

    public Team jsonToTeam(String json) throws IOException {
        Gson gson = new Gson();

        TeamDTO teamDTO = gson.fromJson(json, TeamDTO.class);

        return new Team(teamDTO.getTeamId(), teamDTO.getTeamName(), League.valueOf(teamDTO.getLeague().toUpperCase()));
    }

    public List<TeamPlayer> jsonToPlayers(String json) throws IOException {
        Gson gson = new Gson();
        Team team = jsonToTeam(json);

        TeamDTO teamDTO = gson.fromJson(json, TeamDTO.class);

        List<Player> players = new ArrayList<>();

        for (PlayerDTO playerDTO : teamDTO.getPlayers()) {
            players.add(new Player(playerDTO.getDiscordId()));
        }

        Player playerCaptain = new Player(teamDTO.getCaptain().getDiscordId());
        TeamPlayer captain = new TeamPlayer();
        captain.setPlayer(playerCaptain);
        captain.setCaptain(true);
        captain.setTeam(team);

        List<TeamPlayer> teamPlayers = new ArrayList<>();
        teamPlayers.add(captain);
        players.forEach(p -> {
            TeamPlayer teamPlayer = new TeamPlayer();
            teamPlayer.setTeam(team);
            teamPlayer.setCaptain(false);
            teamPlayer.setPlayer(p);

            teamPlayers.add(teamPlayer);
        });

        return teamPlayers;
    }

    public FileUpload generateTemplateJson() {
        PlayerDTO playerDTO = new PlayerDTO(0L,null);

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
        TeamService teamService = new TeamService(setting);
        TeamPlayerService teamPlayerService = new TeamPlayerService(setting);
        List<Team> teams = teamService.findAllByLeagueIsNotNull();
        List<TeamDTO> teamDTOS = new ArrayList<>();

        for (Team team : teams) {
            List<TeamPlayer> teamPlayers = teamPlayerService.findAllByTeamId(team.getTeamID()).get();

            TeamDTO teamDTO = new TeamDTO();
            teamDTO.setTeamName(team.getTeamName());
            teamDTO.setTeamId(team.getTeamID());
            teamDTO.setLeague(team.getLeague().label);

            for (TeamPlayer teamPlayer : teamPlayers) {
                if (teamPlayer.isCaptain()) {
                    teamDTO.setCaptain(new PlayerDTO(teamPlayer.getPlayer().getDiscordId()));
                }
            }

            List<PlayerDTO> players = playerListToPlayerDtoList(teamPlayers);
            teamDTO.setPlayers(players);
            teamDTOS.add(teamDTO);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(teamDTOS);

        return FileUpload.fromData(new ByteArrayInputStream(json.getBytes()), "teams.json");
    }

    public FileUpload getFreeAgentsToJson() {
        FreeAgentService freeAgentService = new FreeAgentService(setting);

        List<FreeAgent> freeAgents = freeAgentService.findAll();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(freeAgentListToPlayerDtoList(freeAgents));

        return FileUpload.fromData(new ByteArrayInputStream(json.getBytes()), "freeAgents.json");
    }

    private List<PlayerDTO> playerListToPlayerDtoList(List<TeamPlayer> players) {
        List<PlayerDTO> playerDtos = new ArrayList<>();
        players.stream()
                .filter(p -> !p.isCaptain())
                .forEach(p -> {
                    playerDtos.add(new PlayerDTO(p.getPlayer().getDiscordId()));
                });

        return playerDtos;
    }

    private List<PlayerDTO> freeAgentListToPlayerDtoList(List<FreeAgent> players) {
        List<PlayerDTO> playerDtos = new ArrayList<>();
        players
                .forEach(p -> {
                    playerDtos.add(new PlayerDTO(p.getPlayer().getDiscordId()));
                });

        return playerDtos;
    }
}
