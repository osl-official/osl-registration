package org.bot.factories;

import net.dv8tion.jda.api.entities.Guild;
import org.bot.models.Setting;
import org.bot.scripts.Roster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RosterFactory {
    private final Setting setting;

    @Autowired
    public RosterFactory(Setting setting) {
        this.setting = setting;
    }

    public Roster createRoster(Guild guild) {
        return new Roster(setting, guild);
    }
}
