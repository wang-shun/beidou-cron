
USE `beidou`;
set names 'utf8';


alter table usertrade add first_tradeid int(11) not null default 0 COMMENT '一级行业ID';
alter table usertrade add ka_trade1 varchar(64)  COLLATE utf8_bin not null default '' COMMENT 'ka_trade1';
alter table usertrade add ka_trade2 varchar(64)  COLLATE utf8_bin not null default '' COMMENT 'ka_trade2';
