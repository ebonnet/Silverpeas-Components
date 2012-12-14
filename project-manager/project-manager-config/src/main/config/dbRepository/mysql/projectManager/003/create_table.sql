CREATE TABLE sc_projectmanager_tasks (
    id int(11) NOT NULL,
    mereid int(11),
    chrono int(11) NOT NULL,
    nom varchar(100) NOT NULL,
    description varchar(500),
    organisateurid int(11) NOT NULL,
    responsableid int(11) NOT NULL,
    charge double precision,
    consomme double precision,
    raf double precision,
    avancement int(11),
    statut int(11),
    datedebut varchar(10) NOT NULL,
    datefin varchar(10) NOT NULL,
    codeprojet varchar(50),
    descriptionprojet varchar(100),
    estdecomposee int(11),
    instanceid varchar(50) NOT NULL,
    path varchar(50) NOT NULL,
    previousid int(11)
) ENGINE=InnoDB;

CREATE TABLE sc_projectmanager_calendar (
    holidaydate varchar(10) NOT NULL,
    fatherid int(11) NOT NULL,
    instanceid varchar(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_projectmanager_resources (
    id int(11) NOT NULL,
    taskid int(11) NOT NULL,
    resourceid int(11) NOT NULL,
    charge int(11) NOT NULL,
    instanceid varchar(50) NOT NULL
) ENGINE=InnoDB;
