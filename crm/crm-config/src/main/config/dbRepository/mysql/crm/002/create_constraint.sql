alter table sc_crm_contacts
add constraint pk_crm_contacts
primary key (id);

alter table sc_crm_delivery
add constraint pk_crm_delivery
primary key (id);

alter table sc_crm_events
add constraint pk_crm_events
primary key (id);

alter table sc_crm_infos
add constraint pk_crm_infos
primary key (id);

alter table sc_crm_participants
add constraint pk_crm_participants
primary key (id);

alter table sc_crm_contacts
add constraint fk_sc_crm_contacts_1
foreign key (crmid) references sc_crm_infos (id);

alter table sc_crm_delivery
add constraint fk_sc_crm_delivery_1
foreign key (crmid) references sc_crm_infos (id);

alter table sc_crm_delivery
add constraint fk_sc_crm_delivery_2
foreign key (contactid) references sc_crm_contacts (id);

alter table sc_crm_events
add constraint fk_sc_crm_events_1
foreign key (crmid) references sc_crm_infos (id);

alter table sc_crm_participants
add constraint fk_sc_crm_participants_1
foreign key (crmid) references sc_crm_infos (id);