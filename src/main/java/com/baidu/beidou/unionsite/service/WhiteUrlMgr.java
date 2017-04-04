/**
 * 2010-5-20 下午07:57:21
 */
package com.baidu.beidou.unionsite.service;

import java.io.IOException;
import java.util.List;

import com.baidu.beidou.unionsite.bo.WhiteUrl;

/**
 * @author zengyunfeng
 */
public interface WhiteUrlMgr {

	/**
	 * 
	 * getWhiteList: 生成白名单列表,主要完成如下几个任务<br>
	 * 		1. trim, 域名小写替换，去除开头的http://, https:// ;<br>
	 * 		2. 按照url进行排序，去除包含关系（保留范围最小的url）;<br>
	 * 		3. 获取相应的主域id，如果不存在，则需要生成id,并更新数据库。
	 *
	 * @param file 白名单文件的文件名
	 * @return      
	 * @throws IOException 无法打开白名单文件
	 * @since 1.0.51
	 */
	public List<WhiteUrl> getWhiteList(String file) throws IOException;
}
