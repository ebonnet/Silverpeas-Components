CREATE TABLE sc_connecteurjdbc_connectinfo (
    id int(11) NOT NULL,
    jdbcdrivername varchar(250),
    jdbcurl varchar(250),
    login varchar(250),
    password varchar(250),
    sqlreq varchar(4000),
    rowlimit int(11),
    instanceid varchar(50) NOT NULL
) ENGINE=InnoDB;
