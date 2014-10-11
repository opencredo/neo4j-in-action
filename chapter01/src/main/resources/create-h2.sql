create table T_USER (
  id int not null,
  name varchar not null
)

create table T_USER_FRIEND (
  id int not null,
  user_1 int not null,
  user_2 int not null
)