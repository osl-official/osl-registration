package org.bot.components;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.bot.converters.Config;
import org.bot.converters.Database;
import org.bot.models.Player;
import org.bot.models.Team;
import org.bot.scripts.RegistrationMessage;
import org.bot.scripts.Roles;

import java.awt.Color;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SelectMenus extends ListenerAdapter {
    private final Database database = new Database();

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        switch (event.getSelectMenu().getId()){
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
            }
            case "league-selector" -> {
                boolean isFreeAgentApproval = event.getMessage().getEmbeds().get(0).getTitle().contains("Free Agent");

                event.editMessageEmbeds(new EmbedBuilder(event.getMessage().getEmbeds().get(0))
                                .addField("League", event.getSelectedOptions().get(0).getLabel(), true)
                                .setColor(Color.GREEN)
                                .addField("Verdict", "Accepted by " + event.getUser().getAsMention()
                                        + " at <t:" + Instant.now().getEpochSecond() + ":f>", false)
                        .build())
                        .setComponents()
                        .queue();

                if (isFreeAgentApproval) {
                    String discordID = event.getMessage().getEmbeds().get(0).getFields().stream()
                            .filter(field -> field.getName().equals("Player")).findFirst().get()
                            .getValue().split("\n")[0].replace("DiscordID: ", "").trim();

                    new Roles(event.getGuild())
                            .giveRole(
                                    event.getGuild().getMemberById(discordID).getUser(),
                                    event.getSelectedOptions().get(0).getLabel() +
                                            " Free Agent");
                    try {
                        database.addFreeAgent(Long.parseLong(discordID));
                        log.info(discordID + " has become a free agent");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    return;
                }

                List<String> discordIDsList = new ArrayList<>();

                String[] discordIDs = event.getMessage().getEmbeds().get(0).getFields().stream()
                        .filter(field -> field.getName().equals("Team")).findFirst().get()
                        .getValue().replace("Captain: ", "")
                        .replace("\nPlayers: ", " ").split(" ");

                for (String discordID: discordIDs) {
                    discordIDsList.add(discordID.replaceAll("[<@>]", ""));
                }
                Roles roles = new Roles(event.getGuild());
                String teamName = event.getMessage().getEmbeds().get(0).getFields().get(1).getValue();
                String teamID = event.getMessage().getEmbeds().get(0).getFields().get(2).getValue();
                roles.giveRoleToMultiple(discordIDsList,
                        teamName);
                roles.giveRoleToMultiple(discordIDsList,  event.getInteraction().getSelectedOptions().get(0).getLabel() + " Player");

                try {
                    if (!database.doesTeamIdExist(teamID)) {
                        database.addNewTeam(teamID, teamName);
                    }

                    database.setTeamTaken(teamID, event.getUser().getIdLong());
                    discordIDsList.forEach(playerID -> {
                        try {
                            database.addPlayerToTeam(Long.parseLong(playerID), teamID);
                            log.info(playerID + " has been added to " + teamName);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
