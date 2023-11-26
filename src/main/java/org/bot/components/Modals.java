package org.bot.components;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.bot.converters.Database;
import org.bot.models.Player;
import org.bot.models.Team;
import org.bot.scripts.CommandLogger;
import org.bot.scripts.RegistrationMessage;
import org.bot.scripts.ReplyEphemeral;
import org.bot.scripts.Roles;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Modals extends ListenerAdapter {

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        CommandLogger commandLogger  = new CommandLogger();
        switch (event.getModalId()) {
            case "confirm-fa" -> {
                if (!event.getValue("confirm").getAsString().equalsIgnoreCase("confirm")) {
                    event.reply("Confirmation failed try again. \n`This message will delete after 10 seconds`")
                            .submit()
                            .thenCompose((msg) -> msg.deleteOriginal().submitAfter(10, TimeUnit.SECONDS))
                            .whenComplete((success, error) -> {
                                if(error != null) {
                                    log.info("Ephemeral Message was dealt with before auto delete.");
                                }
                            });
                    return;
                }
                Player player = new Player(event.getUser().getIdLong());

                RegistrationMessage registrationMessage = new RegistrationMessage(event.getJDA());
                registrationMessage.freeAgent(player);

                event.reply("A request has been sent to the mod team. \n`This message will delete in 5 seconds.`")
                        .setEphemeral(true)
                        .submit()
                        .thenCompose((msg) -> msg.deleteOriginal().submitAfter(5, TimeUnit.SECONDS))
                        .whenComplete((success, error) -> {
                            if (error != null) {
                                log.info("Ephemeral Message was dealt with before auto delete.");
                            }
                        });
                commandLogger.recordNewRequest(event.getUser().getIdLong(), "free-agent",
                        event.getTimeCreated().toEpochSecond());
            }
            case "new-team" -> {
                Database database = new Database();
                String teamName = event.getValue("team-name").getAsString();
                String teamId = event.getValue("team-id").getAsString();
                try {
                    if (database.doesTeamIdExist(teamId)) {
                        event.editMessage("Team ID already taken, please use a different one." +
                                        "\n`This message will delete in 15 seconds.`")
                                .setComponents()
                                .submit()
                                .thenCompose((msg) -> msg.deleteOriginal().submitAfter(15, TimeUnit.SECONDS))
                                .whenComplete((success, error) -> {
                                    if (error != null) {
                                        log.info("Ephemeral Message was dealt with before auto delete.");
                                    }
                                });
                        return;
                    }
                    if (database.doesTeamNameExist(teamName)) {
                        event.editMessage("Team Name already taken, please use a different one." +
                                        "\n`This message will delete in 15 seconds.`")
                                .setComponents()
                                .submit()
                                .thenCompose((msg) -> msg.deleteOriginal().submitAfter(15, TimeUnit.SECONDS))
                                .whenComplete((success, error) -> {
                                    if (error != null) {
                                        log.info("Ephemeral Message was dealt with before auto delete.");
                                    }
                                });
                        return;
                    }
                } catch (SQLException e) {
                    log.error(e.getMessage());
                }

                Roles roles = new Roles(event.getGuild());
                RegistrationMessage registrationMessage = new RegistrationMessage(event.getJDA());
                List<Player> playerList = new ArrayList<>();
                Arrays.stream(event.getMessage().getEmbeds().get(0).getFields().get(0).getValue().split("Players: ")[1]
                        .replaceAll("[<@>]", "").split(" ")).toList()
                        .forEach(idString -> playerList.add(new Player(Long.parseLong(idString))));

                try {
                    Team newTeam = new Team(new Player(event.getUser().getIdLong()), playerList, teamName, teamId);

                    registrationMessage.leagueTeam(newTeam,
                            roles.getTeamRoleField(teamName));

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
                } catch (IllegalArgumentException e) {
                    event.reply(e.getLocalizedMessage()).setEphemeral(true)
                            .submit()
                            .thenCompose(msg -> msg.deleteOriginal().submitAfter(10, TimeUnit.SECONDS))
                            .whenComplete((success, error) -> {
                                if (error != null) {
                                    log.info("Ephemeral Message was dealt with before auto delete.");
                                }
                            });
                }
            }
        }
    }
}
