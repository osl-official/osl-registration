create table Teams
(
    teamID    CHAR(4) not null
        primary key
        unique,
    teamName  STRING  not null,
    captainID BIGINT,
    league    STRING
);

