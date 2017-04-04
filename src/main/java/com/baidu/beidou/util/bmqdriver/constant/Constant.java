package com.baidu.beidou.util.bmqdriver.constant;

import java.util.Properties;

/**
 * ClassName: ConstantFile
 * Function: TODO ADD FUNCTION
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 1.0.0
 * @since cpweb-325
 * @date Oct 10, 2011
 * @see 
 */
public class Constant {
	
	// 配置信息
	public static final String configFileName = "bmq";
	public static Properties CONFIG_MEM_POP;
	
	// 区分新建修改URL，和轮巡URL
	// 0为新建或者修改URl检查，1为轮巡URL检查
	public static int URL_CHECK_TYPE_INSTANT = 0;
	public static int URL_CHECK_TYPE_PATROL = 1;
	
	// 任务类型. 
	// 0: 新url连通性及跳转检查（刚提交的url或编辑的url）
	// 1: url连通性及跳转恢复巡查(不连通的或不符合跳转限制的URL)
	// 2: url连通性拒绝巡查(连通且符合跳转限制的URL)
	// 3: url病毒木马恢复巡查（含有病毒木马的URL）
	// 4: url病毒木马拒绝巡查（不含有病毒木马的URL）
	public static final int URL_CHECK_INSTANT = 0;
	public static final int URL_CHECK_PATROL_PASS = 1;
	public static final int URL_CHECK_PATROL_REFUSE = 2;
	public static final int URL_CHECK_VIRUS_PASS = 3;
	public static final int URL_CHECK_VIRUS_REFUSE = 4;
	
	// result：
	// 0 : URL连通性正常且符合跳转限制（对应type=0、1、2的结果）
	// 1 : URL不连通（对应type=0、1、2的结果）
	// 2 : URL跳转不符合规范（对应type=0、1、2的结果）
	// 3 : URL含有病毒木马（对应type=3、4的结果）
	// 4 : URL没有病毒木马 对应type=3、4的结果）
	public static final int URL_RESULT_CONNECTED = 0;
	public static final int URL_RESULT_NOT_CONNECTED = 1;
	public static final int URL_RESULT_ILLEGAL_JUMP = 2;
	public static final int URL_RESULT_WITH_VIRUS = 3;
	public static final int URL_RESULT_WITHOUT_VIRUS = 4;
}
