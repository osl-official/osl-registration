package org.bot.models.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "TeamPlayers")
public class TeamPlayer {

    @Id
    private long id;

    private boolean isCaptain;

    @ManyToOne
    @JoinColumn(name = "discordID", referencedColumnName = "discordId")
    private Player player;

    @ManyToOne
    private Team team;
}
