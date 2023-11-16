package org.bot;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bot.commands.slash.CommandInitializer;
import org.bot.commands.slash.AdminLeagueRegistration;
import org.bot.commands.slash.LeagueRegistration;
import org.bot.components.Buttons;
import org.bot.components.Modals;
import org.bot.components.SelectMenus;
import org.bot.converters.Config;

@Slf4j
public class Main {
    private static Config config = new Config();
    private static JDA jda;

    public static void main(String[] args) {
        try {
            jda = JDABuilder.createDefault(config.getToken())
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT) // enables explicit access to message.getContentDisplay()
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    // Event listeners to be added
                    .addEventListeners(
                            new CommandInitializer(),
                            new LeagueRegistration(),
                            new SelectMenus(),
                            new Buttons(),
                            new Modals(),
                            new AdminLeagueRegistration())
                    .build();

            jda.awaitReady();

            // Sets status and activity
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.getPresence().setActivity(Activity.playing("Slapshot: Rebound"));

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Cleaning up...");
                jda.shutdownNow(); // Shutdown the JDA instance
            }));

        } catch (InterruptedException e) {
            jda.shutdownNow();
            throw new RuntimeException(e);
        }
    }
}
