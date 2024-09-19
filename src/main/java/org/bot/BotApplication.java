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
import org.bot.components.Buttons;
import org.bot.components.Modals;
import org.bot.components.SelectMenus;
import org.bot.converters.AppConfig;
import org.bot.models.Setting;
import org.bot.scripts.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
@SpringBootApplication
public class BotApplication {
    private static JDA jda;
    @Value(value = "discordToken")
    private static String DISCORD_TOKEN;

    @Autowired
    public BotApplication(Setting setting) {
        try {
            jda = JDABuilder.createDefault(DISCORD_TOKEN)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .addEventListeners(
                            new CommandInitializer(setting),
                            new SelectMenus(setting),
                            new Buttons(setting),
                            new Modals(setting),
                            new MessageListener())
                    .build();

            jda.awaitReady();
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.getPresence().setActivity(Activity.playing("Slapshot: Rebound"));

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Cleaning up...");
                jda.shutdownNow();
            }));

        } catch (InterruptedException e) {
            jda.shutdownNow();
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
