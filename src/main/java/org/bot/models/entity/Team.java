package org.bot.models.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bot.enums.League;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "teams")
public class Team {

    @Id
    private String teamID;

    @Column(nullable = false)
    private String teamName;

    @Enumerated(EnumType.STRING)
    private League league;

    public Team(String teamID, String teamName, League league) {
        this.teamID = teamID;
        this.teamName = teamName;
        this.league = league;
    }
}
