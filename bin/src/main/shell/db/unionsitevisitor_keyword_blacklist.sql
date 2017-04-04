/*
访客特征数据关键词黑名单表
*********************************************************************
*/

USE `beidouurl`;

CREATE TABLE `unionsitevisitor_keyword_blacklist` (
  `keyword` varchar(1000) NOT NULL COMMENT '封禁的关键词'
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='访客特征关键词部分的黑名单';
