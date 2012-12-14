CREATE TABLE sc_resources_category (
    id bigint NOT NULL,
    instanceid varchar(50) NOT NULL,
    name varchar(50) NOT NULL,
    creationdate varchar(20) NOT NULL,
    updatedate varchar(20) NOT NULL,
    bookable int(11),
    form varchar(50),
    responsibleid int(11),
    createrid varchar(50),
    updaterid varchar(50),
    description varchar(2000)
) ENGINE=InnoDB;

CREATE TABLE sc_resources_managers (
    resourceid bigint NOT NULL,
    managerid bigint NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_resources_reservation (
    id bigint NOT NULL,
    instanceid varchar(50) NOT NULL,
    evenement varchar(128) NOT NULL,
    userid int(11) NOT NULL,
    creationdate varchar(20) NOT NULL,
    updatedate varchar(20) NOT NULL,
    begindate varchar(20) NOT NULL,
    enddate varchar(20) NOT NULL,
    reason varchar(2000),
    place varchar(128),
    `status` varchar(50)
) ENGINE=InnoDB;

CREATE TABLE sc_resources_reservedresource (
    reservationid bigint NOT NULL,
    resourceid bigint NOT NULL,
    `status` varchar(50)
) ENGINE=InnoDB;

CREATE TABLE sc_resources_resource (
    id bigint NOT NULL,
    instanceid varchar(50) NOT NULL,
    categoryid bigint NOT NULL,
    name varchar(128) NOT NULL,
    creationdate varchar(20) NOT NULL,
    updatedate varchar(20) NOT NULL,
    bookable int(11),
    responsibleid int(11),
    createrid varchar(50),
    updaterid varchar(50),
    description varchar(2000)
) ENGINE=InnoDB;

