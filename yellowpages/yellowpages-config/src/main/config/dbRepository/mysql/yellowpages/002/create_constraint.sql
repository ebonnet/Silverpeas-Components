alter table sc_contact_groupfather
add constraint pk_sc_contact_groupfather primary key
	(
		groupid, fatherid, instanceid
	)   
;
