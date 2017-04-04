
set names utf8;
use beidoureport;

CREATE TABLE `realtime_stat` (
  `statid` bigint(20) NOT NULL auto_increment COMMENT '����',
  `time` bigint(20) NOT NULL COMMENT '���Ӵ�',
  `userid` int(10) NOT NULL COMMENT '�û�id',
  `planid` int(10) NOT NULL COMMENT '�ƹ�ƻ�id',
  `groupid` int(10) NOT NULL COMMENT '�ƹ���id',
  `adid` bigint(20) NOT NULL COMMENT '����id',
  `gpid` bigint(20) default NULL COMMENT '��������ϵid',
  `refpackid` int(10) default NULL COMMENT '������id',
  `mainsite` varchar(1024) collate utf8_bin default NULL COMMENT '����',
  `site` varchar(1024) collate utf8_bin default NULL COMMENT 'վ��',
  `firsttradeid` int(10) default NULL COMMENT 'һ����ҵid',
  `secondtradeid` int(10) default NULL COMMENT '������ҵid',
  `provid` int(10) default NULL COMMENT 'һ������id',
  `cityid` int(10) default NULL COMMENT '��������id',
  `keywordid` bigint(20) default NULL COMMENT '�ؼ���id',
  `wordid` bigint(20) default NULL COMMENT '����id',
  `iid` int(10) default NULL COMMENT '��Ȥ��/��Ȥ���id',
  `itid` bigint(20) default NULL COMMENT '��Ȥ������ϵid',
  `genderid` int(10) default NULL COMMENT '�Ա� 7���У�8��Ů��9��δ���룬0��δʶ��',
  `clks` bigint(20) NOT NULL COMMENT '���',
  `cost` bigint(20) NOT NULL COMMENT '����',
  PRIMARY KEY  (`statid`),
  KEY `uid_pid_idx` (`userid`,`planid`),
  KEY `uid_gid_idx` (`userid`,`groupid`),
  KEY `uid_aid_idx` (`userid`,`adid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
