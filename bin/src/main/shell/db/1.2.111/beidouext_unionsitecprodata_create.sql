USE beidouext;
SET names utf8;

DROP TABLE if EXISTS unionsitecprodata;

#beidouext.unionsitecprodata
CREATE TABLE `unionsitecprodata` (
  `id` int(10) NOT NULL auto_increment,
  `siteid` int(10) NOT NULL COMMENT '站点ID，同beidouext.unionsite表中的siteid',
  `insert_date` date NOT NULL COMMENT '数据生成日期',
  `cpm` decimal(12,4) NOT NULL DEFAULT 0 COMMENT '千次展现成本',
  `ctr` decimal(12,4) NOT NULL DEFAULT 0 COMMENT '点击率，存的是百分比，0.2314%，小数点后四位，且不存%',
  `uv` int(11) NOT NULL DEFAULT 0 COMMENT '点击独立访客数',
  `click` int(11) NOT NULL DEFAULT 0 COMMENT '点击次数',
  `hour_click` varchar(256) collate utf8_bin NOT NULL DEFAULT "-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-" COMMENT '分小时点击次数，每个小时之间以|分割，若某小时没有数值用-表示',
  PRIMARY KEY (`id`),
  KEY `idx_unionsitecprodata_siteid_date` (`siteid`,`insert_date`),
  KEY `idx_unionsitecprodata_date` (`insert_date`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='流量V获取的站点推广数据';
