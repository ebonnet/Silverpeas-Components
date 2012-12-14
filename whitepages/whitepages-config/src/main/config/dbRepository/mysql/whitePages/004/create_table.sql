CREATE TABLE sc_whitepages_card (
    id int(11) NOT NULL,
    userid varchar(50) NOT NULL,
    hidestatus int(11) NOT NULL,
    instanceid varchar(50) NOT NULL,
    creationdate varchar(10) DEFAULT '2003/01/01' NOT NULL,
    creatorid int(11) DEFAULT 0 NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_whitepages_searchfields (
    id varchar(255) NOT NULL,
    instanceid varchar(50) NOT NULL,
    fieldid varchar(50) NOT NULL
) ENGINE=InnoDB;
