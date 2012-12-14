create table sc_crm_contacts (
  id int not null,
  crmid int not null,
  instanceid varchar (50) not null,
  name varchar (255) not null,
  functioncontact varchar (255) not null,
  tel varchar (50) null,
  email varchar (50) null,
  address varchar (255) null,
  active varchar (1) not null 
);

create table sc_crm_delivery (
  id int not null,
  crmid int not null,
  instanceid varchar (50) not null,
  deliverydate char (10) not null,
  element varchar (255) not null,
  version varchar (50) not null,
  deliveryid varchar (50) not null,
  deliveryname varchar (50) not null,
  contactid int not null,
  contactname varchar (50) not null,
  media varchar (255) not null
);

create table sc_crm_events (
  id int not null,
  crmid int not null,
  instanceid varchar (50) not null,
  eventdate varchar (10) not null,
  eventlib varchar (255) not null,
  actiontodo varchar (255) not null,
  userid varchar (50) not null,
  username varchar (50) not null,
  actiondate varchar (10) not null,
  state varchar (50) not null
);

create table sc_crm_infos (
  id int not null,
  clientname varchar (255) not null,
  projectcode varchar (255) null,
  instanceid varchar (50) not null
);

create table sc_crm_participants (
  id int not null,
  crmid int not null,
  instanceid varchar (50) not null,
  username varchar (255) not null,
  functionparticipant varchar (255) not null,
  email varchar (255) null,
  active varchar (1) not null,
  userid varchar (50) not null
);