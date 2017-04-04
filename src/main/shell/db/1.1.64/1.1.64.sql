use beidoureport;

drop table if exists ctblacklist;

CREATE TABLE `ctblacklist` (
	`wordid` int(10) unsigned NOT NULL COMMENT 'atomid',
	`tag` int(10) NOT NULL COMMENT '标识，标识为0表示黑名单词，为1的为精确匹配词',
	 PRIMARY KEY  (`wordid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='主题词黑名单词表';

