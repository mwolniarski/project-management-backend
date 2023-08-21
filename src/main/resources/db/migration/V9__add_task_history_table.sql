create table task_histories(
                                  id SERIAL PRIMARY KEY,
                                  task_id int,
                                  created_at timestamp,
                                  description varchar(500)
);

alter table task_histories add foreign key(task_id) references tasks(id) ON DELETE CASCADE;

insert into permissions values ('TASK_HISTORY_READ');