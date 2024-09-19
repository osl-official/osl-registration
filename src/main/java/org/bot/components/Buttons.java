package org.bot.components;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.FileUpload;
import org.bot.enums.TableName;
import org.bot.factories.RosterFactory;
import org.bot.models.Setting;
import org.bot.models.entity.Team;
import org.bot.scripts.*;
import org.bot.service.TeamPlayerService;
import org.bot.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.InputStream;
import java.time.Instant;
import java.util.Objects;

@Slf4j
@Component
public class Buttons extends ListenerAdapter {
    private final String APPROVED_IMG = "/images/approve.png";
    private final String DENIED_IMG = "/images/denied.png";
    private final Setting setting;
    private final RosterFactory rosterFactory;

    @Autowired
    public Buttons(Setting setting) {
        this.setting = setting;
        this.rosterFactory = new RosterFactory(setting);
    }

    @Override
    @SneakyThrows
    public void onButtonInteraction(ButtonInteractionEvent event) {
        MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);

        switch (Objects.requireNonNull(event.getButton().getId())) {
            case "deny" -> {
                @Cleanup InputStream inputStream = getClass().getResourceAsStream(DENIED_IMG);
                CommandLogger commandLogger = new CommandLogger(setting);
                event.getMessage().editMessageEmbeds(new EmbedBuilder(messageEmbed)
                                .setThumbnail("attachment://denied.png")
                                .setColor(Color.RED)
                                .addField("Verdict", "Denied by " + event.getUser().getAsMention()
                                        + " at <t:" + Instant.now().getEpochSecond() + ":f>", false)
                                .build())
                        .setComponents()
                        .setFiles(FileUpload.fromData(inputStream, "denied.png"))
                        .complete();
                if (event.getMessage().getEmbeds().get(0).getTitle().contains("Free Agent")) {
                    commandLogger.removeRequest(event.getUser().getId(), "free-agent");
                } else {
                    commandLogger.removeRequest(event.getUser().getId(), "team");
                }
            }
            case "approve" -> {
                @Cleanup InputStream inputStream = getClass().getResourceAsStream(APPROVED_IMG);
                event.getMessage().editMessageEmbeds(new EmbedBuilder(messageEmbed)
                                .setColor(Color.GREEN)
                                .setThumbnail("attachment://approved.png")
                                .addField("Verdict", "Approved by " + event.getUser().getAsMention()
                                        + " at <t:" + Instant.now().getEpochSecond() + ":f>", false)
                                .build())
                        .setComponents()
                        .setFiles(FileUpload.fromData(inputStream, "approved.png"))
                        .complete();

                if (Objects.requireNonNull(messageEmbed.getTitle()).contains("Disband")) {
                    String teamID = messageEmbed.getFields().get(0).getValue();
                    String[] players = Objects.requireNonNull(messageEmbed.getFields().get(1).getValue()).split("\n");
                    String teamName = Objects.requireNonNull(messageEmbed.getDescription()).split("\\*\\*")[1];

                    TeamService teamService = new TeamService(setting);
                    TeamPlayerService teamPlayerService = new TeamPlayerService(setting);


                    Roles roles = new Roles(event.getGuild());

                    assert teamID != null;
                    teamPlayerService.findAllByTeamId(teamID).get().forEach(teamPlayerService::remove);
                    Team updatedTeam = new Team();
                    updatedTeam.setLeague(null);
                    updatedTeam.setTeamName(teamName);
                    updatedTeam.setTeamID(teamID);
                    teamService.save(updatedTeam);

                    Roster roster = rosterFactory.createRoster(event.getGuild());
                    roster.removeFromRoster(roles.getRole(teamName));

                    for (MessageEmbed.Field field : messageEmbed.getFields()) {
                        String fieldName = field.getName();
                        assert fieldName != null;
                        if (fieldName.contains("Assignable")) {
                            if (Objects.requireNonNull(field.getValue()).equalsIgnoreCase(Boolean.FALSE.toString())) {
                                teamService.removeById(teamID);
                            }
                        } else if (fieldName.contains("Role")) {
                            if (Objects.requireNonNull(field.getValue()).equalsIgnoreCase(Boolean.TRUE.toString())) {
                                roles.deleteRole(teamName);
                                for (String discordID : players) {
                                    discordID = discordID.replace("<@", "").replace(">", "").trim();
                                    roles.removeRole(Long.valueOf(discordID), "Player"); // Removes League Player Role
                                }
                            } else {
                                for (String discordID: players) {
                                    discordID = discordID.replace("<@", "").replace(">", "").trim();
                                    roles.removeRole(Long.valueOf(discordID), teamName);
                                    roles.removeRole(Long.valueOf(discordID), "Player"); // Removes League Player Role
                                }
                            }
                        } else if (fieldName.contains("Channel")) {
                            if (Objects.requireNonNull(field.getValue()).equalsIgnoreCase(Boolean.TRUE.toString())) {
                                TeamChannel teamChannel = new TeamChannel(event.getGuild());
                                teamChannel.removeTeamTextChannel(teamName);
                                teamChannel.removeTeamVoiceChannel(teamName);
                            }
                        }
                    }
                }
            }
            case "page-prev" -> {
                TableName tableName = TableName.valueOf(
                        messageEmbed.getFields().get(0)
                                .getValue().toUpperCase().replace(" ", ""));
                DatabaseEmbed databaseEmbed = new DatabaseEmbed(setting, tableName,
                        Integer.parseInt(messageEmbed.getFields().get(1).getValue()) - 1);

                event.editMessageEmbeds(databaseEmbed.getDatabaseEmbed())
                        .setComponents(ActionRow.of(databaseEmbed.getTableSelectMenu()),
                                ActionRow.of(databaseEmbed.getDeleteSelectMenu()),
                                databaseEmbed.getPageButtons())
                        .submit();
            }
            case "page-next" -> {
                TableName tableName = TableName.valueOf(messageEmbed.getFields().get(0)
                        .getValue().toUpperCase().replace(" ", ""));
                DatabaseEmbed databaseEmbed = new DatabaseEmbed(setting, tableName,
                        Integer.parseInt(messageEmbed.getFields().get(1).getValue()) + 1);

                event.editMessageEmbeds(databaseEmbed.getDatabaseEmbed())
                        .setComponents(ActionRow.of(databaseEmbed.getTableSelectMenu()),
                                ActionRow.of(databaseEmbed.getDeleteSelectMenu()),
                                databaseEmbed.getPageButtons())
                        .submit();
            }
        }
    }
}
