alter table sc_formsonline_forms drop primary key;
alter table sc_formsonline_forminstances drop primary key;
alter table sc_formsonline_forminstances drop foreign key fk_forminstance;
alter table sc_formsonline_userrights drop foreign key fk_userrights;
alter table sc_formsonline_grouprights drop foreign key fk_grouprights;
