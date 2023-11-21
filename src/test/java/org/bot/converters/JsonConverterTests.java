package org.bot.converters;

import org.bot.models.Player;
import org.bot.models.Team;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonConverterTests {
    @Test
    void jsonToTeamTest_validTeam() throws IOException {
        Player captain = new Player(1L, 2);
        List<Player> players = new ArrayList<>();
        Player player = new Player(2L, 0);
        players.add(player);
        players.add(player);
        Team expectedTeam = new Team(captain, players, "Team Name", "TN");


        StringBuilder json = new StringBuilder();
        json.append("{ \"teamName\": \"").append(expectedTeam.name()).append("\", \"teamId\": \"")
                .append(expectedTeam.nameAbbr()).append("\", \"captain\": { \"discordId\": 1, \"slapId\": 2 }, ")
                .append("\"players\": [ { \"discordId\": 2, \"slapId\": 0 }, { \"discordId\": 2, \"slapId\": 0 } ] }");

        JsonConverter jsonConverter = new JsonConverter();

        Team actual = jsonConverter.jsonToTeam(json.toString());

        assertEquals(expectedTeam, actual);
    }
}
