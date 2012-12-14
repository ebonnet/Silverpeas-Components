CREATE TABLE sc_scheduleevent_contributor (
    id varchar(255) NOT NULL,
    scheduleeventid varchar(255) NOT NULL,
    userid int(11) NOT NULL,
    lastvisit timestamp,
    lastvalidation timestamp
) ENGINE=InnoDB;

CREATE TABLE sc_scheduleevent_list (
    id varchar(255) NOT NULL,
    title varchar(255) NOT NULL,
    description varchar(4000),
    creationdate timestamp NOT NULL,
    `status` int(11) NOT NULL,
    creatorid int(11) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_scheduleevent_options (
    id varchar(255) NOT NULL,
    scheduleeventid varchar(255) NOT NULL,
    optionday timestamp NOT NULL,
    optionhour int(11) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_scheduleevent_response (
    id varchar(255) NOT NULL,
    scheduleeventid varchar(255) NOT NULL,
    userid int(11) NOT NULL,
    optionid varchar(255) NOT NULL
) ENGINE=InnoDB;

    