create table FreeAgents
(
    discordID BIGINT
        references Players,
    league    STRING not null
);

