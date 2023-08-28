create table task_time_entry(
    id SERIAL PRIMARY KEY,
    hours_spent decimal(20, 2),
    task_id int,
    user_id int,
    description varchar(120)
);

alter table task_time_entry add foreign key(task_id) references tasks(id) on delete cascade;
alter table task_time_entry add foreign key(user_id) references users(id) on delete cascade;

alter table tasks add column estimated_work_time decimal(20, 2);

insert into permissions values ('TIME_ENTRY_ADD');
insert into permissions values ('TIME_ENTRY_REMOVE');
insert into permissions values ('TIME_ENTRY_READ_ALL');