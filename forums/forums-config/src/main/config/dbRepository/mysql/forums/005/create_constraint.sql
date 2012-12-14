alter table sc_forums_forum add 
	 constraint pk_forums_forum primary key 
	(
		forumid
	)   
;

alter table sc_forums_message add 
	 constraint pk_forums_message primary key
	(
		messageid
	)   
;



alter table sc_forums_rights add 
	 constraint pk_forums_rights primary key
	(
		userid,
		forumid
	)   
;

alter table sc_forums_subscription add 
	 constraint pk_forums_subscription primary key 
	(
		userid,
		messageid
	)   
;

alter table sc_forums_historyuser add 
	 constraint pk_forums_historyuser primary key 
	(
		userid,
		messageid
	)   
;  