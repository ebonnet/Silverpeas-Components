CREATE TABLE sc_formsonline_forminstances (
    id int(11) NOT NULL,
    formid int(11) NOT NULL,
    state int(11) NOT NULL,
    creatorid varchar(20) NOT NULL,
    creationdate date NOT NULL,
    validatorid varchar(20),
    validationdate date,
    comments varchar(1000),
    instanceid varchar(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_formsonline_forms (
    id int(11) NOT NULL,
    xmlformname varchar(80) NOT NULL,
    name varchar(80) NOT NULL,
    description varchar(200),
    creationdate date NOT NULL,
    state int(11) NOT NULL,
    instanceid varchar(80) NOT NULL,
    alreadyused smallint DEFAULT 0 NOT NULL,
    creatorid varchar(20) NOT NULL,
    title varchar(200) DEFAULT '' NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_formsonline_grouprights (
    formid int(11) NOT NULL,
    instanceid varchar(80) NOT NULL,
    groupid varchar(20) NOT NULL,
    righttype char(1) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_formsonline_userrights (
    formid int(11) NOT NULL,
    instanceid varchar(80) NOT NULL,
    userid varchar(20) NOT NULL,
    righttype char(1) NOT NULL
) ENGINE=InnoDB;



