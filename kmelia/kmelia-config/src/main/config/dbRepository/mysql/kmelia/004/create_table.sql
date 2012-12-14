CREATE TABLE sc_kmelia_search (
    id bigint NOT NULL,
    instanceid varchar(50),
    topicid int(11) NOT NULL,
    userid int(11) NOT NULL,
    searchdate timestamp NOT NULL,
    language varchar(50),
    query varchar(255)
) ENGINE=InnoDB;
