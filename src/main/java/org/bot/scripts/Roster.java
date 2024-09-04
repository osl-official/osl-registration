package org.bot.scripts;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bot.enums.League;
import org.bot.models.Setting;
import org.bot.models.entity.Team;
import org.bot.models.entity.TeamPlayer;
import org.bot.service.FreeAgentService;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Roster {
    private final Setting setting;
    private final Guild guild;

    public Roster(Setting setting, Guild guild) {
        this.setting = setting;
        this.guild = guild;
        initialize();
    }

    public void initialize() {
        TextChannel channel = guild.getTextChannelById(setting.getRosterChannel());
        assert channel != null;
        MessageHistory messageHistory = MessageHistory.getHistoryFromBeginning(channel).complete();

        if (messageHistory.getRetrievedHistory().isEmpty()) {
            initRoster();
        }
    }

    public void addToRoster(long discordId, League league) {
        TextChannel channel = guild.getTextChannelById(setting.getRosterChannel());
        assert channel != null;
        MessageHistory messageHistory = MessageHistory.getHistoryFromBeginning(channel).complete();

        for (Message message : messageHistory.getRetrievedHistory()) {
            MessageEmbed messageEmbed = message.getEmbeds().get(0);
            if (Objects.requireNonNull(messageEmbed.getTitle()).equalsIgnoreCase("Free Agents")) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle(messageEmbed.getTitle())
                        .setDescription(messageEmbed.getDescription())
                        .setColor(messageEmbed.getColor());

                switch (league) {
                    case PRO -> {
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.OPEN.label)).findFirst().get());
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.INTERMEDIATE.label)).findFirst().get());
                        builder.addField(getFreeAgentField(discordId, league, messageEmbed));
                    }
                    case INTERMEDIATE -> {
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.OPEN.label)).findFirst().get());
                        builder.addField(getFreeAgentField(discordId, league, messageEmbed));
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.PRO.label)).findFirst().get());
                    }
                    case OPEN -> {
                        builder.addField(getFreeAgentField(discordId, league, messageEmbed));
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.INTERMEDIATE.label)).findFirst().get());
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.PRO.label)).findFirst().get());
                    }
                }
                message.editMessageEmbeds(builder.build()).queue();
            }
        }
    }

    public void addToRoster(Team team, List<TeamPlayer> players) {
        TextChannel channel = guild.getTextChannelById(setting.getRosterChannel());
        MessageHistory messageHistory = MessageHistory.getHistoryFromBeginning(channel).complete();

        for (Message message : messageHistory.getRetrievedHistory()) {
            MessageEmbed messageEmbed = message.getEmbeds().get(0);
            if (messageEmbed.getTitle().contains("Teams")) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle(messageEmbed.getTitle())
                        .setDescription(messageEmbed.getDescription())
                        .setColor(messageEmbed.getColor());

                switch (team.getLeague()) {
                    case PRO -> {
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.OPEN.label)).findFirst().get());
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.INTERMEDIATE.label)).findFirst().get());
                        builder.addField(getTeamsField(team, players, messageEmbed));
                    }
                    case INTERMEDIATE -> {
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.OPEN.label)).findFirst().get());
                        builder.addField(getTeamsField(team, players, messageEmbed));
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.PRO.label)).findFirst().get());
                    }
                    case OPEN -> {
                        builder.addField(getTeamsField(team, players, messageEmbed));
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.INTERMEDIATE.label)).findFirst().get());
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.PRO.label)).findFirst().get());
                    }
                }
                message.editMessageEmbeds(builder.build()).queue();
            }
        }
    }

    public void removeFromRoster(long discordId) {
        TextChannel channel = guild.getTextChannelById(setting.getRosterChannel());
        assert channel != null;
        MessageHistory messageHistory = MessageHistory.getHistoryFromBeginning(channel).complete();

        for (Message message : messageHistory.getRetrievedHistory()) {
            MessageEmbed messageEmbed = message.getEmbeds().get(0);
            if (Objects.requireNonNull(messageEmbed.getTitle()).contains("Free Agents")) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle(messageEmbed.getTitle())
                        .setDescription(messageEmbed.getDescription())
                        .setColor(messageEmbed.getColor());

                messageEmbed.getFields().forEach(field -> {
                    String value = field.getValue().replaceAll("(<@" + discordId + ">)", System.lineSeparator());
                    builder.addField(field.getName(), value, true);
                });

                message.editMessageEmbeds(builder.build()).queue();
            }
        }
    }

    public void removeFromRoster(Role teamRole) {
        TextChannel channel = guild.getTextChannelById(setting.getRosterChannel());
        MessageHistory messageHistory = MessageHistory.getHistoryFromBeginning(channel).complete();

        for (Message message : messageHistory.getRetrievedHistory()) {
            MessageEmbed messageEmbed = message.getEmbeds().get(0);
            if (messageEmbed.getTitle().contains("Teams")) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle(messageEmbed.getTitle())
                        .setDescription(messageEmbed.getDescription())
                        .setColor(messageEmbed.getColor());

                messageEmbed.getFields().forEach(field -> {
                    StringBuilder sb = new StringBuilder();
                    for (String team : field.getValue().split("<@&")) {
                        if (!team.contains(teamRole.getId()) && team.length() > 1) {
                            sb.append("<@&").append(team);
                        }
                    }
                    builder.addField(field.getName(), sb.toString(), true);
                });

                message.editMessageEmbeds(builder.build()).queue();
            }
        }
    }

    private MessageEmbed.Field getFreeAgentField(Long discordId, League league, MessageEmbed messageEmbed) {
        for (MessageEmbed.Field field : messageEmbed.getFields()) {
            if (Objects.requireNonNull(field.getName()).equalsIgnoreCase(league.label)) {
                StringBuilder sb = new StringBuilder();
                sb.append(field.getValue());
                if (Objects.requireNonNull(field.getValue()).length() > 1) {
                    sb.append(System.lineSeparator());
                }
                sb.append("<@")
                        .append(discordId)
                        .append(">");

                return new MessageEmbed.Field(field.getName(), sb.toString(), true);
            }
        }
        return null;
    }

    private MessageEmbed.Field getTeamsField(Team team, List<TeamPlayer> playerList, MessageEmbed messageEmbed) {
        Role role = new Roles(guild).getRole(team.getTeamName());
        TeamPlayer captain = playerList.stream().filter(TeamPlayer::isCaptain).findFirst().get();

        for (MessageEmbed.Field field : messageEmbed.getFields()) {
            if (field.getName().equalsIgnoreCase(team.getLeague().label)) {
                StringBuilder sb = new StringBuilder();

                if (!field.getValue().isBlank()) {
                    sb.append(System.lineSeparator());
                }

                sb.append(field.getValue())
                        .append(role.getAsMention()).append(System.lineSeparator())
                        .append("Captain: ")
                        .append("<@")
                        .append(captain.getPlayer().getDiscordId())
                        .append(">").append(System.lineSeparator())
                        .append("Players: ");
                for (TeamPlayer player : playerList.stream().filter(p -> !p.isCaptain()).toList()) {
                    sb.append("<@").append(player.getPlayer().getDiscordId()).append("> ");
                }

                return new MessageEmbed.Field(field.getName(), sb.toString(), true);
            }
        }
        return null;
    }

    public void refreshRoster() {
        TextChannel channel = guild.getTextChannelById(setting.getRosterChannel());
        MessageHistory messageHistory = MessageHistory.getHistoryFromBeginning(channel).complete();

        // Clears Roster
        for (Message message : messageHistory.getRetrievedHistory()) {
            message.getEmbeds().forEach(messageEmbed -> {
                if (messageEmbed.getTitle().contains("Teams")) {
                    messageEmbed.getFields().forEach(field -> Arrays.stream(field.getValue().split(System.lineSeparator()))
                            .filter(s -> s.matches("(<@&\\d+>)"))
                            .forEach(id -> removeFromRoster(
                                    guild.getRoleById(id.replace("<@&", "").replace(">", "")))));
                } else if (messageEmbed.getTitle().contains("Free Agents")) {
                    messageEmbed.getFields().forEach(field -> {
                        for (String id : field.getValue().split(System.lineSeparator())) {
                            if (id.length() > 1) {
                                long idLong = Long.parseLong(id.replaceAll("[^\\d]", ""));
                                removeFromRoster(idLong);
                            }
                        }
                    });
                }
            });
        }
        FreeAgentService service = new FreeAgentService(setting);
        service.findAll().forEach(p -> addToRoster(p.getPlayer().getDiscordId(), p.getLeague()));
    }

    private void initRoster() {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setDescription("A list of all teams participating in the current season.")
                .setColor(Color.CYAN)
                .setTitle("Teams")
                .addField("Pro", "", true)
                .addField("Intermediate", "", true)
                .addField("Open", "", true);

        guild.getTextChannelById(setting.getRosterChannel()).sendMessageEmbeds(embedBuilder.build()).queue();

        embedBuilder.setTitle("Free Agents")
                .setDescription("A list of all free agents participating in the current season. " +
                        "If your team requires a free agent these are the players to speak to!");

        guild.getTextChannelById(setting.getRosterChannel()).sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
