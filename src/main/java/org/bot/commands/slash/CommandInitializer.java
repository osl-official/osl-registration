package org.bot.commands.slash;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bot.scripts.CommandLogger;
import org.bot.scripts.ReplyEphemeral;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CommandInitializer extends ListenerAdapter {
    private static final int MESSAGE_TIMEOUT = 5;

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        log.info("Updating Commands for Guild " + event.getGuild().getName());
        event.getJDA().updateCommands().addCommands(
                Commands.slash("register-fa", "Register to become a Free Agent"),
                Commands.slash("register-team", "Register a Team for the current season")
                        .addOption(OptionType.USER, "player1", "Player 1", true)
                        .addOption(OptionType.USER, "player2", "Player 2", true)
                        .addOption(OptionType.USER, "player3", "Player 3", false)
                        .addOption(OptionType.USER, "player4", "Player 4", false),
                Commands.slash("team-template", "Get a JSON template of a Team Object"),
                Commands.slash("register-team-upload", "Upload a JSON file containing your new Teams details. EXPERIMENTAL")
                        .addOption(OptionType.ATTACHMENT, "json", "Your teams JSON file", true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

                // ADMIN COMMANDS
                Commands.slash("remove-fa", "[ADMIN] Remove Free Agent")
                        .addOption(OptionType.USER, "freeagent", "Free Agent to remove", true)
                        .setDefaultPermissions(
                                DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                        ),
                Commands.slash("add-fa", "[ADMIN] Add Free Agent")
                        .addOption(OptionType.USER, "freeagent", "Free Agent to add", true)
                        .setDefaultPermissions(
                                DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                        ),
                Commands.slash("disband-team", "[ADMIN] Disband Selected Team")
                        .addOption(OptionType.ROLE, "teamrole", "Team to disband", true)
                        .addOption(OptionType.BOOLEAN, "assignable",
                                "Allow new registrations to use the following Team Name", true)
                        .addOption(OptionType.BOOLEAN, "delete-role",
                                "Delete Team Role. If empty false is assumed.", false)
                        .addOption(OptionType.BOOLEAN, "delete-channels",
                                "Delete Team Voice and Text Channels. If empty false is assumed.", false)
                        .setDefaultPermissions(
                                DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                        ),
                Commands.slash("create-team", "[ADMIN] Create new Team")
                        .addOption(OptionType.STRING, "teamname", "New Team's Name", true)
                        .addOption(OptionType.STRING, "teamid", "New Team's ID", true)
                        .setDefaultPermissions(
                                DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                        ),
                Commands.slash("refresh-roster", "[ADMIN] Refresh the roster according to the database")
                        .setDefaultPermissions(
                                DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                        ),
                Commands.slash("teams-json", "[ADMIN] Get all Teams in JSON format for personal view")
                        .setDefaultPermissions(
                                DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                        ),
                Commands.slash("free-agents-json", "[ADMIN] Get all Free Agents in JSON format for personal view")
                        .setDefaultPermissions(
                                DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                        ),

                // CRUD Commands
                Commands.slash("view-database", "[ADMIN] View the database in an Embed")
                        .setDefaultPermissions(
                                DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                        )
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String name = event.getName();
        switch (name.toLowerCase()) {
            case "register-fa" -> new LeagueRegistration(event).registerFreeAgentEvent();
            case "register-team" -> new LeagueRegistration(event).registerTeamEvent();
            case "team-template" -> new LeagueRegistration(event).teamTemplateEvent();
            case "register-team-upload" -> new LeagueRegistration(event).teamTemplateUploadEvent();

            default -> {
                if (!Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getMember(event.getUser()))
                        .hasPermission(Permission.ADMINISTRATOR)) {
                    return;
                }
                switch (name.toLowerCase()) {
                    case "remove-fa" -> new AdminLeagueRegistration(event).removeFreeAgent();
                    case "add-fa" -> new AdminLeagueRegistration(event).addFreeAgent();
                    case "disband-team" -> new AdminLeagueRegistration(event).disbandTeam();
                    case "create-team" -> new AdminLeagueRegistration(event).createTeam();
                    case "refresh-roster" ->  new AdminLeagueRegistration(event).refreshRoster();
                    case "teams-json" ->  new AdminLeagueRegistration(event).teamsToJson();
                    case "free-agents-json" ->  new AdminLeagueRegistration(event).faToJson();
                    case "view-database" ->  new AdminLeagueRegistration(event).viewDatabase();
                }
            }
        }
    }
}
