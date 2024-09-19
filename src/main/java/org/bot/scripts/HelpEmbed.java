package org.bot.scripts;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;

@AllArgsConstructor
@Slf4j
public class HelpEmbed {
    private final String[][] HELP_SECTIONS = {{"Registration", "U+1F4DD"}, {"Admin", "U+1F916"}, {"Report a Bug", "U+1F41E"}};

    private int helpIdx;

    public HelpEmbed() {
        this.helpIdx = 0;
    }

    public MessageEmbed getHelpMessageEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("League Registration Help - " + HELP_SECTIONS[helpIdx][0])
                .setColor(Color.green);

        switch (HELP_SECTIONS[helpIdx][0]) {
            case "Registration" -> eb.setDescription(getRegistrationBody());
            case "Admin" -> eb.setDescription(getAdminBody());
            case "Report a Bug" -> eb.setDescription(getReportBugBody());
        }

        return eb.build();
    }

    private String getRegistrationBody() {
        String[][] commands = {
                {"register-fa", "Register to become a Free Agent. Have a read of <#980299028464013322> before hand."},
                {"register-team",
                        "Register a Team for the current season." + System.lineSeparator() + "*Note:*" + System.lineSeparator() +
                                "```• You will automatically be assigned as the Captain." + System.lineSeparator() +
                                "• Player1 and Player2 are required." + System.lineSeparator() +
                                "• Player3 and Player4 are option." + System.lineSeparator() +
                                "• Do not include yourself as a player you are automatically listed as one.```" + System.lineSeparator() +
                                "If you opt to create a new Team please note" + System.lineSeparator() +
                                "```• Team ID must be no more then 4 characters long and must be unique." + System.lineSeparator() +
                                "• Team Name must be 5-50 characters long and also must be unique.```" + System.lineSeparator()
                }
        };

        StringBuilder sb = new StringBuilder();
        for (String[] command: commands) {
            sb.append(System.lineSeparator())
                    .append("**")
                    .append(command[0])
                    .append("**")
                    .append(System.lineSeparator())
                    .append(command[1])
                    .append(System.lineSeparator());
        }
        return sb.toString();
    }

    private String getAdminBody() {
        String[][] commands = {
                {"remove-fa", "Select a user to remove as a Free Agent."},
                {"add-fa", "Select a user to add as a Free Agent."},
                {"disband-team",
                        "Select a Team to disband via mentioning its Role." + System.lineSeparator() +
                                "```" +
                                "• Setting a team as assignable will allow new team registrations to select this team instead of creating a new team." + System.lineSeparator() +
                                "• Setting delete-role to True will delete the role from the server. Note: If you are deleting Donda but there is a team called Donda 2, the role for Donda 2 is at risk of being deleted." + System.lineSeparator() +
                                "• Setting delete-channels will delete the Text and Voice channels. Note: If you are deleting Donda but there is a team called Donda 2, the channels for Donda 2 are at risk of being deleted." + System.lineSeparator() +
                                "```"
                },
                {"create-team", "Creates an empty team in the database for users to register with."},
                {"refresh-roster", "Refreshes the roster embeds according to what is stored in the database."},
                {"teams-json", "Provides a JSON file with a list of all teams for easy viewing."},
                {"free-agents-json", "Provides a JSON file with a list of all free agents for easy viewing."},
                {"view-database", "Shows an embed of each table in the database, allowing you to directly delete rows from the database."}
        };

        StringBuilder sb = new StringBuilder();
        for (String[] command: commands) {
            sb.append(System.lineSeparator())
                    .append("**")
                    .append(command[0])
                    .append("**")
                    .append(System.lineSeparator())
                    .append(command[1])
                    .append(System.lineSeparator());
        }
        return sb.toString();
    }

    private String getReportBugBody() {
        return "If you encounter a bug while using the bot. " +
                "Please send any screenshots as well as the command used to <@257439788209143808>, along with a timestamp so we can check the error logs! " +
                System.lineSeparator() + System.lineSeparator() + "**Have a feature request or an idea?**" + System.lineSeparator() +
                "Leave a suggestion in <#1019787788478074911> and tag <@516995472561405966> (He loves being tagged!).";
    }

    public StringSelectMenu getSelectMenu() {
        StringSelectMenu.Builder selectBuilder = StringSelectMenu.create("help-options");
        for (int i = 0; i < HELP_SECTIONS.length; i++) {
            String[] helpSection = HELP_SECTIONS[i];
            selectBuilder.addOption(helpSection[0], String.valueOf(i), Emoji.fromUnicode(helpSection[1]));
        }

        selectBuilder.getOptions().forEach(selectOption -> {
            if (selectOption.getLabel().equalsIgnoreCase(HELP_SECTIONS[helpIdx][0])) {
                selectBuilder.setDefaultOptions(selectOption);
            }
        });

        return selectBuilder.build();
    }
}
