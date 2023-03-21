create table users(
  id int primary key AUTO_INCREMENT,
  first_name varchar(30),
  last_name varchar(30),
  email varchar(50),
  password varchar(100),
  role varchar(10),
  locked boolean,
  enabled boolean
);

create table confirmation_tokens(
    id int primary key AUTO_INCREMENT,
    token varchar(100),
    created_at datetime,
    expired_at datetime,
    confirmed_at datetime,
    user_id int
);

alter table confirmation_tokens add foreign key(user_id) references users(id)
