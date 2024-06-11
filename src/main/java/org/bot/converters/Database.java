package org.bot.converters;

import lombok.extern.slf4j.Slf4j;
import org.bot.enums.League;
import org.bot.models.Player;
import org.bot.models.Team;

import java.sql.*;
import java.util.*;

@Slf4j
public class Database {
    private final String DATABASE_PATH = new Config().getDatabasePath();
    private final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_PATH;

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
            teamTaken.put("league", resultSet.getString("league"));
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
                "SET captainID = NULL, league = NULL " +
                "WHERE teamID = " + toSqlString(teamID.toUpperCase()));
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public void addNewTeam(String teamID, String teamName) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Teams " +
                "VALUES (?,?,NULL,NULL)");
        preparedStatement.setString(1, teamID.toUpperCase());
        preparedStatement.setString(2, teamName);
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public void addFreeAgent(long discordID, League league) throws SQLException {
        if (getPlayer(discordID).isNull()) {
            addPlayer(discordID);
        }

        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO FreeAgents VALUES (?, ?)");
        preparedStatement.setLong(1, discordID);
        preparedStatement.setString(2, league.label);
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

    public void setLeague(League league, String teamID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE Teams SET league = ? WHERE teamID = " +  toSqlString(teamID.toUpperCase()));
        preparedStatement.setString(1, league.label);

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
            player = new Player(discordID, slapID, null);
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

    public void deleteTeam(String teamID) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection
                .prepareStatement("DELETE FROM Teams WHERE teamID = " + toSqlString(teamID));
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public List<Player> getFreeAgents() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        List<Player> freeAgents = new ArrayList<>();

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT * FROM FreeAgents");


        while (resultSet.next()) {
            freeAgents.add(new Player(
                    resultSet.getLong("discordID"),
                    League.valueOf(resultSet.getString("league").toUpperCase())));
        }
        connection.close();

        return freeAgents;
    }

    public Iterable<Team> getTakenTeamModels() throws SQLException {
        List<HashMap<String, String>> takenTeams = getTeamsTaken();
        List<Team> teams = new ArrayList<>();

        for (HashMap<String, String> takenTeam : takenTeams) {
            List<Player> players = getTeamPlayers(takenTeam.get("teamID"));
            Player captain = getCaptain(takenTeam.get("teamID"));
            League league = League.valueOf(takenTeam.get("league").toUpperCase());
            teams.add(new Team(captain, players, takenTeam.get("teamName"), takenTeam.get("teamID"), league));
        }

        return teams;
    }

    public List<String> getTableNames() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        List<String> tables = new ArrayList<>();

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT\n" +
                "    name\n" +
                "FROM\n" +
                "    sqlite_schema\n" +
                "WHERE\n" +
                "        type ='table' AND\n" +
                "        name NOT LIKE 'sqlite_%';");


        while (resultSet.next()) {
            tables.add(resultSet.getString("name"));
        }
        connection.close();

        return tables;
    }

    public int getEntryLength(String table) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM  " + table);

        int count = 0;
        while (resultSet.next()) {
            count = resultSet.getInt(1);
        }
        connection.close();

        return count;
    }

    public HashMap<String, List<String>> getTable(String tableName, int page) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT * FROM  " + tableName
                + " LIMIT 10 OFFSET " + (page-1) * 10);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

        int columnCount = resultSetMetaData.getColumnCount();

        HashMap<String, List<String>> rows = new HashMap<>();

        while (resultSet.next()) {
            for (int i = 0; i < columnCount; i++) {
                rows.putIfAbsent(resultSetMetaData.getColumnName(i + 1), new ArrayList<>());
                if (resultSetMetaData.getColumnName(i + 1).equalsIgnoreCase("discordID") ||
                        resultSetMetaData.getColumnName(i + 1).equalsIgnoreCase("captainID")) {
                    rows.get(resultSetMetaData.getColumnName(i + 1))
                            .add("<@" + resultSet.getString(i + 1) + ">");
                } else {
                    rows.get(resultSetMetaData.getColumnName(i + 1))
                            .add(resultSet.getString(i + 1));
                }
            }
        }

        return rows;
    }

    public void deleteRow(String tableName, int rowIdx) throws SQLException {
        String whereClause = getDeleteRowWhereClause(tableName, rowIdx);
        String deleteQuery = "DELETE FROM " + tableName + " " + whereClause;
        System.out.println(deleteQuery);

        Connection connection = DriverManager.getConnection(DATABASE_URL);
        PreparedStatement preparedStatement = connection
                .prepareStatement(deleteQuery);
        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    private String getDeleteRowWhereClause(String tableName, int rowIdx) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        ResultSet resultSet = statement.executeQuery("SELECT * FROM  " + tableName
                + " LIMIT 1 OFFSET " + (rowIdx - 1));
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        StringBuilder whereClauseBuilder = new StringBuilder("WHERE ");

        while (resultSet.next()) {
            for (int i = 0; i < columnCount; i++) {
                String columnName = resultSetMetaData.getColumnName(i + 1);
                whereClauseBuilder.append(columnName)
                        .append(" = ");
                Object object = resultSet.getObject(i + 1);

                if (object == null) {
                    whereClauseBuilder.append(object)
                            .append(" IS NULL");
                } else if (object instanceof String) {
                    whereClauseBuilder.append("'")
                            .append(object)
                            .append("'");
                } else {
                    whereClauseBuilder.append(object);
                }

                if (i + 1 != columnCount) {
                    whereClauseBuilder.append(" AND ");
                }
            }
        }

        resultSet.close();
        connection.close();

        return whereClauseBuilder.toString();
    }
}
