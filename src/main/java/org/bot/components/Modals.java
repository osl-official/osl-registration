package org.bot.components;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bot.models.entity.Player;
import org.bot.models.Setting;
import org.bot.models.entity.Team;
import org.bot.scripts.CommandLogger;
import org.bot.scripts.RegistrationMessage;
import org.bot.scripts.Roles;
import org.bot.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class Modals extends ListenerAdapter {
    private final Setting setting;

    @Autowired
    public Modals(Setting setting) {
        this.setting = setting;
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        CommandLogger commandLogger  = new CommandLogger(setting);
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

                RegistrationMessage registrationMessage = new RegistrationMessage(event.getJDA(), setting);
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
                TeamService service = new TeamService(setting);
                String teamName = event.getValue("team-name").getAsString();
                String teamId = event.getValue("team-id").getAsString();

                for (Team team : service.findAll()) {
                    if (team.getTeamID().equalsIgnoreCase(teamId)) {
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
                    if (team.getTeamName().equalsIgnoreCase(teamName)) {
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
                }

                Roles roles = new Roles(event.getGuild());
                RegistrationMessage registrationMessage = new RegistrationMessage(event.getJDA(), setting);
                List<Player> playerList = new ArrayList<>();
                Arrays.stream(event.getMessage().getEmbeds().get(0).getFields().get(0).getValue().split("Players: ")[1]
                        .replaceAll("[<@>]", "").split(" ")).toList()
                        .forEach(idString -> playerList.add(new Player(Long.parseLong(idString))));

                try {
                    Team newTeam = new Team();
                    newTeam.setTeamID(teamId);
                    newTeam.setTeamName(teamName);

                    registrationMessage.leagueTeam(newTeam, event.getUser().getIdLong(), playerList,
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
