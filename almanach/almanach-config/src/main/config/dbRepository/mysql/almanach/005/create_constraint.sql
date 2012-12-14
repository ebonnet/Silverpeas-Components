alter table sc_almanach_event add 
	 constraint pk_almanach_event primary key
	(
		eventid
	)   
;

alter table sc_almanach_periodicity add 
	 constraint pk_almanach_periodicity primary key
	(
		id
	)   
;

alter table sc_almanach_periodicityexcept add 
	 constraint pk_almanach_periodicityexcept primary key
	(
		id
	)   
;

