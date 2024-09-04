//package org.bot.converters;
//
//import org.bot.enums.League;
//import org.bot.models.entity.Player;
//import org.bot.models.Setting;
//import org.bot.models.entity.Team;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.ArrayList;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//@Component
//public class JsonConverterTests {
//    private final Setting setting;
//
//    @Autowired
//    public JsonConverterTests(Setting setting) {
//        this.setting = setting;
//    }
//
//    @Test
//    void jsonToTeamTest_validTeamJson() throws IOException {
//        Player captain = new Player(1L);
//        List<Player> players = new ArrayList<>();
//        Player player = new Player(2L);
//        players.add(player);
//        players.add(player);
//        Team expectedTeam = new Team("Team Name", "TN", League.PRO);
//
//
//        StringBuilder json = new StringBuilder();
//        json.append("{ \"teamName\": \"").append(expectedTeam.getTeamName()).append("\", \"teamId\": \"")
//                .append(expectedTeam.getTeamID()).append("\", \"captain\": { \"discordId\": 1 }, ")
//                .append("\"players\": [ { \"discordId\": 2 }, { \"discordId\": 2 } ] }");
//
//        JsonConverter jsonConverter = new JsonConverter(setting);
//
//        Team actual = jsonConverter.jsonToTeam(json.toString());
//
//        assertEquals(expectedTeam, actual);
//    }
//
//    @Test
//    void jsonToTeamTest_invalidTeamJson() throws IOException {
//        Player captain = new Player(1L);
//        List<Player> players = new ArrayList<>();
//        Player player = new Player(2L);
//        players.add(player);
//        players.add(player);
//        Team expectedTeam = new Team("Team Name", "TN", League.PRO);
//
//
//        StringBuilder json = new StringBuilder();
//        json.append("{ \"teamName\": \"No").append("\", \"teamId\": \"")
//                .append(expectedTeam.getTeamID()).append("\", \"captain\": { \"discordId\": 1, \"slapId\": 2 }, ")
//                .append("\"players\": [ { \"discordId\": 2, \"slapId\": 0 }, { \"discordId\": 2, \"slapId\": 0 } ] }");
//
//        JsonConverter jsonConverter = new JsonConverter(setting);
//
//        assertThrows(IllegalArgumentException.class, () -> jsonConverter.jsonToTeam(json.toString()).getClass());
//    }
//}
