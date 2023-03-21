create table projects(
                         id int primary key AUTO_INCREMENT,
                         name varchar(30),
                         status varchar(10),
                         start_time date,
                         end_time date,
                         description varchar(120),
                         owner_id int
);

create table projects_users(
                        project_id int,
                        user_id int,
                        user_role varchar(10),
                        primary key(project_id, user_id)
);

create table task_groups(
                        id int primary key AUTO_INCREMENT,
                        name varchar(30),
                        project_id int
);

create table tasks(
                        id int primary key AUTO_INCREMENT,
                        name varchar(30),
                        status varchar(10),
                        priority varchar(10),
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

alter table projects_users add foreign key(project_id) references projects(id);
alter table projects_users add foreign key(user_id) references users(id);

alter table projects add foreign key(owner_id) references users(id);

alter table task_groups add foreign key(project_id) references projects(id);

alter table tasks add foreign key(task_group_id) references task_groups(id);
