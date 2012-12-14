CREATE TABLE sc_classifieds_classifieds (
    classifiedid int(11) NOT NULL,
    instanceid varchar(50) NOT NULL,
    title varchar(255) NOT NULL,
    creatorid varchar(50) NOT NULL,
    creationdate varchar(13) NOT NULL,
    updatedate varchar(13),
    `status` varchar(50) NOT NULL,
    validatorid varchar(50),
    validatedate varchar(13)
) ENGINE=InnoDB;

CREATE TABLE sc_classifieds_subscribes (
    subscribeid int(11) NOT NULL,
    userid varchar(50) NOT NULL,
    instanceid varchar(50) NOT NULL,
    field1 varchar(100) NOT NULL,
    field2 varchar(100) NOT NULL
) ENGINE=InnoDB;
