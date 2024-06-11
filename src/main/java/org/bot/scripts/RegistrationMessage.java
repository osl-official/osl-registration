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
import org.bot.converters.Config;
import org.bot.models.Player;
import org.bot.models.Team;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RegistrationMessage {
    private final String PENDING_IMG = "/images/pending.png";
    private JDA jda;
    private final Config config = new Config();

    public RegistrationMessage(JDA jda) {
        this.jda = jda;
    }

    @SneakyThrows
    public void freeAgent(Player player) {
        @Cleanup InputStream inputStream = getClass().getResourceAsStream(PENDING_IMG);
        this.jda.getChannelById(TextChannel.class, config.getFaRegistrationChannel())
                .sendMessageEmbeds(new EmbedBuilder()
                        .setThumbnail("attachment://pending.png")
                        .setTitle("Free Agent Request")
                        .setColor(Color.ORANGE)
                        .setDescription(this.jda.getUserById(player.discordId()).getAsMention() + " has requested to be a Free Agent")
                        .addField("Player", "DiscordID: " + player.discordId() + "\nSlapID: " + player.slapId().orElse(0), true)
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
    public void leagueTeam(Team team, MessageEmbed.Field rolesField) {
        StringBuilder playersField = new StringBuilder();
        playersField.append("Captain: <@")
                .append(team.captain().discordId())
                .append(">\nPlayers: ");
        for (Player player: team.players()) {
            playersField.append("<@")
                    .append(player.discordId())
                    .append("> ");
        }
        @Cleanup InputStream inputStream = getClass().getResourceAsStream(PENDING_IMG);
        this.jda.getChannelById(TextChannel.class, config.getTeamRegistrationChannel())
                .sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Request for Team Creation")
                        .setThumbnail("attachment://pending.png")
                        .setColor(Color.ORANGE)
                        .addField("Team", playersField.toString(), true)
                        .addField("Team Name", team.name(), true)
                        .addField("Team ID", team.nameAbbr().toString(), true)
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
    public void disbandTeam(HashMap<String, String> team, User author, List<Player> players, boolean assignable, boolean deleteRole, boolean deleteChannels) {
        StringBuilder playersField = new StringBuilder();
        for (Player player: players) {
            playersField.append("<@")
                    .append(player.discordId()).append(">")
                    .append(System.lineSeparator());
        }
        @Cleanup InputStream inputStream = getClass().getResourceAsStream(PENDING_IMG);
        this.jda.getChannelById(TextChannel.class, config.getTeamRegistrationChannel())
                .sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Team Disband Confirmation")
                        .setThumbnail("attachment://pending.png")
                        .setDescription(author.getAsMention() + " has requested to disband **" +
                                        team.get("teamName") + "**. This will kick all players from the team and make the team available for new team registration")
                        .setColor(Color.ORANGE)
                        .addField("Team ID", team.get("teamID").toUpperCase(), true)
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
