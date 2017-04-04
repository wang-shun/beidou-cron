
set names utf8;
use beidoureport;

CREATE TABLE `realtime_stat` (
  `statid` bigint(20) NOT NULL auto_increment COMMENT '主键',
  `time` bigint(20) NOT NULL COMMENT '刻钟戳',
  `userid` int(10) NOT NULL COMMENT '用户id',
  `planid` int(10) NOT NULL COMMENT '推广计划id',
  `groupid` int(10) NOT NULL COMMENT '推广组id',
  `adid` bigint(20) NOT NULL COMMENT '创意id',
  `gpid` bigint(20) default NULL COMMENT '包关联关系id',
  `refpackid` int(10) default NULL COMMENT '包引用id',
  `mainsite` varchar(1024) collate utf8_bin default NULL COMMENT '主域',
  `site` varchar(1024) collate utf8_bin default NULL COMMENT '站点',
  `firsttradeid` int(10) default NULL COMMENT '一级行业id',
  `secondtradeid` int(10) default NULL COMMENT '二级行业id',
  `provid` int(10) default NULL COMMENT '一级地域id',
  `cityid` int(10) default NULL COMMENT '二级地域id',
  `keywordid` bigint(20) default NULL COMMENT '关键词id',
  `wordid` bigint(20) default NULL COMMENT '字面id',
  `iid` int(10) default NULL COMMENT '兴趣点/兴趣组合id',
  `itid` bigint(20) default NULL COMMENT '兴趣关联关系id',
  `genderid` int(10) default NULL COMMENT '性别 7：男，8：女，9：未传入，0：未识别',
  `clks` bigint(20) NOT NULL COMMENT '点击',
  `cost` bigint(20) NOT NULL COMMENT '消费',
  PRIMARY KEY  (`statid`),
  KEY `uid_pid_idx` (`userid`,`planid`),
  KEY `uid_gid_idx` (`userid`,`groupid`),
  KEY `uid_aid_idx` (`userid`,`adid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
