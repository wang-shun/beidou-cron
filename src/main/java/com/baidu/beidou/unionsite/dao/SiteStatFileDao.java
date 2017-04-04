/**
 * 2009-4-23 下午04:07:31
 */
package com.baidu.beidou.unionsite.dao;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.baidu.beidou.unionsite.bo.IPCookieBo;
import com.baidu.beidou.unionsite.bo.SiteStatBo;
import com.baidu.beidou.unionsite.bo.SiteStatExtBo;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;

/**
 * @author zengyunfeng
 * @version 1.0.7
 * 对beidou-stat数据进行的文件读写的接口
 */
public interface SiteStatFileDao {
	
	/**
	 * 返回cpro-stat站点统计数据的个数和起始序号
	 * @author zengyunfeng
	 * @return 如果不存在返回{0,0}
	 */
	int[] getFileRange();
	
	/**
	 * 存储cpro-stat站点统计数据的个数和起始序号
	 * @author zengyunfeng
	 * @return 
	 */
	void storeFileRange(int start, int size);
	
	
	/**
	 * 读取一条记录
	 * @author zengyunfeng
	 * @param reader
	 * @param displayType 展现类型（二进制）：1-固定;2-悬浮;4-贴片
	 * @return 解析的统计数据，null表示读到文件末尾
	 * @throws IOException 
	 * @throws ErrorFormatException 
	 * 
	 */
	SiteStatBo readRecord(BufferedReader reader, int displayType) throws IOException, ErrorFormatException;
	
	/**
	 * 读取一条IP,cookie记录
	 * @author zengyunfeng
	 * @param line
	 * @return 解析的统计数据，null表示读到文件末尾
	 * @throws IOException 
	 * @throws ErrorFormatException 
	 * 
	 */
	IPCookieBo readIPCookieRecord(BufferedReader reader) throws IOException, ErrorFormatException;
	
	/**
	 * 生成老的bo对象
	 * @author zengyunfeng
	 * @param reader
	 * @return
	 * @throws IOException
	 * @throws ErrorFormatException
	 */
	public SiteStatBo readOldRecord(BufferedReader reader)
			throws IOException, ErrorFormatException;
	
	
	/**
	 * 保存所有的文件
	 * @author zengyunfeng
	 * @param list
	 * @throws IOException 
	 * 
	 * @Deprecated 使用明文存储，取代二进制文件 @beidou 1.2.33
	 */
	@Deprecated
	void persistentAll(ObjectOutputStream output, List<SiteStatExtBo> list) throws IOException;
	
	
	/**
	 * 保持每日汇总文件
	 * 
	 * @author zhuqian
	 * @since beidou1.2.33
	 * 
	 * @param output
	 * @param list
	 * @throws IOException
	 */
	void persistentAll(FileOutputStream output, List<SiteStatBo> list) throws IOException;
	
	
	/**
	 * 保存所有的文件
	 * @author zengyunfeng
	 * @param list
	 * @throws IOException 
	 */
	void persistentOldAll(ObjectOutputStream output, List<SiteStatBo> list) throws IOException;
	
	/**
	 * 保存所有的文件
	 * @author zengyunfeng
	 * @param list
	 * @throws IOException 
	 */
	void persistentAllIPCookie(ObjectOutputStream output, List<IPCookieBo> list) throws IOException;
	
	/**
	 * 读取一条记录
	 * @author zengyunfeng
	 * @param input
	 * @return 返回null表示读到文件结尾
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * 
	 * @Deprecated 使用明文存储，取代二进制文件 @beidou 1.2.33
	 */
	@Deprecated
	SiteStatBo next(ObjectInputStream input) throws IOException, ClassNotFoundException;
	
	/**
	 * 读取一行7天平均数据文件（明文存储）
	 * 
	 * @since beidou1.2.33
	 * @author zhuqian
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 * @throws ErrorFormatException
	 */
	SiteStatBo next(BufferedReader reader) throws IOException, ErrorFormatException;
	
	/**
	 * 读取一条记录
	 * 
	 * @author zengyunfeng
	 * @param input
	 * @return 返回null表示读到文件结尾
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	IPCookieBo nextIp(ObjectInputStream input) throws IOException, ClassNotFoundException;

}
