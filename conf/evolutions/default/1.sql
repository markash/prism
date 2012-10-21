# --- !Ups

create table appraisal (
  id                        bigint not null,
  period_id                 bigint,
  completed                 timestamp,
  constraint pk_appraisal primary key (id))
;

create table appraisal_period (
  id                        bigint not null,
  starts                    timestamp,
  ends                      timestamp,
  closed                    timestamp,
  constraint pk_appraisal_period primary key (id))
;

create table appraisal_item (
  id                        bigint not null,
  appraisal_id              bigint not null,
  description               varchar(256),
  score                     decimal(12, 2),
  constraint pk_appraisal_item primary key (id)
);

create sequence appraisal_seq;
create sequence appraisal_period_seq;
create sequence appraisal_item_seq;

alter table appraisal add constraint fk_appraisal_period_1 foreign key (period_id) references appraisal_period (id) on delete restrict on update restrict;
alter table appraisal_item add constraint fk_appraisal_1 foreign key (appraisal_id) references appraisal(id) on delete restrict on update restrict;

create index ix_appraisal_period_1 on appraisal (period_id);
create index ix_appraisal_item_1 on appraisal_item (appraisal_id);


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists appraisal_item;

drop table if exists appraisal;

drop table if exists appraisal_period;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists appraisal_item_seq;

drop sequence if exists appraisal_seq;

drop sequence if exists appraisal_period_seq;


