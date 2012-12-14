create index ind_sc_formsonline_userrights_1
   on sc_formsonline_userrights (formid, instanceid, righttype);
   
create index ind_sc_formsonline_userrights_2
   on sc_formsonline_userrights (righttype, userid);
   
create index ind_sc_formsonline_grouprights_1
   on sc_formsonline_grouprights (formid, instanceid, righttype);
   
create index ind_sc_formsonline_grouprights_2
   on sc_formsonline_grouprights (righttype, groupid);
   
   
