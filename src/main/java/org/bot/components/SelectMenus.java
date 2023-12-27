package org.bot.components;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import org.bot.converters.Config;
import org.bot.converters.Database;
import org.bot.converters.EmbedConverter;
import org.bot.enums.League;
import org.bot.models.Player;
import org.bot.models.Team;
import org.bot.scripts.*;

import java.awt.Color;
import java.io.File;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SelectMenus extends ListenerAdapter {
    private final File APPROVED_IMG = new File("src/main/resources/images/approve.png");

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        CommandLogger commandLogger = new CommandLogger();
        switch (Objects.requireNonNull(event.getSelectMenu().getId())){
            case "choose-team" -> {
                if (event.getSelectedOptions().get(0).getValue().equalsIgnoreCase("NEW")) {
                    TextInput teamIdInput = TextInput.create("team-id", "Team ID", TextInputStyle.SHORT)
                            .setRequired(true)
                            .build();
                    TextInput teamNameInput = TextInput.create("team-name", "Team Name", TextInputStyle.SHORT)
                            .setRequired(true)
                            .build();

                    event.replyModal(Modal.create("new-team", "New Team")
                            .addActionRow(teamIdInput)
                            .addActionRow(teamNameInput)
                            .build()).queue();
                    return;
                }
                Roles roles = new Roles(event.getGuild());
                MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);

                Player captain = new Player(event.getUser().getIdLong());
                List<Player> players = new ArrayList<>();
                Arrays.stream(messageEmbed.getFields().get(0).getValue().replace("Players: ", "")
                        .split("\n")[1]
                        .replace("\n", " ")
                        .replaceAll("[<@>]", "")
                        .split(" ")).toList().forEach(idString -> players.add(new Player(Long.parseLong(idString))));

                RegistrationMessage registrationMessage = new RegistrationMessage(event.getJDA());
                registrationMessage.leagueTeam(new Team(captain, players, event.getSelectedOptions().get(0).getLabel(),
                        event.getSelectedOptions().get(0).getValue().toUpperCase()),
                        roles.getTeamRoleField(event.getSelectedOptions().get(0).getLabel()));

                event.editMessage("A request has been sent to the mod team. \n`This message will delete in 5 seconds.`")
                        .setComponents()
                        .submit()
                        .thenCompose((msg) -> msg.deleteOriginal().submitAfter(5, TimeUnit.SECONDS))
                        .whenComplete((success, error) -> {
                            if (error != null) {
                                log.info("Ephemeral Message was dealt with before auto delete.");
                            }
                        });
                commandLogger.recordNewRequest(event.getUser().getIdLong(), "team",
                        event.getTimeCreated().toEpochSecond());
            }
            case "league-selector" -> {
                boolean isFreeAgentApproval = event.getMessage().getEmbeds().get(0).getTitle().contains("Free Agent");

                event.editMessageEmbeds(new EmbedBuilder(event.getMessage().getEmbeds().get(0))
                                .setThumbnail("attachment://approved.png")
                                .addField("League", event.getSelectedOptions().get(0).getLabel(), true)
                                .setColor(Color.GREEN)
                                .addField("Verdict", "Accepted by " + event.getUser().getAsMention()
                                        + " at <t:" + Instant.now().getEpochSecond() + ":f>", false)
                        .build())
                        .setComponents()
                        .setFiles(FileUpload.fromData(APPROVED_IMG, "approved.png"))
                        .queue();

                Registration registration = new Registration(event.getGuild());

                if (isFreeAgentApproval) {
                    String discordID = Objects.requireNonNull(event.getMessage().getEmbeds().get(0).getFields().stream()
                            .filter(field -> Objects.equals(field.getName(), "Player")).findFirst().get()
                            .getValue()).split("\n")[0].replace("DiscordID: ", "").trim();

                    registration.assignFreeAgent(Long.parseLong(discordID),
                            League.valueOf(event.getSelectedOptions().get(0).getLabel().toUpperCase()));
                    commandLogger.removeRequest(event.getUser().getId(), "free-agent");
                } else {
                    MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);

                    registration.registerTeam(new EmbedConverter(messageEmbed).getTeamFromEmbed(),
                            event.getSelectedOptions().get(0).getLabel());
                    commandLogger.removeRequest(event.getUser().getId(), "team");
                }
            }
            case "table-names" -> {
                DatabaseEmbed databaseEmbed = new DatabaseEmbed(event.getSelectedOptions().get(0).getLabel());

                event.editMessageEmbeds(databaseEmbed.getDatabaseEmbed())
                        .setComponents(ActionRow.of(databaseEmbed.getTableSelectMenu()),
                                ActionRow.of(databaseEmbed.getDeleteSelectMenu()),
                                databaseEmbed.getPageButtons())
                        .submit();
            }
            case "delete-row" -> {
                try {
                    MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);

                    Database database = new Database();
                    database.deleteRow(messageEmbed.getFields().get(0).getValue(),
                            Integer.parseInt(event.getSelectedOptions().get(0).getValue()));


                    DatabaseEmbed databaseEmbed = new DatabaseEmbed(messageEmbed.getFields().get(0).getValue(),
                            Integer.parseInt(messageEmbed.getFields().get(1).getValue()));

                    event.editMessageEmbeds(databaseEmbed.getDatabaseEmbed())
                            .setComponents(ActionRow.of(databaseEmbed.getTableSelectMenu()),
                                    ActionRow.of(databaseEmbed.getDeleteSelectMenu()),
                                    databaseEmbed.getPageButtons())
                            .submit();
                } catch (SQLException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
            case "help-options" -> {
                HelpEmbed helpEmbed = new HelpEmbed(Integer.parseInt(event.getSelectedOptions().get(0).getValue()));

                event.editMessageEmbeds(helpEmbed.getHelpMessageEmbed())
                        .setComponents(ActionRow.of(helpEmbed.getSelectMenu()))
                        .submit();
            }
        }
    }
}
