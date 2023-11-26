package org.bot.components;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bot.converters.Database;
import org.bot.scripts.CommandLogger;
import org.bot.scripts.Roles;
import org.bot.scripts.Roster;
import org.bot.scripts.TeamChannel;

import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Objects;

@Slf4j
public class Buttons extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);

        switch (Objects.requireNonNull(event.getButton().getId())) {
            case "deny" -> {
                CommandLogger commandLogger = new CommandLogger();
                event.getMessage().editMessageEmbeds(new EmbedBuilder(messageEmbed)
                                .setColor(Color.RED)
                                .addField("Verdict", "Denied by " + event.getUser().getAsMention()
                                        + " at <t:" + Instant.now().getEpochSecond() + ":f>", false)
                                .build())
                        .setComponents()
                        .queue();
                if (event.getMessage().getEmbeds().get(0).getTitle().contains("Free Agent")) {
                    commandLogger.removeRequest(event.getUser().getId(), "free-agent");
                } else {
                    commandLogger.removeRequest(event.getUser().getId(), "team");
                }
            }
            case "approve" -> {
                event.getMessage().editMessageEmbeds(new EmbedBuilder(messageEmbed)
                                .setColor(Color.GREEN)
                                .addField("Verdict", "Approved by " + event.getUser().getAsMention()
                                        + " at <t:" + Instant.now().getEpochSecond() + ":f>", false)
                                .build())
                        .setComponents()
                        .queue();

                if (Objects.requireNonNull(messageEmbed.getTitle()).contains("Disband")) {
                    String teamID = messageEmbed.getFields().get(0).getValue();
                    String[] players = Objects.requireNonNull(messageEmbed.getFields().get(1).getValue()).split("\n");
                    String teamName = Objects.requireNonNull(messageEmbed.getDescription()).split("\\*\\*")[1];

                    Database database = new Database();
                    Roles roles = new Roles(event.getGuild());

                    try {
                        assert teamID != null;
                        database.setTeamNotTaken(teamID);
                        database.disbandTeam(teamID);

                        new Roster(Objects.requireNonNull(event.getGuild())).removeFromRoster(roles.getRole(teamName));

                        for (MessageEmbed.Field field : messageEmbed.getFields()) {
                            String fieldName = field.getName();
                            assert fieldName != null;
                            if (fieldName.contains("Assignable")) {
                                if (Objects.requireNonNull(field.getValue()).equalsIgnoreCase(Boolean.FALSE.toString())) {
                                    database.deleteTeam(teamID);
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
                    } catch (SQLException e) {
                        log.error(e.getLocalizedMessage());
                    }
                }
            }
        }
    }
}
