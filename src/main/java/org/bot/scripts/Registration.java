package org.bot.scripts;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.FileUpload;
import org.bot.converters.Database;
import org.bot.enums.League;
import org.bot.models.Team;

import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class Registration {
    private Guild guild;

    public void registerTeam(Team team, String league) {
        List<String> discordIDsList = new ArrayList<>();
        team.players().forEach(p -> discordIDsList.add(String.valueOf(p.discordId())));
        discordIDsList.add(String.valueOf(team.captain().discordId()));

        Roles roles = new Roles(guild);

        roles.giveRoleToMultiple(discordIDsList, league + " Player");
        roles.giveRoleToMultiple(discordIDsList, team.name());

        try {
            Database database = new Database();
            if (!database.doesTeamIdExist(team.nameAbbr())) {
                database.addNewTeam(team.nameAbbr(), team.name());
            }
            StringBuilder playerBuilder = new StringBuilder();
            database.setTeamTaken(team.nameAbbr(), team.captain().discordId());
            team.players().forEach(player -> {
                playerBuilder.append("<@").append(player.discordId()).append(">").append(System.lineSeparator());

                try {
                    database.addPlayerToTeam(player.discordId(), team.nameAbbr());
                    log.info(player.discordId() + " has been added to " + team.name());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            database.addCaptain(team.captain().discordId(), team.nameAbbr());
            database.setLeague(League.valueOf(league.toUpperCase()), team.nameAbbr());
            log.info(team.captain().discordId() + " has been added to " + team.name());

            Roster roster = new Roster(guild);
            roster.addToRoster(new Team(team.captain(), team.players(), team.name(), team.nameAbbr(),
                    League.valueOf(league.toUpperCase())));

            TeamChannel teamChannel = new TeamChannel(guild);
            teamChannel.addTeamTextChannel(team.name(), League.valueOf(league.toUpperCase()));
            teamChannel.addTeamVoiceChannel(team.name(), League.valueOf(league.toUpperCase()));

            EmbedBuilder embedBuilder = new EmbedBuilder();
            File file = new File("src/main/resources/images/osl-logo.png");
            embedBuilder.setTitle("Oceanic Slapshot League Team Created")
                    .setDescription("Welcome to OSL's current season of **Slapshot:Rebound** :hockey:. Congratulations on " +
                                    "the successful registration of **" + team.name()+"** :star_struck:!! We welcome you aboard, " +
                                    "and encourage you to keep an eye out on your league's fixture channel. If you have " +
                                    "any issues feel free to reach out to a **League Coordinator**. Most importantly " +
                                    "have fun and keep slappin! :thumbsup:")
                    .setColor(Color.cyan)
                    .addField("Team Name", team.name(), true)
                    .addField("Team ID", team.nameAbbr(), true)
                    .addField("Captain", "<@" + team.captain().discordId() + ">", true)
                    .addField("Players", playerBuilder.toString(), true)
                    .addField("League", league, true)
                    .setThumbnail("attachment://osl-logo.png")
                    .setFooter(guild.getSelfMember().getEffectiveName(), guild.getSelfMember().getEffectiveAvatarUrl());

            guild.getMemberById(team.captain().discordId()).getUser().openPrivateChannel()
                    .queue(ch -> ch.sendMessageEmbeds(embedBuilder.build()).addFiles(FileUpload.fromData(file, "osl-logo.png")).queue());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void assignFreeAgent(long discordId, League league) {
        new Roles(guild)
                .giveRole(
                        Objects.requireNonNull(Objects.requireNonNull(guild).getMemberById(discordId))
                                .getUser(), league.label +
                                " Free Agent");
        try {
            Database database = new Database();
            database.addFreeAgent(discordId, league);
            log.info(discordId + " has become a free agent");
            Roster roster = new Roster(guild);
            roster.addToRoster(discordId, league);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
