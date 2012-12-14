alter table sc_formsonline_forms add 
	 constraint pk_sc_formsonline_forms primary key
	(
		id
	)
;

alter table sc_formsonline_forminstances add 
	 constraint pk_sc_formsonline_forminstances primary key
	(
		id
	)
;

alter table sc_formsonline_forminstances add constraint fk_forminstance foreign key (formid) references sc_formsonline_forms(id);
alter table sc_formsonline_userrights add constraint fk_userrights foreign key (formid) references sc_formsonline_forms(id);
alter table sc_formsonline_grouprights add constraint fk_grouprights foreign key (formid) references sc_formsonline_forms(id);
