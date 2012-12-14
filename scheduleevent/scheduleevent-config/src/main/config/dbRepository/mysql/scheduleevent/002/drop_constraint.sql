alter table sc_scheduleevent_response
        drop foreign key fk_response_optionid;
alter table sc_scheduleevent_response
        drop foreign key fk_response_scheduleeventid;
alter table sc_scheduleevent_contributor
        drop foreign key fk_contributor_scheduleeventid;
alter table sc_scheduleevent_options
        drop foreign key fk_options_eventid;
        
alter table sc_scheduleevent_response
        drop primary key;
alter table sc_scheduleevent_contributor
        drop primary key;
alter table sc_scheduleevent_options
        drop primary key;
alter table sc_scheduleevent_list
        drop primary key;