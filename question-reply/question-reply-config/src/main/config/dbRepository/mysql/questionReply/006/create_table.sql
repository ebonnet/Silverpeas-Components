CREATE TABLE sc_questionreply_question (
    id int(11) NOT NULL,
    title varchar(100) NOT NULL,
    content varchar(2000),
    creatorid varchar(50) NOT NULL,
    creationdate varchar(10) NOT NULL,
    `status` int(11) NOT NULL,
    publicreplynumber int(11) NOT NULL,
    privatereplynumber int(11) NOT NULL,
    replynumber int(11) NOT NULL,
    instanceid varchar(50) NOT NULL,
    categoryid varchar(50)
) ENGINE=InnoDB;

CREATE TABLE sc_questionreply_recipient (
    id int(11) NOT NULL,
    questionid int(11) NOT NULL,
    userid varchar(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_questionreply_reply (
    id int(11) NOT NULL,
    questionid int(11) NOT NULL,
    title varchar(100) NOT NULL,
    content varchar(2000),
    creatorid varchar(50) NOT NULL,
    creationdate varchar(10) NOT NULL,
    publicreply int(11) NOT NULL,
    privatereply int(11) NOT NULL
) ENGINE=InnoDB;

