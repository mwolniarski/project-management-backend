create table reset_password_token(
                      id SERIAL primary key,
                      token varchar(100),
                      created_at timestamp,
                      expired_at timestamp,
                      used_at timestamp,
                      user_id int
);

alter table reset_password_token add foreign key(user_id) references users(id);