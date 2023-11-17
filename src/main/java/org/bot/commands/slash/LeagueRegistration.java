package org.bot.commands.slash;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.bot.converters.Database;
import org.bot.scripts.CommandLogger;
import org.bot.scripts.ReplyEphemeral;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LeagueRegistration {
    private final int MESSAGE_TIMEOUT = 10;
    private final Database DATABASE = new Database();
    private ReplyEphemeral replyEphemeral;
    private SlashCommandInteractionEvent event;

    public LeagueRegistration(SlashCommandInteractionEvent event) {
        this.replyEphemeral = new ReplyEphemeral(event);
        this.event = event;
    }

    public void registerFreeAgentEvent() {
        try {
            if (DATABASE.isFreeAgent(event.getUser().getIdLong())) {
                replyEphemeral.sendThenDelete(
                        "You are already a Free Agent contact a **League Coordinator** if you believe this to be wrong",
                        MESSAGE_TIMEOUT, TimeUnit.SECONDS
                );
                return;
            }
            if (DATABASE.isInTeam(event.getUser().getIdLong())) {
                replyEphemeral.sendThenDelete(
                        "You can't be a Free Agent while in a League Team, contact a **League Coordinator** if you believe this to be wrong",
                        MESSAGE_TIMEOUT, TimeUnit.SECONDS
                );
                return;
            }
        } catch (SQLException e) {
            log.error(e.getLocalizedMessage());
        }

        TextInput confirm = TextInput.create("confirm", "Confirmation", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("Type `confirm` if you have read and agree to #rules")
                .build();

        Modal modal = Modal.create("confirm-fa", "Confirm Free Agent Registration")
                .addActionRow(confirm)
                .build();

        event.replyModal(modal).queue();
    }

    public void registerTeamEvent() {
        Database database = new Database();
        replyEphemeral = new ReplyEphemeral(event);
        List<OptionMapping> players = event.getOptionsByType(OptionType.USER);
        StringBuilder sb = new StringBuilder();

        try {
            if (database.isInTeam(event.getUser().getIdLong())) {
                replyEphemeral.sendThenDelete(
                        "You are already in a team.",
                        MESSAGE_TIMEOUT, TimeUnit.SECONDS
                );
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (OptionMapping player:players) {
            if (player.getAsUser().isBot()) {
                replyEphemeral.sendThenDelete(
                        "Players must not be a Bot",
                        MESSAGE_TIMEOUT, TimeUnit.SECONDS
                );
                return;
            }
            if (players.stream().filter(p->p.getAsUser().equals(player.getAsUser())).count() > 1) {
                replyEphemeral.sendThenDelete(
                        "Do not repeat players",
                        MESSAGE_TIMEOUT, TimeUnit.SECONDS
                );
                return;
            }
            if (players.stream().filter(p->p.getAsUser().equals(event.getUser())).count() > 1) {
                replyEphemeral.sendThenDelete(
                        "Do not include yourself as a player, you are automatically included",
                        MESSAGE_TIMEOUT, TimeUnit.SECONDS
                );
                return;
            }

            try {
                if (database.isInTeam(player.getAsUser().getIdLong())) {
                    sb.append(player.getAsUser().getAsMention()).append(System.lineSeparator());
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (!sb.toString().isBlank()) {
            replyEphemeral.sendThenDelete("Team cannot be made. The following Players are already in a team:\n" +
                    sb.toString(), 30, TimeUnit.SECONDS);
            return;
        }

        List<HashMap<String, String>> teams;
        try {
            teams = database.getTeamsNotTaken();

            StringSelectMenu.Builder teamsSelectMenu = StringSelectMenu.create("choose-team")
                    .addOption("Create New Team", "new");

            for (HashMap<String, String> team: teams) {
                teamsSelectMenu.addOption(team.get("teamName"), team.get("teamID").toLowerCase());
            }

            replyEphemeral.sendEmbedThenDelete(new EmbedBuilder()
                            .setTitle("Select Team you wish to form")
                            .addField("Team", getTeamsMessage(event.getUser(), event.getOptionsByType(OptionType.USER)), true)
                            .setFooter("This message will be deleted after 5 minutes if there is no team selected.")
                            .build(),
                    5, TimeUnit.MINUTES, teamsSelectMenu.build()
            );
        } catch (SQLException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    private String getTeamsMessage(User captain, List<OptionMapping> players) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Captain: ")
                .append(captain.getAsMention())
                .append("\nPlayers: ");
        for (OptionMapping player: players) {
            stringBuilder.append(player.getAsUser().getAsMention())
                    .append(" ");
        }

        return stringBuilder.toString();
    }
}
