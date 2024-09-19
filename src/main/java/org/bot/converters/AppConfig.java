package org.bot.converters;

import org.bot.models.Setting;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(Setting.class)
public class AppConfig {
}
