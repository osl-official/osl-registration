package org.bot.scripts;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot() && event.getMessage().toString().toLowerCase().contains("boktay")) {
            event.getMessage().delete().queue();
            event.getChannel().sendMessage("Hey! Don't say that he doesn't like it!!!").queue();
        }
    }
}
