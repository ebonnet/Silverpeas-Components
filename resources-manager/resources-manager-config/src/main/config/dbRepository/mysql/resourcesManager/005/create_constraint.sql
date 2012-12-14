alter table sc_resources_category 
add constraint pk_resources_category primary key
	(
	id
	)   
;

alter table sc_resources_resource
add constraint pk_resources_resource primary key
	(
	id
	)   
;

alter table sc_resources_reservation
add constraint pk_resources_reservation primary key
	(
	id
	)   
;

alter table sc_resources_reservedresource
add constraint pk_resources_reservedresource primary key
	(
	reservationid,
	resourceid	
	)   
;

alter table sc_resources_managers
add constraint pk_resources_managers primary key
	(
	resourceid,
	managerid
	)     
;
