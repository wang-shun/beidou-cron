package com.baidu.beidou.unionsite.service;

import java.io.IOException;

/**
 * InterfaceName: WM123SiteurlService <br>
 * Function: 根据siteurl list文件，生成一级域名包含www.开头的另一份文件，供司南系统使用生成访客特征数据
 *
 * @author   <a href="mailto:zhangxu04@baidu.com">张旭</a>
 */
public interface WM123SiteurlService{

	/**
	 * 根据siteurl list文件，生成一级域名包含www.开头的另一份文件，供司南系统使用生成访客特征数据 <br>
	 * 
	 * input的siteurl文件格式如下：<br>
	 * ifeng.com <br>
	 * youku.com <br>
	 * sina.com.cn <br>
	 * 
	 * output的siteurl文件格式如下：<br>
	 * www.ifeng.com <br>
	 * www.youku.com <br>
	 * www.sina.com.cn <br>
	 * 
	 * @param file
	 * @return 
	 */
	public void getSiteurl4SN(String src_file, String dest_file) throws IOException;
}
