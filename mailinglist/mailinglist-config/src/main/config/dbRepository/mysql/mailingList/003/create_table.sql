CREATE TABLE sc_mailinglist_attachment (
    id varchar(255) NOT NULL,
    version int(11) NOT NULL,
    attachmentsize bigint,
    attachmentpath varchar(255),
    filename varchar(255),
    contenttype varchar(255),
    md5signature varchar(255),
    messageid varchar(255)
) ENGINE=InnoDB;

CREATE TABLE sc_mailinglist_external_user (
    id varchar(255) NOT NULL,
    version int(11) NOT NULL,
    componentid varchar(255),
    email varchar(255) NOT NULL,
    listid varchar(255)
) ENGINE=InnoDB;

CREATE TABLE sc_mailinglist_internal_sub (
    id varchar(255) NOT NULL,
    version int(11) NOT NULL,
    subscriber_type varchar(255) NOT NULL,
    externalid varchar(255) NOT NULL,
    mailinglistid varchar(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_mailinglist_list (
    id varchar(255) NOT NULL,
    version int(11) NOT NULL,
    componentid varchar(255)
) ENGINE=InnoDB;

CREATE TABLE sc_mailinglist_message (
    id varchar(255) NOT NULL,
    version int(11) NOT NULL,
    mailid varchar(255) NOT NULL,
    componentid varchar(255) NOT NULL,
    title varchar(255),
    summary varchar(255),
    sender varchar(255),
    sentdate timestamp,
    referenceid varchar(255),
    moderated bool,
    contenttype varchar(255),
    attachmentssize bigint,
    messageyear int(11),
    messagemonth int(11),
    body longtext
) ENGINE=InnoDB;
