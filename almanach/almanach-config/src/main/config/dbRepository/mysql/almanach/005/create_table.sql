CREATE TABLE sc_almanach_event (
    eventid int(11) NOT NULL,
    eventname varchar(2000),
    eventstartday varchar(10) NOT NULL,
    eventendday varchar(10),
    eventdelegatorid varchar(100) NOT NULL,
    eventpriority int(11) NOT NULL,
    eventtitle varchar(2000) NOT NULL,
    instanceid varchar(50),
    eventstarthour varchar(5),
    eventendhour varchar(5),
    eventplace varchar(200),
    eventurl varchar(200)
) ENGINE=InnoDB;


CREATE TABLE sc_almanach_periodicity (
    id int(11) NOT NULL,
    eventid int(11) NOT NULL,
    unity int(11) NOT NULL,
    frequency int(11) NOT NULL,
    daysweekbinary varchar(7),
    numweek int(11),
    day int(11),
    untildateperiod varchar(10)
) ENGINE=InnoDB;

CREATE TABLE sc_almanach_periodicityexcept (
    id int(11) NOT NULL,
    periodicityid int(11) NOT NULL,
    begindateexception varchar(10),
    enddateexception varchar(10)
) ENGINE=InnoDB;
