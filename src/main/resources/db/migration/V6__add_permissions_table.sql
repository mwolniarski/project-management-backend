create table permissions(
                              name varchar(30) primary key
);
insert into permissions values ('ALLOW_ALL');

insert into permissions values ('ORGANIZATION_DELETE');
insert into permissions values ('ORGANIZATION_UPDATE');
insert into permissions values ('ORGANIZATION_ADD_USER');
insert into permissions values ('ORGANIZATION_DELETE_USER');
insert into permissions values ('ORGANIZATION_READ_USERS');

insert into permissions values ('ROLE_CREATE');
insert into permissions values ('ROLE_DELETE');

insert into permissions values ('TASK_UPDATE');
insert into permissions values ('TASK_DELETE');
insert into permissions values ('TASK_CREATE');

insert into permissions values ('TASK_GROUP_CREATE');
insert into permissions values ('TASK_GROUP_DELETE');
insert into permissions values ('TASK_GROUP_UPDATE');

insert into permissions values ('PROJECT_CREATE');
insert into permissions values ('PROJECT_DELETE');
insert into permissions values ('PROJECT_UPDATE');
insert into permissions values ('PROJECT_READ');

insert into permissions values ('PROJECT_ADD_USER');
insert into permissions values ('PROJECT_REMOVE_USER');

insert into permissions values ('TASK_ADD_COMMENT');
insert into permissions values ('TASK_EDIT_COMMENT');
insert into permissions values ('TASK_DELETE_COMMENT');