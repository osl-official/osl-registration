package org.bot.scripts;

import lombok.Cleanup;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.FileUpload;
import org.bot.models.entity.FreeAgent;
import org.bot.models.Setting;
import org.bot.models.entity.Player;
import org.bot.models.entity.Team;
import org.bot.models.entity.TeamPlayer;
import org.bot.service.TeamPlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Component
public class RegistrationMessage {
    private final String PENDING_IMG = "/images/pending.png";
    private JDA jda;
    private final Setting setting;

    @Autowired
    public RegistrationMessage(@Autowired(required = false) JDA jda, Setting setting) {
        this.jda = jda;
        this.setting = setting;
    }

    @SneakyThrows
    public void freeAgent(Player freeAgent) {
        @Cleanup InputStream inputStream = getClass().getResourceAsStream(PENDING_IMG);
        this.jda.getChannelById(TextChannel.class, setting.getFaRegistration())
                .sendMessageEmbeds(new EmbedBuilder()
                        .setThumbnail("attachment://pending.png")
                        .setTitle("Free Agent Request")
                        .setColor(Color.ORANGE)
                        .setDescription(this.jda.getUserById(freeAgent.getDiscordId()).getAsMention() + " has requested to be a Free Agent")
                        .addField("Player", "DiscordID: " + freeAgent.getDiscordId(), true)
                        .build())
                .setComponents(
                        ActionRow.of(Button.danger("deny", "Deny").withEmoji(Emoji.fromUnicode("U+2716"))),
                        ActionRow.of(StringSelectMenu.create("league-selector")
                                .addOption("Pro", "pro")
                                .addOption("Intermediate", "im")
                                .addOption("Open", "open").build()))
                .addFiles(FileUpload.fromData(inputStream, "pending.png"))
                .complete();
    }

    @SneakyThrows
    public void leagueTeam(Team team, long captainId, List<Player> players, MessageEmbed.Field rolesField) {
        StringBuilder playersField = new StringBuilder();
        playersField.append("Captain: <@")
                .append(captainId)
                .append(">\nPlayers: ");
        for (Player player: players) {
            playersField.append("<@")
                    .append(player.getDiscordId())
                    .append("> ");
        }
        @Cleanup InputStream inputStream = getClass().getResourceAsStream(PENDING_IMG);
        this.jda.getChannelById(TextChannel.class, setting.getTeamRegistration())
                .sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Request for Team Creation")
                        .setThumbnail("attachment://pending.png")
                        .setColor(Color.ORANGE)
                        .addField("Team", playersField.toString(), true)
                        .addField("Team Name", team.getTeamName(), true)
                        .addField("Team ID", team.getTeamID(), true)
                        .addField(rolesField)
                        .addField("Requested Time","<t:" + Instant.now().getEpochSecond() + ":f>", false)
                        .build())
                .setComponents(
                        ActionRow.of(Button.danger("deny", "Deny").withEmoji(Emoji.fromUnicode("U+2716"))),
                        ActionRow.of(StringSelectMenu.create("league-selector")
                                .addOption("Pro", "pro")
                                .addOption("Intermediate", "im")
                                .addOption("Open", "open").build()))
                .addFiles(FileUpload.fromData(inputStream, "pending.png"))
                .complete();
    }

    @SneakyThrows
    public void disbandTeam(Team team, User author, List<TeamPlayer> teamPlayers, boolean assignable, boolean deleteRole, boolean deleteChannels) {
        StringBuilder playersField = new StringBuilder();
        for (TeamPlayer player: teamPlayers) {
            playersField.append("<@")
                    .append(player.getPlayer().getDiscordId()).append(">")
                    .append(System.lineSeparator());
        }
        @Cleanup InputStream inputStream = getClass().getResourceAsStream(PENDING_IMG);
        this.jda.getChannelById(TextChannel.class, setting.getTeamRegistration())
                .sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Team Disband Confirmation")
                        .setThumbnail("attachment://pending.png")
                        .setDescription(author.getAsMention() + " has requested to disband **" +
                                        team.getTeamName() + "**. This will kick all players from the team and make the team available for new team registration")
                        .setColor(Color.ORANGE)
                        .addField("Team ID", team.getTeamID().toUpperCase(), true)
                        .addField("Players", playersField.toString(),true)
                        .addField("Assignable After Disband", String.valueOf(assignable), true)
                        .addField("Delete Role", String.valueOf(deleteRole), true)
                        .addField("Delete Channels", String.valueOf(deleteChannels), true)
                        .build())
                .setComponents(
                        ActionRow.of(Button.danger("deny", "Deny").withEmoji(Emoji.fromUnicode("U+2716")),
                                        Button.success("approve", "Approve").withEmoji(Emoji.fromUnicode("U+2705")))
                )
                .addFiles(FileUpload.fromData(inputStream, "pending.png"))
                .complete();
    }
}
