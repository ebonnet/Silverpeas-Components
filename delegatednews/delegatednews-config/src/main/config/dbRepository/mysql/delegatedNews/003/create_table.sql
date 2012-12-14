CREATE TABLE sc_delegatednews_news (
    pubid int(11) NOT NULL,
    instanceid varchar(50) NOT NULL,
    `status` varchar(100) NOT NULL,
    contributorid varchar(50) NOT NULL,
    validatorid varchar(50),
    validationdate timestamp ,
    begindate timestamp ,
    enddate timestamp,
    newsorder int(11) DEFAULT 0 NOT NULL
) ENGINE=InnoDB;
