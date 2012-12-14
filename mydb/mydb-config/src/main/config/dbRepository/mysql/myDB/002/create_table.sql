CREATE TABLE sc_mydb_connectinfo (
    id int(11) NOT NULL,
    jdbcdrivername varchar(250),
    jdbcurl varchar(250),
    login varchar(250),
    password varchar(250),
    tablename varchar(100),
    rowlimit int(11),
    instanceid varchar(50) NOT NULL
) ENGINE=InnoDB;
