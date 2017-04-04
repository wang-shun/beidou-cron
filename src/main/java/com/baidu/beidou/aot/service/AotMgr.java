package com.baidu.beidou.aot.service;

/**
 * ClassName:AotMgr
 * Function: TODO ADD FUNCTION
 *
 * @author   <a href="mailto:yang_yun@baidu.com">阳云</a>
 * @version  
 * @since    cpweb203 账户优化
 * @Date	 2010-12-6		下午11:59:23
 * @see 	 
 */
public interface AotMgr {
	
	/**
	 * importDBInfo:导入主库中昨日部分数据到账户优化库
	 * cproplanstat:推广计划在线时长、有效时长
	 * cprogroupstat:推广组出价、一级地域数量、二级地域数量、所选网站平均出价、所选网站固定总展现量、所选网站悬浮总展现量
	 *      
	 * @since cpweb203 账户优化
	 * @author yang_yun
	 * @Date	 2010-12-7		上午12:09:11
	*/
	public void importDBInfo();

}
