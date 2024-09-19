package org.bot.commands.slash;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import org.bot.converters.JsonConverter;
import org.bot.models.Setting;
import org.bot.models.entity.Player;
import org.bot.models.entity.Team;
import org.bot.models.entity.TeamPlayer;
import org.bot.scripts.CommandLogger;
import org.bot.scripts.RegistrationMessage;
import org.bot.scripts.ReplyEphemeral;
import org.bot.scripts.Roles;
import org.bot.service.FreeAgentService;
import org.bot.service.TeamPlayerService;
import org.bot.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LeagueRegistration {
    private final Setting setting;
    private final int MESSAGE_TIMEOUT = 10;
    private final CommandLogger COMMAND_LOGGER;
    private final ReplyEphemeral replyEphemeral;
    private final SlashCommandInteractionEvent event;

    @Autowired
    public LeagueRegistration(Setting setting, @Autowired(required = false) SlashCommandInteractionEvent event) {
        this.setting = setting;
        this.replyEphemeral = new ReplyEphemeral(event);
        this.event = event;
        this.COMMAND_LOGGER = new CommandLogger(setting);
    }

    public void registerFreeAgentEvent() {
        FreeAgentService freeAgentService = new FreeAgentService(setting);
        TeamPlayerService teamPlayerService = new TeamPlayerService(setting);

        if (COMMAND_LOGGER.checkPendingRequest(event.getUser().getIdLong(), "free-agent")) {
            replyEphemeral.sendThenDelete(
                    "You already have a Free Agent request. Contact a **League Coordinator** if you believe this to be wrong",
                    MESSAGE_TIMEOUT, TimeUnit.SECONDS
            );
            return;
        }
        if (freeAgentService.existsById(event.getUser().getIdLong())) {
            replyEphemeral.sendThenDelete(
                    "You are already a Free Agent contact a **League Coordinator** if you believe this to be wrong",
                    MESSAGE_TIMEOUT, TimeUnit.SECONDS
            );
            return;
        }
        if (teamPlayerService.findById(event.getUser().getIdLong()).isPresent()) {
            replyEphemeral.sendThenDelete(
                    "You can't be a Free Agent while in a League Team, contact a **League Coordinator** if you believe this to be wrong",
                    MESSAGE_TIMEOUT, TimeUnit.SECONDS
            );
            return;
        }

        TextInput confirm = TextInput.create("confirm", "Confirmation", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("Type `confirm` if you have read and agree to #rules")
                .build();

        Modal modal = Modal.create("confirm-fa", "Confirm Free Agent Registration")
                .addActionRow(confirm)
                .build();

        event.replyModal(modal).queue();
    }

    public void registerTeamEvent() {
        FreeAgentService freeAgentService = new FreeAgentService(setting);
        TeamPlayerService teamPlayerService = new TeamPlayerService(setting);
        TeamService teamService = new TeamService(setting);

        List<OptionMapping> players = event.getOptionsByType(OptionType.USER);
        StringBuilder sb = new StringBuilder();
        if (COMMAND_LOGGER.checkPendingRequest(event.getUser().getIdLong(), "team")) {
            replyEphemeral.sendThenDelete(
                    "You already have a Team request. Contact a **League Coordinator** if you believe this to be wrong",
                    MESSAGE_TIMEOUT, TimeUnit.SECONDS
            );
            return;
        }

        if (teamPlayerService.findById(event.getUser().getIdLong()).isPresent()) {
            replyEphemeral.sendThenDelete(
                    "You are already in a team.",
                    MESSAGE_TIMEOUT, TimeUnit.SECONDS
            );
            return;
        }

        for (OptionMapping player:players) {
            if (player.getAsUser().isBot()) {
                replyEphemeral.sendThenDelete(
                        "Players must not be a Bot",
                        MESSAGE_TIMEOUT, TimeUnit.SECONDS
                );
                return;
            }
            if (players.stream().filter(p->p.getAsUser().equals(player.getAsUser())).count() > 1) {
                replyEphemeral.sendThenDelete(
                        "Do not repeat players",
                        MESSAGE_TIMEOUT, TimeUnit.SECONDS
                );
                return;
            }
            if (players.stream().filter(p->p.getAsUser().equals(event.getUser())).count() > 1) {
                replyEphemeral.sendThenDelete(
                        "Do not include yourself as a player, you are automatically included",
                        MESSAGE_TIMEOUT, TimeUnit.SECONDS
                );
                return;
            }

            if (teamPlayerService.findById(event.getUser().getIdLong()).isPresent()) {
                sb.append(player.getAsUser().getAsMention()).append(System.lineSeparator());
            }
        }

        if (!sb.toString().isBlank()) {
            replyEphemeral.sendThenDelete("Team cannot be made. The following Players are already in a team:\n" +
                    sb.toString(), 30, TimeUnit.SECONDS);
            return;
        }

        List<Team> teams = teamService.findAllByLeagueIsNull();

        StringSelectMenu.Builder teamsSelectMenu = StringSelectMenu.create("choose-team")
                .addOption("Create New Team", "new");

        for (Team team: teams) {
            teamsSelectMenu.addOption(team.getTeamName(), team.getTeamID().toLowerCase());
        }

        replyEphemeral.sendEmbedThenDelete(new EmbedBuilder()
                        .setTitle("Select Team you wish to form")
                        .addField("Team", getTeamsMessage(event.getUser(), event.getOptionsByType(OptionType.USER)), true)
                        .setFooter("This message will be deleted after 5 minutes if there is no team selected.")
                        .build(),
                5, TimeUnit.MINUTES, teamsSelectMenu.build()
        );
    }

    private String getTeamsMessage(User captain, List<OptionMapping> players) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Captain: ")
                .append(captain.getAsMention())
                .append("\nPlayers: ");
        for (OptionMapping player: players) {
            stringBuilder.append(player.getAsUser().getAsMention())
                    .append(" ");
        }

        return stringBuilder.toString();
    }

    @SneakyThrows
    public void teamTemplateEvent() {
        JsonConverter jsonConverter = new JsonConverter(setting);
        String path = "/images/json-icon.png";

        @Cleanup InputStream inputStream = getClass().getResourceAsStream(path);

        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle("Team Template JSON")
                .setThumbnail("attachment://json-icon.png")
                .setColor(9926568)
                .addField("Notes:", "- Slap ID are option fields and are not required.\n" +
                        "- A minimum of 2 players and a max of 4 must be entered.", false)
                .setFooter("If you have any questions or issues feel free to reach out to a League Coordinator",
                        event.getGuild().getIconUrl());

        assert inputStream != null;
        event.replyEmbeds(builder.build())
                .addFiles(jsonConverter.generateTemplateJson(),
                        FileUpload.fromData(inputStream, "json-icon.png")).complete();
    }

    public void teamTemplateUploadEvent() {
        Message.Attachment file = event.getOptionsByType(OptionType.ATTACHMENT).get(0).getAsAttachment();

        if (!file.getFileExtension().equalsIgnoreCase("json")) {
            replyEphemeral.sendThenDelete("Invalid file type. Please upload a .json file instead.",
                    10, TimeUnit.SECONDS);
            return;
        }

        if (COMMAND_LOGGER.checkPendingRequest(event.getUser().getIdLong(), "team")) {
            replyEphemeral.sendThenDelete(
                    "You already have a Team request. Contact a **League Coordinator** if you believe this to be wrong",
                    MESSAGE_TIMEOUT, TimeUnit.SECONDS
            );
            return;
        }

        try {
            InputStream inputStream = file.getProxy().download().get();
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader
                    (inputStream, StandardCharsets.UTF_8))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }

                Team newTeam = new JsonConverter(setting).jsonToTeam(textBuilder.toString());
                List<TeamPlayer> newTeamPlayers = new JsonConverter(setting).jsonToPlayers(textBuilder.toString());

                TeamPlayer captain = newTeamPlayers.stream().filter(TeamPlayer::isCaptain).findFirst().get();
                List<Player> newPlayers = new ArrayList<>();
                newTeamPlayers.stream()
                        .filter(p -> !p.isCaptain())
                        .forEach(p -> newPlayers.add(p.getPlayer()));

                new RegistrationMessage(event.getJDA(), setting).leagueTeam(newTeam, captain.getPlayer().getDiscordId(),
                        newPlayers,
                        new Roles(event.getGuild()).getTeamRoleField(newTeam.getTeamName()));

                replyEphemeral.sendThenDelete("Valid JSON! Team registration now awaiting approval.", 10,
                        TimeUnit.SECONDS);

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                replyEphemeral.sendThenDelete(e.getLocalizedMessage(), 10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
