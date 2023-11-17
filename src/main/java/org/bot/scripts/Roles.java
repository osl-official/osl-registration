package org.bot.scripts;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
public class Roles {
    private Guild guild;

    public void giveRole(@NotNull User user, @NotNull String roleName) {
        Role role = getRole(roleName);

        if (role != null) {
            guild.addRoleToMember(user, role).queue();
        } else {
            guild.createRole().setName(roleName)
                    .setMentionable(true)
                    .setHoisted(true)
                    .submit()
                    .thenCompose((r -> guild.addRoleToMember(user, r).submit()));
        }
    }

    public void giveRole(@NotNull Long discordID,@NotNull String roleName) {
        giveRole(Objects.requireNonNull(guild.getMemberById(discordID)).getUser(), roleName);
    }

    public void removeRole(@NotNull User user, String roleName) {
        Member member = guild.getMemberById(user.getId());
        assert member != null;
        member.getRoles().forEach(role -> {
            if (role.getName().contains(roleName)) {
                System.out.println(role);
                guild.removeRoleFromMember(user, role).queue();
            }
        });
    }

    public void removeRole(@NotNull Long discordID,@NotNull String roleName) {
        removeRole(Objects.requireNonNull(guild.getMemberById(discordID)).getUser(), roleName);
    }

    public void giveRoleToMultiple(@NotNull List<String> discordIDs,@NotNull String roleName) {
        discordIDs.forEach(discordID -> giveRole(Objects.requireNonNull(this.guild.getMemberById(discordID)).getUser(), roleName));
    }

    public boolean roleExist(@NotNull String roleName) {
        return guild.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(roleName));
    }

    public Role getRole(String roleName) {
        Optional<Role> roleOptional = this.guild.getRoles().stream()
                .filter(role -> role.getName().toLowerCase()
                        .contains(roleName.toLowerCase()))
                .findFirst();

        return roleOptional.orElse(null);
    }

    public MessageEmbed.Field getTeamRoleField(String teamName) {
        EmbedBuilder field = new EmbedBuilder();

        Optional<Role> roleOptional = guild.getRoles().stream()
                .filter(role -> role.getName().toLowerCase().contains(teamName.toLowerCase()))
                .findFirst();

        if (roleOptional.isEmpty()) {
            return field.addField("Role", "To be made if approved.", true).build().getFields().get(0);
        }

        return field.addField("Role", roleOptional.get().getAsMention(), true).build().getFields().get(0);
    }
}
