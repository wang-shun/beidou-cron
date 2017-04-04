/**
 * 2009-4-21 上午10:14:44
 */
package com.baidu.beidou.unionsite.dao;

import java.io.BufferedReader;
import java.io.IOException;

import com.baidu.beidou.unionsite.bo.UnionSiteBo;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;

/**
 * @author zengyunfeng
 * @version 1.0.7
 * 联盟站点的文件接口，包含读取文件，校验记录，获得一条联盟站点对象
 */
public interface UnionSiteFileDao {

	/**
	 * 读取一行联盟记录
	 * @author zengyunfeng
	 * @param reader 联盟接口文件reader
	 * @return 校验，处理后的文件，如何文件格式不符合要求，记error日志，返回null
	 * @throws IOException 文件读取失败
	 * @throws ErrorFormatException 输入格式错误
	 */
	UnionSiteBo readRecord(BufferedReader reader) throws IOException, ErrorFormatException;
}
