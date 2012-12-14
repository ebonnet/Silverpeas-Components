alter table sc_classifieds_classifieds
add constraint pk_sc_classifieds_classifieds primary key
	(
		classifiedid
	)   
;

alter table sc_classifieds_subscribes
add constraint pk_sc_classifieds_subscribes primary key
	(
		userid,
		instanceid,
		field1,
		field2
	)   
;
