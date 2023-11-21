package org.bot.commands.slash;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.bot.converters.Config;
import org.bot.converters.Database;
import org.bot.models.Player;
import org.bot.scripts.RegistrationMessage;
import org.bot.scripts.ReplyEphemeral;
import org.bot.scripts.Roles;
import org.bot.scripts.Roster;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AdminLeagueRegistration {
    private final Database database = new Database();
    private final Config config = new Config();
    private final ReplyEphemeral replyEphemeral;
    private final SlashCommandInteractionEvent event;

    public AdminLeagueRegistration(SlashCommandInteractionEvent event) {
        this.replyEphemeral = new ReplyEphemeral(event);
        this.event = event;
    }

    public void removeFreeAgent() {
        User freeAgent = Objects.requireNonNull(event.getOption("freeagent")).getAsUser();
        try {
            if (!database.isFreeAgent(freeAgent.getIdLong())) {
                replyEphemeral.sendThenDelete("User is not a Free Agent",
                        10, TimeUnit.SECONDS);
                return;
            }

            replyEphemeral.sendThenDelete(freeAgent.getAsMention() + " has been removed from the Free Agent list",
                    5, TimeUnit.SECONDS);

            Roles roles = new Roles(event.getGuild());
            roles.removeRole(freeAgent, "Free Agent");

            database.removeFreeAgent(freeAgent.getIdLong());
            new Roster(Objects.requireNonNull(event.getGuild())).removeFromRoster(freeAgent.getIdLong());
        } catch (SQLException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public void addFreeAgent() {
        User freeAgent = Objects.requireNonNull(event.getOption("freeagent")).getAsUser();
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
    }

    public void disbandTeam() {
        Role role = Objects.requireNonNull(event.getOption("teamrole")).getAsRole();
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

            replyEphemeral.sendThenDelete("Please confirm action at <#" + config.getTeamRegistrationChannel() + ">",
                    10, TimeUnit.SECONDS);

            List<Player> players = database.getTeamPlayers(team.get("teamID"));
            players.add(database.getCaptain(team.get("teamID")));

            boolean assignable = Objects.requireNonNull(event.getOption("assignable")).getAsBoolean();
            boolean deleteRole = event.getOption("delete-role") != null &&
                    Objects.requireNonNull(event.getOption("delete-role")).getAsBoolean();
            boolean deleteChannels = event.getOption("delete-channels") != null &&
                    Objects.requireNonNull(event.getOption("delete-channels")).getAsBoolean();

            new RegistrationMessage(event.getJDA()).disbandTeam(team, event.getUser(), players, assignable, deleteRole, deleteChannels);
        } catch (SQLException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public void createTeam() {
        String teamName = Objects.requireNonNull(event.getOption("teamname")).getAsString();
        String teamID = Objects.requireNonNull(event.getOption("teamid")).getAsString();

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
            log.error(e.getLocalizedMessage());
        }
    }

    public void refreshRoster() {
        Roster roster = new Roster(Objects.requireNonNull(event.getGuild()));
        roster.refreshRoster();

        replyEphemeral.sendThenDelete("Updating the roster now, this may take a minute", 10, TimeUnit.SECONDS);
    }
}
