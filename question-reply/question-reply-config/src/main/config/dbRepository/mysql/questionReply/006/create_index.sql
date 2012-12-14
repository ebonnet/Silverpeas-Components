create index questionreply_instanceid
on sc_questionreply_question (instanceid);

create index questionreply_userid
on sc_questionreply_recipient (userid);