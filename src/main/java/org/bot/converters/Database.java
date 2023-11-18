package org.bot.converters;

import lombok.extern.slf4j.Slf4j;
import org.bot.models.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalInt;

@Slf4j
public class Database {

    private final String DATABASE_URL = "jdbc:sqlite:src/main/resources/teams.db";

    /**
     * Encloses a string in single quotations for SQL queries.
     * Example: toSqlString("My Team Name") returns "'My Team Name'".
     * @param text the input string
     * @return the input string enclosed in single quotations
     */
    private String toSqlString(String text) {
        return "'" + text + "'";
    }

    public List<HashMap<String, String>> getTeamsNotTaken() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        List<HashMap<String, String>> teamsNotTaken = new ArrayList<>();

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT * FROM Teams WHERE captainID IS NULL");
        while (resultSet.next()) {
            HashMap<String, String> teamNotTaken = new HashMap<>();
            teamNotTaken.put("teamName", resultSet.getString("teamName"));
            teamNotTaken.put("teamID", resultSet.getString("teamID"));
            teamsNotTaken.add(teamNotTaken);
        }
        connection.close();
        return teamsNotTaken;
    }

    public List<HashMap<String, String>> getTeamsTaken() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        List<HashMap<String, String>> teamsTaken = new ArrayList<>();

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT * FROM Teams WHERE captainID NOT NULL");
        while (resultSet.next()) {
            HashMap<String, String> teamTaken = new HashMap<>();
            teamTaken.put("teamName", resultSet.getString("teamName"));
            teamTaken.put("teamID", resultSet.getString("teamID"));
            teamsTaken.add(teamTaken);
        }
        connection.close();
        return teamsTaken;
    }

    public void setTeamTaken(String teamID, long captainID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Teams " +
                "SET captainID = ? " +
                "WHERE teamID = ?");
        preparedStatement.setLong(1, captainID);
        preparedStatement.setString(2, toSqlString(teamID.toUpperCase()));
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public void setTeamNotTaken(String teamID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Teams " +
                "SET captainID = NULL " +
                "WHERE teamID = " + toSqlString(teamID.toUpperCase()));
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public void addNewTeam(String teamID, String teamName) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Teams " +
                "VALUES (?,?,NULL)");
        preparedStatement.setString(1, teamID.toUpperCase());
        preparedStatement.setString(2, teamName);
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public void addFreeAgent(long discordID) throws SQLException {
        if (getPlayer(discordID).isNull()) {
            addPlayer(discordID);
        }

        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO FreeAgents VALUES (?)");
        preparedStatement.setLong(1, discordID);
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();

    }

    public void removePlayerFromTeam(long discordID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM TeamPlayers WHERE discordID = ?");
        preparedStatement.setLong(1, discordID);
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public void disbandTeam(String teamID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM TeamPlayers WHERE teamID = " +
                toSqlString(teamID.toUpperCase()));
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public void removeFreeAgent(long discordID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM FreeAgents WHERE discordID = ?");
        preparedStatement.setLong(1, discordID);
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public void addPlayer(long discordID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Players " +
                "VALUES (?, NULL)");
        preparedStatement.setLong(1, discordID);
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public void addPlayerToTeam(long discordID, String teamID) throws SQLException {
        if (getPlayer(discordID).isNull()) {
            addPlayer(discordID);
        }

        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO TeamPlayers " +
                "VALUES (?,?)");
        preparedStatement.setString(1, teamID.toUpperCase());
        preparedStatement.setLong(2, discordID);
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public void addCaptain(long discordID, String teamID) throws SQLException {
        if (getPlayer(discordID).isNull()) {
            addPlayer(discordID);
        }

        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE Teams SET captainID = ? WHERE teamID = " +  toSqlString(teamID.toUpperCase()));
        preparedStatement.setLong(1, discordID);

        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public Player getPlayer(long discordID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        Player player = new Player();

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT * FROM Players WHERE discordID = " + discordID);

        if(resultSet.next()) {
            OptionalInt slapID = resultSet.getObject("slapID") != null ? OptionalInt.of(resultSet.getInt("slapID")) : OptionalInt.empty();
            player = new Player(discordID, slapID);
        }
        connection.close();
        return player;
    }

    public List<Player> getTeamPlayers(String teamID) throws SQLException {
        List<Player> players = new ArrayList<>();
        Connection connection = DriverManager.getConnection(DATABASE_URL);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT * FROM TeamPlayers WHERE teamID = " + toSqlString(teamID.toUpperCase()));

        while (resultSet.next()) {
            players.add(new Player(
                    resultSet.getLong("discordID")
            ));
        }
        connection.close();

        return players;
    }

    public Player getCaptain(String teamID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        Player captain = new Player();

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT * FROM Teams WHERE teamID = " +  toSqlString(teamID.toUpperCase()));


        while (resultSet.next()) {
            captain = new Player(
                    resultSet.getLong("captainID")
            );
        }
        connection.close();

        return captain;
    }

    public boolean doesTeamNameExist(String teamName) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        boolean exist;

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT * FROM Teams WHERE teamName = " + toSqlString(teamName));

        exist = resultSet.next();

        connection.close();
        return exist;
    }

    public boolean doesTeamIdExist(String teamID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        boolean exist;

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT * FROM Teams WHERE teamID = " + toSqlString(teamID.toUpperCase()));

        exist = resultSet.next();

        connection.close();
        return exist;
    }

    public boolean isFreeAgent(long discordID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT * FROM FreeAgents WHERE discordID = " + discordID);
        boolean isFreeAgent = resultSet.next();

        connection.close();

        return isFreeAgent;
    }

    public boolean isInTeam(long discordID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        boolean isInTeam = statement.executeQuery("SELECT * FROM TeamPlayers WHERE discordID = " + discordID).next();

        boolean isCaptain = statement.executeQuery("SELECT * FROM Teams WHERE captainID = " + discordID).next();

        connection.close();

        return isInTeam || isCaptain;
    }
}
