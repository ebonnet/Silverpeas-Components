CREATE TABLE sc_gallery_photo (
    photoid int(11) NOT NULL,
    title varchar(255) NOT NULL,
    description varchar(255),
    sizeh int(11),
    sizel int(11),
    creationdate varchar(10) NOT NULL,
    updatedate varchar(10),
    vuedate varchar(10),
    author varchar(50),
    download int(11),
    albumlabel int(11),
    `status` char(1),
    albumid varchar(50) NOT NULL,
    creatorid varchar(50) NOT NULL,
    updateid varchar(50),
    instanceid varchar(50) NOT NULL,
    imagename varchar(255),
    imagesize int(11),
    imagemimetype varchar(100),
    begindate varchar(10) DEFAULT '0000/00/00' NOT NULL,
    enddate varchar(10) DEFAULT '9999/99/99' NOT NULL,
    keyword varchar(1000),
    begindownloaddate varchar(10),
    enddownloaddate varchar(10)
) ENGINE=InnoDB;

CREATE TABLE sc_gallery_path (
    photoid int(11) NOT NULL,
    nodeid int(11) NOT NULL,
    instanceid varchar(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_gallery_order (
    orderid int(11) NOT NULL,
    userid int(11) NOT NULL,
    instanceid varchar(50) NOT NULL,
    creationdate char(13) NOT NULL,
    processdate char(13),
    processuser int(11)
) ENGINE=InnoDB;

CREATE TABLE sc_gallery_orderdetail (
    orderid int(11) NOT NULL,
    photoid int(11) NOT NULL,
    instanceid varchar(50) NOT NULL,
    downloaddate char(13),
    downloaddecision varchar(50)
) ENGINE=InnoDB;

