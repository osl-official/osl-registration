package org.bot.commands.slash;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandInitializer extends ListenerAdapter {

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event.getJDA().updateCommands().addCommands(
                Commands.slash("register-fa", "Register to become a Free Agent"),
                Commands.slash("register-team", "Register a Team for the current season")
                        .addOption(OptionType.USER, "player1", "Player 1", true)
                        .addOption(OptionType.USER, "player2", "Player 2", true)
                        .addOption(OptionType.USER, "player3", "Player 3", false)
                        .addOption(OptionType.USER, "player4", "Player 4", false),

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
                        .setDefaultPermissions(
                                DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                        ),
                Commands.slash("create-team", "[ADMIN] Create new Team")
                        .addOption(OptionType.STRING, "teamname", "New Team's Name", true)
                        .addOption(OptionType.STRING, "teamid", "New Team's ID", true)
                        .setDefaultPermissions(
                                DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                        )
        ).queue();
    }
}
