package org.bot.scripts;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReplyEphemeral {
    private SlashCommandInteractionEvent event;

    public ReplyEphemeral(SlashCommandInteractionEvent event) {
        this.event = event;
    }

    public void sendThenDelete(String message, int timeValue, TimeUnit timeUnit) {
        this.event.reply(message +
                        "\n`This message will be deleted after " + timeValue + " " + timeUnit.name() + "`")
                .setEphemeral(true)
                .submit()
                .thenCompose((msg) -> msg.deleteOriginal().submitAfter(timeValue, timeUnit))
                .whenComplete((success, error) -> {
                    if (error != null) {
                        log.info("Ephemeral Message was dealt with before auto delete.");
                    }
                });
    }

    public void sendEmbedThenDelete(MessageEmbed messageEmbed, int timeValue, TimeUnit timeUnit, Collection<LayoutComponent> components) {
        this.event.replyEmbeds(messageEmbed)
                .setComponents(components)
                .setEphemeral(true)
                .submit()
                .thenCompose((msg) -> msg.deleteOriginal().submitAfter(timeValue, timeUnit))
                .whenComplete((success, error) -> {
                    if (error != null) {
                        log.info("Ephemeral Message was dealt with before auto delete.");
                    }
                });
    }

    public void sendEmbedThenDelete(MessageEmbed messageEmbed, int timeValue, TimeUnit timeUnit, ItemComponent component) {
        this.event.replyEmbeds(messageEmbed)
                .setActionRow(component)
                .setEphemeral(true)
                .submit()
                .thenCompose((msg) -> msg.deleteOriginal().submitAfter(timeValue, timeUnit))
                .whenComplete((success, error) -> {
                    if (error != null) {
                        log.info("Ephemeral Message was dealt with before auto delete.");
                    }
                });
    }
}
