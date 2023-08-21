create table organizations(
                         org_id SERIAL primary key,
                         name varchar(50),
                         org_status varchar(10)
);

alter table users add column organization_org_id int;

alter table users add foreign key(organization_org_id) references organizations(org_id) on delete cascade;

alter table projects add column organization_org_id int;

alter table projects add foreign key(organization_org_id) references organizations(org_id) on delete cascade;