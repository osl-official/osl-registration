package org.bot.scripts;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bot.converters.Config;
import org.bot.converters.Database;
import org.bot.enums.League;
import org.bot.models.Player;
import org.bot.models.Team;

import java.awt.*;
import java.nio.channels.Channel;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

public class Roster {
    private final Config CONFIG = new Config();
    private Guild guild;
    public Roster(Guild guild) {
        this.guild = guild;
        TextChannel channel = guild.getTextChannelById(CONFIG.getRosterChannel());
        MessageHistory messageHistory = MessageHistory.getHistoryFromBeginning(channel).complete();

        if (messageHistory.getRetrievedHistory().isEmpty()) {
            initRoster();
        }
    }

    public void addToRoster(long discordId, League league) {
        TextChannel channel = guild.getTextChannelById(CONFIG.getRosterChannel());
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

    public void addToRoster(Team team) {
        TextChannel channel = guild.getTextChannelById(CONFIG.getRosterChannel());
        MessageHistory messageHistory = MessageHistory.getHistoryFromBeginning(channel).complete();

        for (Message message : messageHistory.getRetrievedHistory()) {
            MessageEmbed messageEmbed = message.getEmbeds().get(0);
            if (messageEmbed.getTitle().contains("Teams")) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle(messageEmbed.getTitle())
                        .setDescription(messageEmbed.getDescription())
                        .setColor(messageEmbed.getColor());

                switch (team.league()) {
                    case PRO -> {
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.OPEN.label)).findFirst().get());
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.INTERMEDIATE.label)).findFirst().get());
                        builder.addField(getTeamsField(team, messageEmbed));
                    }
                    case INTERMEDIATE -> {
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.OPEN.label)).findFirst().get());
                        builder.addField(getTeamsField(team, messageEmbed));
                        builder.addField(messageEmbed.getFields().stream()
                                .filter(field -> field.getName().equalsIgnoreCase(League.PRO.label)).findFirst().get());
                    }
                    case OPEN -> {
                        builder.addField(getTeamsField(team, messageEmbed));
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
        TextChannel channel = guild.getTextChannelById(CONFIG.getRosterChannel());
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
        TextChannel channel = guild.getTextChannelById(CONFIG.getRosterChannel());
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

                System.out.println("---" + sb + "---");
                System.out.println(sb.length());
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

    private MessageEmbed.Field getTeamsField(Team team, MessageEmbed messageEmbed) {
        Role role = new Roles(guild).getRole(team.name());

        for (MessageEmbed.Field field : messageEmbed.getFields()) {
            if (field.getName().equalsIgnoreCase(team.league().label)) {
                StringBuilder sb = new StringBuilder();

                if (!field.getValue().isBlank()) {
                    sb.append(System.lineSeparator());
                }

                sb.append(field.getValue())
                        .append(role.getAsMention()).append(System.lineSeparator())
                        .append("Captain: ")
                        .append("<@")
                        .append(team.captain().discordId())
                        .append(">").append(System.lineSeparator())
                        .append("Players: ");
                for (Player player : team.players()) {
                    sb.append("<@").append(player.discordId()).append("> ");
                }

                return new MessageEmbed.Field(field.getName(), sb.toString(), true);
            }
        }
        return null;
    }

    public void refreshRoster() {
        TextChannel channel = guild.getTextChannelById(CONFIG.getRosterChannel());
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
                                System.out.println(idLong);
                                removeFromRoster(idLong);
                            }
                        }
                    });
                }
            });
        }
        try {
            Database database = new Database();
            database.getFreeAgents().forEach(player -> addToRoster(player.discordId(), player.league()));
            database.getTakenTeamModels().forEach(this::addToRoster);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void initRoster() {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setDescription("A list of all teams participating in the current season.")
                .setColor(Color.CYAN)
                .setTitle("Teams")
                .addField("Pro", "", true)
                .addField("Intermediate", "", true)
                .addField("Open", "", true);

        guild.getTextChannelById(CONFIG.getRosterChannel()).sendMessageEmbeds(embedBuilder.build()).queue();

        embedBuilder.setTitle("Free Agents")
                .setDescription("A list of all free agents participating in the current season. " +
                        "If your team requires a free agent these are the players to speak to!");

        guild.getTextChannelById(CONFIG.getRosterChannel()).sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
