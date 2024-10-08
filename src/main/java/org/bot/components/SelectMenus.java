package org.bot.components;

import lombok.Cleanup;
import lombok.SneakyThrows;
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
import org.bot.converters.EmbedConverter;
import org.bot.enums.League;
import org.bot.enums.TableName;
import org.bot.models.entity.FreeAgent;
import org.bot.models.entity.Player;
import org.bot.models.Setting;
import org.bot.models.entity.Team;
import org.bot.models.entity.TeamPlayer;
import org.bot.scripts.*;
import org.bot.service.FreeAgentService;
import org.bot.service.PlayerService;
import org.bot.service.TeamPlayerService;
import org.bot.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SelectMenus extends ListenerAdapter {
    private final String APPROVED_IMG = "/images/approve.png";
    private final Setting setting;

    @Autowired
    public SelectMenus(Setting setting) {
        this.setting = setting;
    }

    @Override
    @SneakyThrows
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        CommandLogger commandLogger = new CommandLogger(setting);
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

                RegistrationMessage registrationMessage = new RegistrationMessage(event.getJDA(), setting);
                Team team = new Team();
                team.setTeamName(event.getSelectedOptions().get(0).getLabel());
                team.setTeamID(event.getSelectedOptions().get(0).getValue().toUpperCase());
                registrationMessage.leagueTeam(team, captain.getDiscordId(), players,
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
                @Cleanup InputStream inputStream = getClass().getResourceAsStream(APPROVED_IMG);

                boolean isFreeAgentApproval = event.getMessage().getEmbeds().get(0).getTitle().contains("Free Agent");

                event.editMessageEmbeds(new EmbedBuilder(event.getMessage().getEmbeds().get(0))
                                .setThumbnail("attachment://approved.png")
                                .addField("League", event.getSelectedOptions().get(0).getLabel(), true)
                                .setColor(Color.GREEN)
                                .addField("Verdict", "Accepted by " + event.getUser().getAsMention()
                                        + " at <t:" + Instant.now().getEpochSecond() + ":f>", false)
                        .build())
                        .setComponents()
                        .setFiles(FileUpload.fromData(inputStream, "approved.png"))
                        .complete();

                Registration registration = new Registration(setting, event.getGuild());

                if (isFreeAgentApproval) {
                    String discordID = Objects.requireNonNull(event.getMessage().getEmbeds().get(0).getFields().stream()
                            .filter(field -> Objects.equals(field.getName(), "Player")).findFirst().get()
                            .getValue()).split("\n")[0].replace("DiscordID: ", "").trim();

                    registration.assignFreeAgent(Long.parseLong(discordID),
                            League.valueOf(event.getSelectedOptions().get(0).getLabel().toUpperCase()));
                    commandLogger.removeRequest(event.getUser().getId(), "free-agent");
                } else {
                    MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);
                    EmbedConverter embedConverter = new EmbedConverter(messageEmbed);

                    registration.registerTeam(embedConverter.getTeamFromEmbed(),
                            embedConverter.getPlayersFromEmbed(), event.getSelectedOptions().get(0).getLabel());

                    commandLogger.removeRequest(event.getUser().getId(), "team");
                }
            }
            case "table-names" -> {
                TableName tableName = TableName.valueOf(event.getSelectedOptions().get(0)
                        .getLabel().toUpperCase().replace(" ", ""));
                DatabaseEmbed databaseEmbed = new DatabaseEmbed(setting, tableName);

                event.editMessageEmbeds(databaseEmbed.getDatabaseEmbed())
                        .setComponents(ActionRow.of(databaseEmbed.getTableSelectMenu()),
                                ActionRow.of(databaseEmbed.getDeleteSelectMenu()),
                                databaseEmbed.getPageButtons())
                        .submit();
            }
            case "delete-row" -> {
                MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);
                String tableNameStr = messageEmbed.getFields().get(0).getValue();
                TableName tableName = TableName.valueOf(tableNameStr.replace(" ", "").toUpperCase());
                int rowIndex = Integer.parseInt(event.getSelectedOptions().get(0).getValue());

                switch (tableName) {
                    case TEAM -> {
                        TeamService service = new TeamService(setting);
                        Team team = service.findAll().get(rowIndex);

                        service.remove(team);
                    }
                    case TEAMPLAYER -> {
                        TeamPlayerService service = new TeamPlayerService(setting);
                        TeamPlayer teamPlayer = service.findAll().get(rowIndex);

                        service.remove(teamPlayer);
                    }
                    case PLAYER -> {
                        PlayerService service = new PlayerService(setting);
                        Player player = service.findAll().get(rowIndex);

                        service.remove(player);
                    }
                    case FREEAGENT -> {
                        FreeAgentService service = new FreeAgentService(setting);
                        FreeAgent freeAgent = service.findAll().get(rowIndex - 1);

                        service.remove(freeAgent);
                    }
                }

                DatabaseEmbed databaseEmbed = new DatabaseEmbed(setting, tableName, rowIndex);

                event.editMessageEmbeds(databaseEmbed.getDatabaseEmbed())
                        .setComponents(ActionRow.of(databaseEmbed.getTableSelectMenu()),
                                ActionRow.of(databaseEmbed.getDeleteSelectMenu()),
                                databaseEmbed.getPageButtons())
                        .submit();
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
