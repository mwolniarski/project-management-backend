create table notifications(
                        id SERIAL primary key,
                        status varchar(20),
                        notification_content varchar(300),
                        related_to_id int
);

create table comments(
                         id SERIAL primary key,
                         created_by_id int,
                         content varchar(300),
                         task_id int,
                         created_time timestamp
);

alter table notifications add foreign key(related_to_id) references users(id) ON DELETE cascade;

alter table comments add foreign key(created_by_id) references users(id);
alter table comments add foreign key(task_id) references tasks(id) ON DELETE cascade;