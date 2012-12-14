CREATE TABLE sc_rss_channels (
    id int(11) NOT NULL,
    url varchar(1000) NOT NULL,
    refreshrate int(11) NOT NULL,
    nbdisplayeditems int(11) NOT NULL,
    displayimage int(11) NOT NULL,
    creatorid varchar(100) NOT NULL,
    creationdate char(10) NOT NULL,
    instanceid varchar(50) NOT NULL
) ENGINE=InnoDB;
