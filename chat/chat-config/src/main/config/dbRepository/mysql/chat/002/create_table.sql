CREATE TABLE sc_chat_banned (
    id int(11) NOT NULL,
    chatroomid int(11) NOT NULL,
    userid varchar(150) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE sc_chat_chatroom (
    id int(11) NOT NULL,
    instanceid varchar(50) NOT NULL,
    chatroomid int(11) NOT NULL
) ENGINE=InnoDB;
