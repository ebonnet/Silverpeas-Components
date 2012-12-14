alter table sc_mailinglist_message
        drop foreign key mailinglist_message_mailid_key;
alter table sc_mailinglist_external_user
        drop foreign key FK9290F7C94B1A1B47;
alter table sc_mailinglist_attachment
        drop foreign key FKCE814959DB1C14EE;
alter table sc_mailinglist_internal_sub
        drop foreign key fk_subscriber_mailinglist_id;

alter table sc_mailinglist_message
        drop primary key;
alter table sc_mailinglist_list
        drop primary key;
alter table sc_mailinglist_external_user
        drop primary key ;
alter table sc_mailinglist_attachment
        drop primary key;
alter table sc_mailinglist_internal_sub
        drop primary key;