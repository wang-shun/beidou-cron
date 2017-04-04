package com.baidu.beidou.aot.service;

public interface AotQtMgr {
	
	/**
	 * 将QT推广组的主动推荐词存入数据库。
	 * 需要经过相关性阈值过滤，黑名单词过滤，已购词过滤等
	 * 
	 * @param inputGroupkrWordFile 输入的推荐词，格式形如groupid \t [wordid:value, wordid:value, …] \n
	 * @param outputGroupkrWordFile 输出的关键词，格式形如groupid \t wordid \n
	 * @param relativity	推荐词的相关性
	 * @param minQtkrCnt    最少的推荐词数量
	 * 
	 * @author	hanxu03
	 * @Date	2011-12-7
	 */
	public void importQtkrWord(String inputGroupkrWordFile, String outputGroupkrWordFile, float relativity, int minQtkrCnt);
	
	/**
	 * 将QT推广组的有效提词数存入数据库。
	 * 要过滤大客户的推广组，还要经过黑名单词过滤，已购词过滤等
	 * 
	 * @param userTradeFile 输入用户的行业文件，格式形如  userId \t tradeId
	 * @param groupQtWordNumFile 输出的QT推广组的有效词数量，格式形如 tradeId  \t  userId  \t  groupId  \t  qtWordCount
	 * 
	 * @author	hanxu03
	 * @Date	2011-12-7
	 */
	public void importQtWordNum(String userTradeFile, String groupQtWordNumFile, int batchGroupPerUser);
}
