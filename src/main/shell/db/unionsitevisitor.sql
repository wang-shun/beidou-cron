/*
�ÿ��������ݿ����
*********************************************************************
*/

USE `beidouurl`;

CREATE TABLE `unionsitevisitor` (
  `siteid` int(10) NOT NULL COMMENT 'վ��ID��ͬBeidou��UnionSite���е�siteid',
  `tid` int(11) NOT NULL COMMENT '˾��ϵͳ��ͳ�ƴ����ݵ�����id',
  `siteurl` varchar(256) NOT NULL COMMENT 'վ��url',
  `site` varchar(6000) collate utf8_bin NOT NULL COMMENT '���ʸ���վ���û���ϲ�����ʵ�վ�㣬���ݸ�ʽΪվ����վ��,url,�������Ƕ�,ȫ���������Ƕ�,���ֶȣ������ֶȽ������д洢ǰ10����ʾ�����»���,www.xfwed.com,0.0076,0.0000,2.7712|�Ѻ���,www.shbk.net,0.0057,0.0000,2.6438...',
  `keyword` varchar(6000) collate utf8_bin NOT NULL COMMENT '���ʸ���վ���û���ϲ�������Ĺؼ��ʣ����ݸ�ʽΪ�ؼ����������Ƕ�,ȫ���������Ƕ�,���ֶȣ������ֶȽ������д洢ǰ10����ʾ������������,0.0316,0.0277,0.0576|��������,0.0401,0.0354,0.053...',
  `interest` varchar(6000) collate utf8_bin NOT NULL COMMENT '���ʸ���վ���û�����Ȥ�㣬���ݸ�ʽΪ��Ȥ��,�������Ƕ�,ȫ���������Ƕ�,���ֶȣ������ֶȽ������д洢ǰ10����ʾ��������/װ��,0.0316,0.0277,0.0576|ũ��/����,0.0401,0.0354,0.053...',
  `updatetime` datetime NOT NULL COMMENT '��¼����ʱ��',
  PRIMARY KEY  (`siteid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='�Էÿ��������м����Ľ��';
