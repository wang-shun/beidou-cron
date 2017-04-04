﻿set names utf8; 
use beidouext;
drop table if exists composite_unionsite_tmp;
CREATE TABLE `composite_unionsite_tmp` (
  `siteid` int(10) unsigned NOT NULL COMMENT '网站id',
  `siteurl` varchar(256) collate utf8_bin NOT NULL COMMENT '网站URL',
  `firsttradeid` int(10) unsigned NOT NULL default '0' COMMENT '网站一级行业',
  `secondtradeid` int(10) unsigned NOT NULL default '0' COMMENT '网站二级行业',
  `isdomain` tinyint(3) NOT NULL COMMENT '网站是否为主域',
  `parentid` int(10) unsigned NOT NULL default '0' COMMENT '网站的父id',
  `valid` tinyint(3) unsigned NOT NULL default '1' COMMENT '7天是否有效，1：有效，0：无效',
  `invalidtime` datetime NOT NULL COMMENT '网站最后一次失效的时间',
  `jointime` datetime NOT NULL COMMENT '网站的加入时间',
  `currentvalid` tinyint(3) unsigned NOT NULL default '1' COMMENT '当前是否有效，1：有效，0：无效',
  `srchs` int(10) unsigned NOT NULL COMMENT '网站广告日检索量',
  `adviews` int(10) unsigned NOT NULL COMMENT '广告日展现量',
  `ips` int(10) unsigned NOT NULL COMMENT 'IP数',
  `cookies` int(10) unsigned NOT NULL COMMENT 'unique cookie数',
  `clks` int(10) unsigned NOT NULL COMMENT '日点击量',
  `cost` int(10) unsigned NOT NULL COMMENT '日消费量',
  `suporttype` smallint(5) unsigned NOT NULL COMMENT '支持物料类型，1：文字，2：文字/图片，3：仅图片',
  `displaytype` tinyint(3) NOT NULL default '0' COMMENT '支持展示类型，1：固定，2：悬浮，4：贴片',
  `size` varchar(200) collate utf8_bin default NULL COMMENT '支持的尺寸id列表，按照|分隔',
  `adblockthruput` varchar(500) collate utf8_bin default NULL COMMENT '支持的尺寸流量列表，按照|分隔',
  `fixed_srchs` int(10) unsigned NOT NULL default '0' COMMENT '固定类型检索量',
  `fixed_adviews` int(10) unsigned NOT NULL default '0' COMMENT '固定类型展现量',
  `fixed_clks` int(10) unsigned NOT NULL default '0' COMMENT '固定类型点击量',
  `fixed_cost` int(10) unsigned NOT NULL default '0' COMMENT '固定类型消费',
  `flow_srchs` int(10) unsigned NOT NULL default '0' COMMENT '悬浮类型检索量',
  `flow_adviews` int(10) unsigned NOT NULL default '0' COMMENT '悬浮类型展现量',
  `flow_clks` int(10) unsigned NOT NULL default '0' COMMENT '悬浮类型点击量',
  `flow_cost` int(10) unsigned NOT NULL default '0' COMMENT '悬浮类型消费',
  `film_srchs` int(10) unsigned NOT NULL default '0' COMMENT '贴片类型检索量',
  `film_adviews` int(10) unsigned NOT NULL default '0' COMMENT '贴片类型展现量',
  `film_clks` int(10) unsigned NOT NULL default '0' COMMENT '贴片类型点击量',
  `film_cost` int(10) unsigned NOT NULL default '0' COMMENT '贴片类型消费',
  `sitename` varchar(128) collate utf8_bin default NULL COMMENT '站点名称',
  `sitedesc` varchar(1024) collate utf8_bin default NULL COMMENT '站点描述',
  `filter` varchar(256) collate utf8_bin default NULL COMMENT '过滤广告分类，按照逗号分隔',
  `certification` tinyint(3) NOT NULL default '0' COMMENT '联盟认证',
  `finanobj` tinyint(3) NOT NULL COMMENT '财务结算类型，0：个人，1：企业',
  `credit` int(10) default NULL COMMENT '信誉等级',
  `direct` tinyint(3) default NULL COMMENT '0：直营，1：二级',
  `channel` int(10) NOT NULL COMMENT '渠道，7：二级联盟，6：其他，2：电信，5：百度测试，4：百度自由流量，3：网吧，0：网站，1：软件',
  `cheats` int(10) unsigned default NULL COMMENT '作弊次数',
  `sitelink` varchar(500) collate utf8_bin NOT NULL COMMENT '站点链接，以http://开头',
  `snapshot` varchar(40) collate utf8_bin default '' COMMENT '站点截图',
  `site_source` int(10) NOT NULL default '1' COMMENT '网站来源: 按bit表示，1：百度联盟，2：google',
  `scale` tinyint(3) NOT NULL default '2' COMMENT '网站等级',
  `ratecmp` decimal(9,8) unsigned NOT NULL default '0.00000000' COMMENT '投放竞争比',
  `scorecmp` float unsigned NOT NULL default '0' COMMENT '竞争度得分',
  `cmplevel` tinyint(3) NOT NULL default '1' COMMENT '竞争激烈程度',
  `q1` decimal(10,4) default NULL COMMENT '流量质量度Q1',
  `q2` decimal(10,4) default NULL COMMENT '流量质量度Q2',
  `thruputtype` tinyint(3) default NULL COMMENT '检索量等级，1：0-10万，2：10-100万，3： 100-1000万，4：1000万以上',
  `sizethruput` varchar(100) collate utf8_bin default NULL COMMENT '各个尺寸等级',
  `score` tinyint(4) NOT NULL default '0' COMMENT '网站评分，0：暂无评分，否则为6、7、8、9、10中的一个值',
  PRIMARY KEY  (`siteid`),
  KEY `COMPOSITE_UNIONSITE_SITEURL` (`siteurl`),
  KEY `COMPOSITE_UNIONSITE_VALID` (`valid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='网站信息聚合表' ;