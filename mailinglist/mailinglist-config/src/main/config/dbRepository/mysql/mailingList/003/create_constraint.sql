alter table sc_mailinglist_message add constraint pk_mailinglist_message
        primary key (id);
alter table sc_mailinglist_list add constraint pk_mailinglist_list
        primary key (id);
alter table sc_mailinglist_external_user add constraint pk_mailinglist_external_user
        primary key (id);
alter table sc_mailinglist_attachment add constraint pk_mailinglist_attachment
        primary key (id);
alter table sc_mailinglist_internal_sub add constraint pk_mailinglist_internal_sub
        primary key (id);

alter table sc_mailinglist_external_user add constraint fk9290f7c94b1a1b47
        foreign key (listid)
        references sc_mailinglist_list (id);
alter table sc_mailinglist_attachment add constraint fkce814959db1c14ee
        foreign key (messageid)
        references sc_mailinglist_message (id);
alter table sc_mailinglist_internal_sub add constraint fk_subscriber_mailinglist_id
         foreign key (mailinglistid)
        references sc_mailinglist_list (id);
alter table sc_mailinglist_message add constraint mailinglist_message_mailid_key
        unique (mailid, componentid);
