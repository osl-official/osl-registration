package org.bot.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter @ToString
public class TeamDTO {
    @Setter
    private String teamName;
    @Setter
    private String teamId;
    @Setter
    private PlayerDTO captain;
    @Setter
    private List<PlayerDTO> players;
}
