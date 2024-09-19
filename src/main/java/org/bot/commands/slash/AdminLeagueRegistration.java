package org.bot.commands.slash;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.bot.converters.JsonConverter;
import org.bot.factories.RosterFactory;
import org.bot.models.Setting;
import org.bot.models.entity.Player;
import org.bot.models.entity.Team;
import org.bot.models.entity.TeamPlayer;
import org.bot.scripts.*;
import org.bot.service.FreeAgentService;
import org.bot.service.TeamPlayerService;
import org.bot.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AdminLeagueRegistration {
    private final Setting setting;
    private final RosterFactory rosterFactory;
    private final ReplyEphemeral replyEphemeral;
    private final SlashCommandInteractionEvent event;

    @Autowired
    public AdminLeagueRegistration(Setting setting, @Autowired(required = false) SlashCommandInteractionEvent event) {
        this.setting = setting;
        this.replyEphemeral = new ReplyEphemeral(event);
        this.event = event;
        this.rosterFactory = new RosterFactory(setting);
    }

    public void removeFreeAgent() {
        User freeAgent = Objects.requireNonNull(event.getOption("freeagent")).getAsUser();
        FreeAgentService service = new FreeAgentService(setting);

        if (service.existsById(freeAgent.getIdLong())) {
            replyEphemeral.sendThenDelete("User is not a Free Agent",
                    10, TimeUnit.SECONDS);
            return;
        }
        service.removeById(freeAgent.getIdLong());
        Roles roles = new Roles(event.getGuild());
        roles.removeRole(freeAgent, "Free Agent");

        replyEphemeral.sendThenDelete(freeAgent.getAsMention() + " has been removed from the Free Agent list",
                5, TimeUnit.SECONDS);

        Roster roster = rosterFactory.createRoster(event.getGuild());
        roster.removeFromRoster(freeAgent.getIdLong());
    }

    public void addFreeAgent() {
        FreeAgentService service = new FreeAgentService(setting);

        User freeAgent = Objects.requireNonNull(event.getOption("freeagent")).getAsUser();

        if (freeAgent.isBot()) {
            replyEphemeral.sendThenDelete("Bot cannot be registered as a Free Agent",
                    10, TimeUnit.SECONDS);
            return;
        }
        if (service.existsById(freeAgent.getIdLong())) {
            replyEphemeral.sendThenDelete("User is not a Free Agent",
                    10, TimeUnit.SECONDS);
            return;
        }
        replyEphemeral.sendThenDelete("Refer to <#" + setting.getFaRegistration() + "> for the next steps",
                5, TimeUnit.SECONDS);

        RegistrationMessage registrationMessage = new RegistrationMessage(event.getJDA(), setting);
        registrationMessage.freeAgent(new Player(freeAgent.getIdLong()));
    }

    public void disbandTeam() {
        TeamService teamService = new TeamService(setting);
        TeamPlayerService teamPlayerService = new TeamPlayerService(setting);

        Role role = Objects.requireNonNull(event.getOption("teamrole")).getAsRole();
        List<Team> teamsTaken = teamService.findAllByLeagueIsNotNull();

        Team team = new Team();
        for (Team teamTaken : teamsTaken) {
            if (role.getName().contains(teamTaken.getTeamName())) {
                team = teamTaken;
            }
        }
        if (team.getTeamID().isEmpty()) {
            replyEphemeral.sendThenDelete("Role either isn't linked to a Team or the Team has no players",
                    10, TimeUnit.SECONDS);
            return;
        }

        replyEphemeral.sendThenDelete("Please confirm action at <#" + setting.getTeamRegistration() + ">",
                10, TimeUnit.SECONDS);

        List<TeamPlayer> players = teamPlayerService.findAllByTeamId(team.getTeamID()).get();

        boolean assignable = Objects.requireNonNull(event.getOption("assignable")).getAsBoolean();
        boolean deleteRole = event.getOption("delete-role") != null &&
                Objects.requireNonNull(event.getOption("delete-role")).getAsBoolean();
        boolean deleteChannels = event.getOption("delete-channels") != null &&
                Objects.requireNonNull(event.getOption("delete-channels")).getAsBoolean();

        new RegistrationMessage(event.getJDA(), setting).disbandTeam(team, event.getUser(), players, assignable, deleteRole, deleteChannels);
    }

    public void createTeam() {
        TeamService service = new TeamService(setting);

        String teamName = Objects.requireNonNull(event.getOption("teamname")).getAsString();
        String teamID = Objects.requireNonNull(event.getOption("teamid")).getAsString();

        for (Team team : service.findAll()) {
            if (team.getTeamID().equalsIgnoreCase(teamID)) {
                replyEphemeral.sendThenDelete("Team ID already exists. Please use another Team ID",
                        10, TimeUnit.SECONDS);
                return;
            }
            if (team.getTeamName().equalsIgnoreCase(teamName)) {
                replyEphemeral.sendThenDelete("Team Name already exists. Please use another Team Name",
                        10, TimeUnit.SECONDS);
                return;
            }
        }

        Team team = new Team();
        team.setTeamName(teamName);
        team.setTeamID(teamID);

        service.save(team);

        replyEphemeral.sendThenDelete(teamName + " has been added",
                5, TimeUnit.SECONDS);
    }

    public void refreshRoster() {

        Roster roster = rosterFactory.createRoster(event.getGuild());
        roster.refreshRoster();

        replyEphemeral.sendThenDelete("Updating the roster now, this may take a minute", 10, TimeUnit.SECONDS);
    }

    public void faToJson() {
        JsonConverter jsonConverter = new JsonConverter(setting);

        event.reply("All current free agents stored in the database.")
                .setEphemeral(true)
                .addFiles(jsonConverter.getFreeAgentsToJson())
                .queue();
    }

    public void teamsToJson() {
        JsonConverter jsonConverter = new JsonConverter(setting);

        event.reply("All current teams stored in the database.")
                .setEphemeral(true)
                .addFiles(jsonConverter.getTeamsToJson())
                .queue();
    }

    public void viewDatabase() {
        DatabaseEmbed databaseEmbed = new DatabaseEmbed(setting);

        event.replyEmbeds(databaseEmbed.getDatabaseEmbed())
                .setEphemeral(true)
                .setComponents(ActionRow.of(databaseEmbed.getTableSelectMenu()),
                        ActionRow.of(databaseEmbed.getDeleteSelectMenu()),
                        databaseEmbed.getPageButtons())
                .submit()
                .thenCompose(me -> me.deleteOriginal().submitAfter(5, TimeUnit.MINUTES))
                .whenComplete((success, error) -> {
                    if (error != null) {
                        log.info("Ephemeral Message was dealt with before auto delete.");
                    }
                });
    }
}
