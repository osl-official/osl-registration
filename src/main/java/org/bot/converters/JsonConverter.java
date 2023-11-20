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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;

public class JsonConverter {
    private final String TEMPLATE_NAME = "team-template.json";

    public Team jsonToTeam(String json) throws IOException {
        Gson gson = new Gson();

        TeamDTO teamDTO = gson.fromJson(json, TeamDTO.class);

        List<Player> players = new ArrayList<>();

        for (PlayerDTO playerDTO : teamDTO.getPlayers()) {
            players.add(new Player(playerDTO.getDiscordId(), OptionalInt.of(playerDTO.getSlapId())));
        }

        Player captain = new Player(teamDTO.getCaptain().getDiscordId(), OptionalInt.of(teamDTO.getCaptain().getSlapId()));

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
}
