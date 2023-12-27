package org.bot.scripts;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.bot.enums.League;

import java.util.EnumSet;

@AllArgsConstructor
public class TeamChannel {
    private Guild guild;

    public void addTeamTextChannel(String teamName, League league) {
        if (channelExist(teamName.replace(" ", "-"), ChannelType.TEXT)) {
            guild.getTextChannelsByName(teamName.replace(" ", "-"), true).forEach(ch -> {
                ch.upsertPermissionOverride(guild.getPublicRole()).setDenied(Permission.VIEW_CHANNEL).setAllowed().queue();
                ch.upsertPermissionOverride(new Roles(guild).getRole(teamName)).setAllowed(Permission.VIEW_CHANNEL).queue();
                ch.upsertPermissionOverride(guild.getSelfMember()).setAllowed(Permission.MANAGE_CHANNEL).setAllowed(Permission.VIEW_CHANNEL).queue();
            });
        } else {
            guild.getCategoriesByName(league.name() + " League", true).forEach(
                    category -> guild.createTextChannel(teamName, category)
                            .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                            .addPermissionOverride(new Roles(guild).getRole(teamName), EnumSet.of(Permission.VIEW_CHANNEL), null)
                            .addPermissionOverride(guild.getSelfMember(), EnumSet.of(Permission.MANAGE_CHANNEL, Permission.VIEW_CHANNEL), null)
                            .queue()
            );
        }
    }

    public void addTeamVoiceChannel(String teamName, League league) {
        if (channelExist(teamName, ChannelType.VOICE)) {
            guild.getVoiceChannelsByName(teamName, true).forEach(ch -> {
                ch.upsertPermissionOverride(guild.getPublicRole()).setDenied(Permission.VIEW_CHANNEL).setAllowed().queue();
                ch.upsertPermissionOverride(new Roles(guild).getRole(teamName)).setAllowed(Permission.VIEW_CHANNEL).queue();
                ch.upsertPermissionOverride(guild.getSelfMember()).setAllowed(Permission.MANAGE_CHANNEL).setAllowed(Permission.VIEW_CHANNEL).queue();
            });
        } else {
            guild.getCategoriesByName(league.name() + " League", true).forEach(
                    category -> guild.createVoiceChannel(teamName, category)
                            .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                            .addPermissionOverride(new Roles(guild).getRole(teamName), EnumSet.of(Permission.VIEW_CHANNEL), null)
                            .addPermissionOverride(guild.getSelfMember(), EnumSet.of(Permission.MANAGE_CHANNEL, Permission.VIEW_CHANNEL), null)
                            .queue()
            );
        }
    }

    public void removeTeamTextChannel(String teamName) {
        teamName = teamName.replace(" ", "-");
        guild.getTextChannelsByName(teamName, true).forEach(ch -> ch.delete().submit());
    }

    public void removeTeamVoiceChannel(String teamName) {
        guild.getVoiceChannelsByName(teamName, true).forEach(ch -> ch.delete().complete());
    }

    private boolean channelExist(String channelName, ChannelType type) {
        for (Channel channel : guild.getChannels()) {
            if (channel.getType().equals(type) && channel.getName().toLowerCase().contains(channelName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
