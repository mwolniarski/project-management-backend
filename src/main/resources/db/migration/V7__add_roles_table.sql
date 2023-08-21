create table roles(
    id SERIAL primary key,
    name varchar(30),
    organization_org_id int
);

create table permissions_roles(
    role_id int,
    permission_name varchar(30)
);

alter table roles add foreign key(organization_org_id) references organizations(org_id);

alter table permissions_roles add foreign key(role_id) references roles(id);
alter table permissions_roles add foreign key(permission_name) references permissions(name);


alter table users drop column role;
alter table users add column main_role_id int;
alter table users add foreign key(main_role_id) references roles(id);