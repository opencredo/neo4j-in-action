create table t_user (
  id bigint not null,
  name varchar(255) not null,
  primary key (id)
);
create table t_user_friend (
  id bigint not null,
  user_1 bigint  not null,
  user_2 bigint  not null,
  primary key (id)
);

alter table t_user_friend add index FK416055ABC6132571 (user_1), add constraint FK416055ABC6132571 foreign key (user_1) references t_user (id);
alter table t_user_friend add index FK416055ABC6132572 (user_2), add constraint FK416055ABC6132572 foreign key (user_2) references t_user (id);

