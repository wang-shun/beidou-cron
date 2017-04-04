/*
访客特征数据相关站点黑名单表
*********************************************************************
*/

USE `beidouurl`;

CREATE TABLE `unionsitevisitor_siteurl_blacklist` (
  `siteurl` varchar(256) NOT NULL COMMENT '封禁的站点url'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='访客特征相关站点部分的黑名单';
