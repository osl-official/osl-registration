package org.bot.scripts;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.bot.enums.TableName;
import org.bot.models.Setting;
import org.bot.models.entity.FreeAgent;
import org.bot.models.entity.Player;
import org.bot.models.entity.Team;
import org.bot.models.entity.TeamPlayer;
import org.bot.service.FreeAgentService;
import org.bot.service.PlayerService;
import org.bot.service.TeamPlayerService;
import org.bot.service.TeamService;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

@Slf4j
public class DatabaseEmbed {
    private final Setting setting;
    private final TableName tableName;
    private final int page;
    private final int itemsPerPage = 10;

    public DatabaseEmbed(Setting setting) {
        this.setting = setting;
        this.tableName = TableName.values()[0];
        this.page = 1;
    }

    public DatabaseEmbed(Setting setting, TableName tableName) {
        this.setting = setting;
        this.tableName = tableName;
        this.page = 1;
    }

    public DatabaseEmbed(Setting setting, TableName tableName, int page) {
        this.setting = setting;
        this.tableName = tableName;
        this.page = page;
    }

    public MessageEmbed getDatabaseEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Database Table Viewer")
                .setColor(Color.magenta)
                .addField("Table Name", tableName.label, true)
                .addField("Page Number", String.valueOf(page), true)
                .addBlankField(false);

        if (tableName.label.equalsIgnoreCase("Players") || tableName.label.equalsIgnoreCase("Teams")) {
            eb.setFooter(tableName + " contains primary key values which might be referenced in other tables. " +
                    "Be careful before deleting rows in this table. Errors are likely to occur.");
        }

        List<?> columns = new ArrayList<>();

        switch (tableName) {
            case PLAYER -> {
                PlayerService service = new PlayerService(setting);
                List<Player> allPlayers = service.findAll();
                int fromIndex = Math.max(0, (page - 1) * itemsPerPage);
                int toIndex = Math.min(page * itemsPerPage, allPlayers.size());
                columns = allPlayers.subList(fromIndex, toIndex);
            }
            case FREEAGENT -> {
                FreeAgentService service = new FreeAgentService(setting);
                List<FreeAgent> allFreeAgents = service.findAll();
                int fromIndex = Math.max(0, (page - 1) * itemsPerPage);
                int toIndex = Math.min(page * itemsPerPage, allFreeAgents.size());
                columns = allFreeAgents.subList(fromIndex, toIndex);
            }
            case TEAMPLAYER -> {
                TeamPlayerService service = new TeamPlayerService(setting);
                List<TeamPlayer> allTeamPlayers = service.findAll();
                int fromIndex = Math.max(0, (page - 1) * itemsPerPage);
                int toIndex = Math.min(page * itemsPerPage, allTeamPlayers.size());
                columns = allTeamPlayers.subList(fromIndex, toIndex);
            }
            case TEAM -> {
                TeamService service = new TeamService(setting);
                List<Team> allTeams = service.findAll();

                allTeams.forEach(System.out::println);

                int fromIndex = Math.max(0, (page - 1) * itemsPerPage);
                int toIndex = Math.min(page * itemsPerPage, allTeams.size());
                columns = allTeams.subList(fromIndex, toIndex);
            }
        }

        if (columns.isEmpty()) {
            eb.addField("Empty Table", tableName + " contains no entries.", true);
        } else {
            List<String> fields = new ArrayList<>();
            for (Field field : columns.get(0).getClass().getDeclaredFields()) {
                fields.add(field.getName());
            }

            for (String fieldName : fields) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < columns.size(); i++) {
                    try {
                        Field field = columns.get(i).getClass().getDeclaredField(fieldName);
                        field.setAccessible(true);

                        if (columns.get(i).getClass() == Player.class) {
                            Player player = (Player) columns.get(i);
                            stringBuilder.append(i)
                                    .append(". <@")
                                    .append(player.getDiscordId())
                                    .append(">")
                                    .append(System.lineSeparator());
                        } else if (columns.get(i).getClass() == Team.class) {
                            Team team = (Team) columns.get(i);
                            stringBuilder.append(i)
                                    .append(team.toString())
                                    .append(System.lineSeparator());
                        } else if (field.get(columns.get(i)).getClass() == Player.class) {
                            Player player = (Player) field.get(columns.get(i));
                            stringBuilder.append(i)
                                    .append(". <@")
                                    .append(player.getDiscordId())
                                    .append(">")
                                    .append(System.lineSeparator());
                        } else {
                            Object value = field.get(columns.get(i));
                            stringBuilder.append(i)
                                    .append(". ")
                                    .append(value != null ? value.toString() : "null")
                                    .append(System.lineSeparator());
                        }

                        field.setAccessible(false);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                eb.addField(fieldName, stringBuilder.toString(), true);
            }
        }
        return eb.build();
    }

    public StringSelectMenu getDeleteSelectMenu() {
        int length = getCount();

        StringSelectMenu.Builder deleteBuilder = StringSelectMenu.create("delete-row");
        for (int i = 0; i < length; i++) {
            String value = String.valueOf(i + 1);
            deleteBuilder.addOption(value, value, Emoji.fromUnicode("U+274C"));
        }
        if (length == 0) {
            deleteBuilder.addOption("null", "null");
            deleteBuilder.setDisabled(true);
        } else {
            deleteBuilder.setPlaceholder("Select index to delete");
        }

        return deleteBuilder.build();
    }

    public StringSelectMenu getTableSelectMenu() {
        TableName[] tableNames = TableName.values();

        StringSelectMenu.Builder tablesBuilder = StringSelectMenu.create("table-names");
        Arrays.stream(tableNames).toList()
                .forEach(name -> tablesBuilder.addOption(name.label, name.label.toLowerCase()));

        tablesBuilder.getOptions().forEach(selectOption -> {
            if (selectOption.getLabel().equalsIgnoreCase(tableName.label)) {
                tablesBuilder.setDefaultOptions(selectOption);
            }
        });

        return tablesBuilder.build();
    }

    public ActionRow getPageButtons() {
        Button prevBtn = Button
                .primary("page-prev", "10")
                .withEmoji(Emoji.fromUnicode("U+2B05"));
        Button nextBtn = Button
                .primary("page-next", "10")
                .withEmoji(Emoji.fromUnicode("U+27A1"));

        if (page == 1) {
            prevBtn = prevBtn.asDisabled();
        }

        int count = getCount();

        if (count - page * 10 <= 0) {
            nextBtn = nextBtn.asDisabled();
        }

        return ActionRow.of(prevBtn, nextBtn);
    }

    private int getCount() {
        int count = 0;

        switch (tableName) {
            case PLAYER -> {
                PlayerService service = new PlayerService(setting);
                count = service.findAll().size();
            }
            case FREEAGENT -> {
                FreeAgentService service = new FreeAgentService(setting);
                count = service.findAll().size();
            }
            case TEAMPLAYER -> {
                TeamPlayerService service = new TeamPlayerService(setting);
                count = service.findAll().size();
            }
            case TEAM -> {
                TeamService service = new TeamService(setting);
                count = service.findAll().size();
            }
        }
        return count;
    }
}
