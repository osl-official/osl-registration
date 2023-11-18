package org.bot.scripts;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.bot.converters.Database;
import org.bot.enums.League;
import org.bot.models.Team;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Registration {
    private final Guild guild;

    public Registration(Guild guild) {
        this.guild = guild;
    }

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

            database.setTeamTaken(team.nameAbbr(), team.captain().discordId());
            team.players().forEach(player -> {
                try {
                    database.addPlayerToTeam(player.discordId(), team.nameAbbr());
                    log.info(player.discordId() + " has been added to " + team.name());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            database.addCaptain(team.captain().discordId(), team.nameAbbr());
            log.info(team.captain().discordId() + " has been added to " + team.name());

            Roster roster = new Roster(guild);
            roster.addToRoster(new Team(team.captain(), team.players(), team.name(), team.nameAbbr(),
                    League.valueOf(league.toUpperCase())));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void assignFreeAgent(long discordId, League league) {
        new Roles(guild)
                .giveRole(
                        Objects.requireNonNull(Objects.requireNonNull(guild).getMemberById(discordId))
                                .getUser(), league.name() +
                                " Free Agent");
        try {
            Database database = new Database();
            database.addFreeAgent(discordId);
            log.info(discordId + " has become a free agent");
            Roster roster = new Roster(guild);
            roster.addToRoster(discordId, league);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



}
