
CREATE TABLE sc_websites_icons (
    iconsid int(11) NOT NULL,
    iconsname varchar(1000) NOT NULL,
    iconsdescription varchar(2000) NOT NULL,
    iconsaddress varchar(1000) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_websites_site (
    siteid int(11) NOT NULL,
    sitename varchar(1000) NOT NULL,
    sitedescription varchar(2000),
    sitepage varchar(1000) NOT NULL,
    sitetype int(11) NOT NULL,
    siteauthor varchar(1000) NOT NULL,
    sitedate varchar(10) NOT NULL,
    sitestate int(11) NOT NULL,
    instanceid varchar(50) NOT NULL,
    popup int(11) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_websites_siteicons (
    siteid int(11) NOT NULL,
    iconsid int(11) NOT NULL
) ENGINE=InnoDB;
