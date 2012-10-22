# --- !Ups

create table profile (
  email                     varchar(256) not null,
  name                      varchar(64),
  surname                   varchar(64),
  credentials               varchar(64) not null,
  constraint pk_profile primary key (email)
);

create index ix_profile_1 on profile (name, surname);

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists profile;
