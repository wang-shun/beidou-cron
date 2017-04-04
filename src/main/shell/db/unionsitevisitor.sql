/*
访客特征数据库表建立
*********************************************************************
*/

USE `beidouurl`;

CREATE TABLE `unionsitevisitor` (
  `siteid` int(10) NOT NULL COMMENT '站点ID，同Beidou库UnionSite表中的siteid',
  `tid` int(11) NOT NULL COMMENT '司南系统里统计此数据的任务id',
  `siteurl` varchar(256) NOT NULL COMMENT '站点url',
  `site` varchar(6000) collate utf8_bin NOT NULL COMMENT '访问该网站的用户还喜欢访问的站点，数据格式为站点网站名,url,样本覆盖度,全网样本覆盖度,区分度，按区分度降序排列存储前10个，示例：新华网,www.xfwed.com,0.0076,0.0000,2.7712|搜狐网,www.shbk.net,0.0057,0.0000,2.6438...',
  `keyword` varchar(6000) collate utf8_bin NOT NULL COMMENT '访问该网站的用户还喜欢搜索的关键词，数据格式为关键词样本覆盖度,全网样本覆盖度,区分度，按区分度降序排列存储前10个，示例：国家新闻,0.0316,0.0277,0.0576|最新新闻,0.0401,0.0354,0.053...',
  `interest` varchar(6000) collate utf8_bin NOT NULL COMMENT '访问该网站的用户的兴趣点，数据格式为兴趣点,样本覆盖度,全网样本覆盖度,区分度，按区分度降序排列存储前10个，示例：建筑/装修,0.0316,0.0277,0.0576|农林/牧工,0.0401,0.0354,0.053...',
  `updatetime` datetime NOT NULL COMMENT '记录更新时间',
  PRIMARY KEY  (`siteid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='对访客特征进行计算后的结果';
