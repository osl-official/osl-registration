package org.bot.commands.slash;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.bot.scripts.HelpEmbed;

@Slf4j
@AllArgsConstructor
public class MiscCommands {
    private SlashCommandInteractionEvent event;
    public void help() {
        HelpEmbed helpEmbed = new HelpEmbed();

        event.replyEmbeds(helpEmbed.getHelpMessageEmbed())
                .setActionRow(helpEmbed.getSelectMenu())
                .queue();
    }
}
