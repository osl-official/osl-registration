package org.bot.scripts;

import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.FileUpload;
import org.bot.enums.League;
import org.bot.factories.RosterFactory;
import org.bot.models.Setting;
import org.bot.models.entity.Player;
import org.bot.models.entity.Team;
import org.bot.models.entity.FreeAgent;
import org.bot.models.entity.TeamPlayer;
import org.bot.service.FreeAgentService;
import org.bot.service.TeamPlayerService;
import org.bot.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
@Component
public class Registration {
    private Guild guild;
    private final Setting setting;
    private final RosterFactory rosterFactory;

    @Autowired
    public Registration(Setting setting, @Autowired(required = false) Guild guild) {
        this.setting = setting;
        this.guild = guild;
        this.rosterFactory = new RosterFactory(setting);
    }

    @SneakyThrows
    public void registerTeam(Team team, List<TeamPlayer> players, String league) {
        TeamService teamService = new TeamService(setting);
        TeamPlayerService teamPlayerService = new TeamPlayerService(setting);

        List<String> discordIDsList = new ArrayList<>();
        StringBuilder playerBuilder = new StringBuilder();
        TeamPlayer captain = players.stream().filter(TeamPlayer::isCaptain).findFirst().get();

        Roles roles = new Roles(guild);

        team.setLeague(League.valueOf(league.toUpperCase()));

        roles.giveRoleToMultiple(discordIDsList, team.getLeague() + " Player");

        teamService.save(team);
        players.forEach(p -> {
            if (!p.isCaptain()) {
                playerBuilder.append("<@").append(p.getPlayer().getDiscordId()).append(">").append(System.lineSeparator());
            }
            discordIDsList.add(p.toString());
            teamPlayerService.save(p);

        });

        Roster roster = rosterFactory.createRoster(guild);

        roster.addToRoster(team, players);

        TeamChannel teamChannel = new TeamChannel(guild);
        teamChannel.addTeamTextChannel(team.getTeamName(), League.valueOf(league.toUpperCase()));
        teamChannel.addTeamVoiceChannel(team.getTeamName(), League.valueOf(league.toUpperCase()));

        EmbedBuilder embedBuilder = new EmbedBuilder();
        @Cleanup InputStream inputStream = getClass().getResourceAsStream("/images/osl-logo.png");



        embedBuilder.setTitle("Oceanic Slapshot League Team Created")
                .setDescription("Welcome to OSL's current season of **Slapshot:Rebound** :hockey:. Congratulations on " +
                        "the successful registration of **" + team.getTeamName() +"** :star_struck:!! We welcome you aboard, " +
                        "and encourage you to keep an eye out on your league's fixture channel. If you have " +
                        "any issues feel free to reach out to a **League Coordinator**. Most importantly " +
                        "have fun and keep slappin! :thumbsup:")
                .setColor(Color.cyan)
                .addField("Team Name", team.getTeamName(), true)
                .addField("Team ID", team.getTeamID(), true)
                .addField("Captain", "<@" + captain.getPlayer().getDiscordId() + ">", true)
                .addField("Players", playerBuilder.toString(), true)
                .addField("League", league, true)
                .setThumbnail("attachment://osl-logo.png")
                .setFooter(guild.getSelfMember().getEffectiveName(), guild.getSelfMember().getEffectiveAvatarUrl());

        guild.getMemberById(captain.getPlayer().getDiscordId()).getUser().openPrivateChannel()
                .queue(ch -> ch.sendMessageEmbeds(embedBuilder.build()).addFiles(FileUpload.fromData(inputStream, "osl-logo.png")).complete());
    }

    public void assignFreeAgent(long discordId, League league) {
        FreeAgentService service = new FreeAgentService(setting);
        new Roles(guild)
                .giveRole(
                        Objects.requireNonNull(Objects.requireNonNull(guild).getMemberById(discordId))
                                .getUser(), league.label +
                                " Free Agent");

        FreeAgent freeAgent = new FreeAgent();
        freeAgent.setPlayer(new Player(discordId));
        freeAgent.setLeague(league);
        service.save(freeAgent);
        log.info(discordId + " has become a free agent");
        Roster roster = rosterFactory.createRoster(guild);
        roster.addToRoster(discordId, league);
    }
}
