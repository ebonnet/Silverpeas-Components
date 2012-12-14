alter table sc_projectmanager_tasks add 
	constraint pk_projectmanager_tasks primary key  
	(
		id
	)
;
alter table sc_projectmanager_calendar add 
	constraint pk_projectmanager_calendar primary key  
	(
		holidaydate, fatherid, instanceid
	)
;
alter table sc_projectmanager_resources add
	constraint pk_projectmanager_resource primary key
	(
		id
	)
; 