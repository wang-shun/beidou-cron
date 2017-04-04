
use aot;

drop table if exists qtkrspecialtrade;
drop table if exists qtwordnum;
drop table if exists tradebaseacp;

CREATE TABLE `qtkrspecialtrade` (
  `secondtradeid` int(10) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE `qtwordnum` (
  `tradeid` int(10) NOT NULL,
  `userid` int(10) NOT NULL,
  `groupid` int(10) NOT NULL,
  `qtwordnum` int(10) NOT NULL default '0',
  KEY `qtwordnum_tradeid` (`tradeid`),
  KEY `qtwordnum_groupid` (`groupid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='同行业用户QT推广组有效提词量';

CREATE TABLE `tradebaseacp` (
  `seqid` int(10) NOT NULL auto_increment COMMENT '自增主键',
  `secondtradeid` int(10) NOT NULL default '0' COMMENT '客户二级行业id',
  `groupclassification` int(10) NOT NULL default '0' COMMENT '1固定文字或图文混排，2固定图片或Flash，3悬浮，4贴片',
  `firstregionid` int(10) NOT NULL default '0' COMMENT '一级地域id',
  `targettype` int(10) NOT NULL default '0' COMMENT '定向方式',
  `bid20` float NOT NULL default '0' COMMENT '20分位的出价,单位为分',
  `bid25` float NOT NULL default '0' COMMENT '25分位的出价,单位为分',
  `bid30` float NOT NULL default '0' COMMENT '30分位的出价,单位为分',
  `bid40` float NOT NULL default '0' COMMENT '40分位的出价,单位为分',
  `bid50` float NOT NULL default '0' COMMENT '50分位的出价,单位为分',
  `bid60` float NOT NULL default '0' COMMENT '60分位的出价,单位为分',
  `bid70` float NOT NULL default '0' COMMENT '70分位的出价,单位为分',
  `bid75` float NOT NULL default '0' COMMENT '75分位的出价,单位为分',
  `bid80` float NOT NULL default '0' COMMENT '80分位的出价,单位为分',
  `bid90` float NOT NULL default '0' COMMENT '90分位的出价,单位为分',
  `avgbid` float NOT NULL default '0' COMMENT '平均出价,单位为分',
  `bidcount` int(10) NOT NULL default '0' COMMENT '出价个数',
  PRIMARY KEY  (`seqid`),
  KEY `tradeIndex` (`secondtradeid`),
  KEY `groupIndex` (`groupclassification`),
  KEY `regionIndex` (`firstregionid`),
  KEY `targettypeIndex` (`targettype`)
) ENGINE=InnoDB AUTO_INCREMENT=1780871 DEFAULT CHARSET=utf8 COMMENT='分行业分推广组类型分一级地域出价表';


