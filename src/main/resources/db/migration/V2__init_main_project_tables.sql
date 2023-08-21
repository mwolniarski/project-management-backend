create table projects(
                         id SERIAL primary key,
                         name varchar(30),
                         status varchar(15),
                         start_time date,
                         end_time date,
                         description varchar(120),
                         owner_id int
);

create table projects_users(
                        project_id int,
                        user_id int,
                        user_role varchar(12),
                        primary key(project_id, user_id)
);

create table task_groups(
                        id SERIAL primary key,
                        name varchar(30),
                        project_id int
);

create table tasks(
                        id SERIAL primary key,
                        name varchar(30),
                        status varchar(15),
                        priority varchar(15),
                        description varchar(120),
                        due_date date,
                        task_group_id int,
                        task_owner_id int
);

create table users_tasks(
                      task_id int,
                      user_id int,
                      primary key(task_id, user_id)
);

alter table users_tasks add foreign key(task_id) references tasks(id);
alter table users_tasks add foreign key(user_id) references users(id);

alter table projects_users add foreign key(project_id) references projects(id) on delete cascade;
alter table projects_users add foreign key(user_id) references users(id);

alter table projects add foreign key(owner_id) references users(id);

alter table task_groups add foreign key(project_id) references projects(id) on delete cascade;

alter table tasks add foreign key(task_group_id) references task_groups(id) on delete cascade;
