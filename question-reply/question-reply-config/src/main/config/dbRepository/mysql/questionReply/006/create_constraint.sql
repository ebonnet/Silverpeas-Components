alter table sc_questionreply_question add 
constraint pk_questionreply_question primary key 
(
	id
)   
;

alter table sc_questionreply_reply add 
constraint pk_questionreply_reply primary key 
(
	id
)   
;

alter table sc_questionreply_recipient add 
constraint pk_questionreply_recipient primary key 
(
	id
)   
;