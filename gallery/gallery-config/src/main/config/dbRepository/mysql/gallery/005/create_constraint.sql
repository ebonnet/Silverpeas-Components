alter table sc_gallery_photo
add constraint pk_sc_gallery_photo primary key
	(
		photoid
	)   
;

alter table sc_gallery_path
add constraint pk_sc_gallery_path primary key
	(
		photoid, nodeid
	)   
;

alter table sc_gallery_order
add constraint pk_sc_gallery_order primary key
	(
		orderid
	)   
;

alter table sc_gallery_orderdetail
add constraint pk_sc_gallery_orderdetail primary key
	(
		orderid, photoid
	)   
;
