package org.bot.commands.slash;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bot.converters.Config;
import org.bot.converters.Database;
import org.bot.models.Player;
import org.bot.scripts.RegistrationMessage;
import org.bot.scripts.ReplyEphemeral;
import org.bot.scripts.Roles;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AdminLeagueRegistration extends ListenerAdapter {
    private final Database database = new Database();
    private ReplyEphemeral replyEphemeral;
    private final Config config = new Config();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String name = event.getName();
        if (name.equalsIgnoreCase("remove-fa")) {
            replyEphemeral = new ReplyEphemeral(event);

            User freeAgent = event.getOption("freeagent").getAsUser();
            try {
                if (freeAgent.isBot()) {
                    replyEphemeral.sendThenDelete("Bot cannot be registered as a Free Agent",
                            10, TimeUnit.SECONDS);
                    return;
                }
                if (!database.isFreeAgent(freeAgent.getIdLong())) {
                    replyEphemeral.sendThenDelete("User is not a Free Agent",
                            10, TimeUnit.SECONDS);
                    return;
                }

                replyEphemeral.sendThenDelete(freeAgent.getAsMention() + " has been removed from the Free Agent list",
                        5, TimeUnit.SECONDS);

                new Roles(event.getGuild()).removeRole(freeAgent, "Free Agent");

                database.removeFreeAgent(freeAgent.getIdLong());
            } catch (SQLException e) {
                log.error(e.getLocalizedMessage());
            }
        } else if (name.equalsIgnoreCase("add-fa")) {
            replyEphemeral = new ReplyEphemeral(event);
            User freeAgent = event.getOption("freeagent").getAsUser();
            try {
                if (freeAgent.isBot()) {
                    replyEphemeral.sendThenDelete("Bot cannot be registered as a Free Agent",
                            10, TimeUnit.SECONDS);
                    return;
                }
                if (database.isFreeAgent(freeAgent.getIdLong())) {
                    replyEphemeral.sendThenDelete("User is not a Free Agent",
                            10, TimeUnit.SECONDS);
                    return;
                }
                replyEphemeral.sendThenDelete("Refer to <#" + config.getFaRegistrationChannel() + "> for the next steps",
                        5, TimeUnit.SECONDS);

                RegistrationMessage registrationMessage = new RegistrationMessage(event.getJDA());
                registrationMessage.freeAgent(new Player(freeAgent.getIdLong()));
            } catch (SQLException e) {
                log.error(e.getLocalizedMessage());
            }
        } else if (name.equalsIgnoreCase("disband-team")) {
            replyEphemeral = new ReplyEphemeral(event);
            Role role = event.getOption("teamrole").getAsRole();
            try {
                List<HashMap<String, String>> teamsTaken = database.getTeamsTaken();
                Optional<HashMap<String, String>> teamRole = teamsTaken.stream().filter(teamTaken -> teamTaken.containsValue(role.getName().split("\\|")[0].trim()))
                        .findFirst();
                HashMap<String, String> team = new HashMap<>();
                for (HashMap<String, String> teamTaken: teamsTaken) {
                    if (teamTaken.containsValue(role.getName().split("\\|")[0].trim())) {
                        team = teamTaken;
                    }
                }
                if (teamRole.isEmpty()) {
                    replyEphemeral.sendThenDelete("Role either isn't linked to a Team or the Team has no players",
                            10, TimeUnit.SECONDS);
                    return;
                }

                replyEphemeral.sendThenDelete("Please confirm action at #" + config.getFaRegistrationChannel(),
                        10, TimeUnit.SECONDS);

                List<Player> players = database.getTeamPlayers(team.get("teamID"));
                players.add(database.getCaptain(team.get("teamID")));

                new RegistrationMessage(event.getJDA()).disbandTeam(team, event.getUser(), players);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (name.equalsIgnoreCase("create-team")) {
            replyEphemeral = new ReplyEphemeral(event);
            String teamName = event.getOption("teamname").getAsString();
            String teamID = event.getOption("teamid").getAsString();

            try {
                if (database.doesTeamIdExist(teamID)) {
                    replyEphemeral.sendThenDelete("Team ID already exists. Please use another Team ID",
                            10, TimeUnit.SECONDS);
                    return;
                }
                if (database.doesTeamNameExist(teamName)) {
                    replyEphemeral.sendThenDelete("Team Name already exists. Please use another Team Name",
                            10, TimeUnit.SECONDS);
                    return;
                }
                database.addNewTeam(teamID, teamName);

                replyEphemeral.sendThenDelete(teamName + " has been added",
                        5, TimeUnit.SECONDS);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
