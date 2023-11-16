package org.bot.components;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bot.converters.Database;
import org.bot.scripts.Roles;

import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;

@Slf4j
public class Buttons extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);

        switch (event.getButton().getId()) {
            case "deny" -> event.getMessage().editMessageEmbeds(new EmbedBuilder(messageEmbed)
                            .setColor(Color.RED)
                            .addField("Verdict", "Denied by " + event.getUser().getAsMention()
                                    + " at <t:" + Instant.now().getEpochSecond() + ":f>", false)
                            .build())
                    .setComponents()
                    .queue();
            case "approve" -> {
                event.getMessage().editMessageEmbeds(new EmbedBuilder(messageEmbed)
                                .setColor(Color.GREEN)
                                .addField("Verdict", "Approved by " + event.getUser().getAsMention()
                                        + " at <t:" + Instant.now().getEpochSecond() + ":f>", false)
                                .build())
                        .setComponents()
                        .queue();

                if (messageEmbed.getTitle().contains("disband")) {
                    String teamID = messageEmbed.getFields().get(0).getValue();
                    String[] players = messageEmbed.getFields().get(1).getValue().split("\n");
                    String teamName = messageEmbed.getDescription().split("\\*\\*")[1];

                    Database database = new Database();
                    Roles roles = new Roles(event.getGuild());

                    try {
                        database.setTeamNotTaken(teamID);
                        database.disbandTeam(teamID);
                    } catch (SQLException e) {
                        log.error(e.getLocalizedMessage());
                    }

                    for (String discordID: players) {
                        roles.removeRole(Long.valueOf(discordID), teamName);
                        System.out.println("Removed");
                        roles.removeRole(Long.valueOf(discordID), "Player"); // Removes League Player Role
                    }
                }
            }
        }
    }
}
