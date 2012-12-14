CREATE TABLE sc_forums_forum (
    forumid int(11) NOT NULL,
    forumname varchar(1000) NOT NULL,
    forumdescription varchar(2000),
    forumcreationdate varchar(50) NOT NULL,
    forumclosedate varchar(50),
    forumcreator varchar(255) NOT NULL,
    forumactive int(11) NOT NULL,
    forumparent int(11) DEFAULT 0 NOT NULL,
    forummodes varchar(50),
    forumlocklevel int(11),
    instanceid varchar(50) NOT NULL,
    categoryid varchar(50)
) ENGINE=InnoDB;

CREATE TABLE sc_forums_historyuser (
    userid varchar(255) NOT NULL,
    messageid int(11) NOT NULL,
    lastaccess varchar(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_forums_message (
    messageid int(11) NOT NULL,
    messagetitle varchar(1000) NOT NULL,
    messageauthor varchar(255) NOT NULL,
    forumid int(11) NOT NULL,
    messageparentid int(11),
    messagedate timestamp,
    `status` varchar(50)
) ENGINE=InnoDB;

CREATE TABLE sc_forums_rights (
    userid varchar(255) NOT NULL,
    forumid varchar(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_forums_subscription (
    userid varchar(255) NOT NULL,
    messageid varchar(255) NOT NULL
) ENGINE=InnoDB;
