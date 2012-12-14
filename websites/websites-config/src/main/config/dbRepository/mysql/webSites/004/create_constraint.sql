alter table sc_websites_icons add 
	 constraint pk_websites_icons primary key
	(
		iconsid
	)
	;   

alter table sc_websites_site add 
	 constraint pk_websites_site primary key
	(
		siteid
	)
	;   

alter table sc_websites_siteicons add 
	 constraint pk_websites_siteicons primary key
	(
		siteid,
		iconsid
	)
	;   
