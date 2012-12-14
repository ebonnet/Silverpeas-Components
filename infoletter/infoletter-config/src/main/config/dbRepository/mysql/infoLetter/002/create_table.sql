CREATE TABLE sc_il_letter (
    id int(11) NOT NULL,
    name varchar(1000) NOT NULL,
    description varchar(2000),
    periode varchar(255),
    instanceid varchar(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_il_publication (
    id int(11) NOT NULL,
    title varchar(1000) NOT NULL,
    description varchar(2000),
    parutiondate varchar(255),
    publicationstate int(11) NOT NULL,
    letterid int(11) NOT NULL,
    instanceid varchar(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_il_extsus (
    letter int(11) NOT NULL,
    email varchar(1000) NOT NULL,
    instanceid varchar(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_il_intsus (
    letter int(11) NOT NULL,
    userid varchar(255) NOT NULL,
    instanceid varchar(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_il_pubs (
    letter int(11) NOT NULL,
    userid varchar(255) NOT NULL,
    instanceid varchar(50) NOT NULL
) ENGINE=InnoDB;

