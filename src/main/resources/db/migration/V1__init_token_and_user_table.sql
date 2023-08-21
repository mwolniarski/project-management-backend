create table users(
  id SERIAL primary key,
  first_name varchar(30),
  last_name varchar(30),
  email varchar(50),
  password varchar(100),
  role varchar(20),
  locked boolean,
  enabled boolean
);

create table confirmation_tokens(
    id SERIAL primary key,
    token varchar(100),
    created_at timestamp,
    expired_at timestamp,
    confirmed_at timestamp,
    user_id int
);

alter table confirmation_tokens add foreign key(user_id) references users(id) ON DELETE cascade;
