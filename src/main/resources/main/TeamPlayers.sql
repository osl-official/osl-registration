create table TeamPlayers
(
    teamID    CHAR(4)
        references Teams,
    discordID BIGINT
        references Players
);

