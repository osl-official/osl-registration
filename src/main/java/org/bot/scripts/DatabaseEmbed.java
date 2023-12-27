package org.bot.scripts;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.bot.converters.Database;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.sql.SQLException;
import java.util.Map;

@AllArgsConstructor
@Slf4j
public class DatabaseEmbed {
    private String tableName;
    private int page;

    public DatabaseEmbed() {
        try {
            Database database = new Database();
            this.tableName = database.getTableNames().get(0);
            this.page = 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public DatabaseEmbed(String tableName) {
        this.tableName = tableName;
        this.page = 1;
    }

    public MessageEmbed getDatabaseEmbed() {
        try {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Database Table Viewer")
                    .setColor(Color.magenta)
                    .addField("Table Name", tableName, true)
                    .addField("Page Number", String.valueOf(page), true)
                    .addBlankField(false);

            if (tableName.equalsIgnoreCase("Players") || tableName.equalsIgnoreCase("Teams")) {
                eb.setFooter(tableName + " contains primary key values which might be referenced in other tables. " +
                        "Be careful before deleting rows in this table");
            }

            Database database = new Database();
            HashMap<String,List<String>> columns = database.getTable(tableName, page);
            if (columns.isEmpty()) {
                eb.addField("Empty Table", tableName + " contains no entries.", true);
            } else {
                for (Map.Entry<String, List<String>> entry : columns.entrySet()) {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();

                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < values.size(); i++) {
                        stringBuilder.append(i)
                                .append(". ")
                                .append(values.get(i))
                                .append(System.lineSeparator());
                    }
                    eb.addField(key, stringBuilder.toString(), true);
                }
            }

            return eb.build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public StringSelectMenu getDeleteSelectMenu() {
        try {
            Database database = new Database();
            int length = database.getEntryLength(tableName);

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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public StringSelectMenu getTableSelectMenu() {
        try {
            Database database = new Database();
            List<String> tableNames = database.getTableNames();

            StringSelectMenu.Builder tablesBuilder = StringSelectMenu.create("table-names");
            tableNames.forEach(name -> tablesBuilder.addOption(name, name.toLowerCase()));

            tablesBuilder.getOptions().forEach(selectOption -> {
                if (selectOption.getLabel().equalsIgnoreCase(tableName)) {
                    tablesBuilder.setDefaultOptions(selectOption);
                }
            });

            return tablesBuilder.build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ActionRow getPageButtons() {
        try {
            Button prevBtn = Button
                    .primary("page-prev", "10")
                    .withEmoji(Emoji.fromUnicode("U+2B05"));
            Button nextBtn = Button
                    .primary("page-next", "10")
                    .withEmoji(Emoji.fromUnicode("U+27A1"));

            if (page == 1) {
                prevBtn = prevBtn.asDisabled();
            }

            Database database = new Database();
            int count = database.getEntryLength(tableName);

            if (count - page * 10 <= 0) {
                nextBtn = nextBtn.asDisabled();
            }

            return ActionRow.of(prevBtn, nextBtn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
