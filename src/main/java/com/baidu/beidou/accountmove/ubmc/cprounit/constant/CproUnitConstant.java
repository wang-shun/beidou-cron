/**
 * 
 */
package com.baidu.beidou.accountmove.ubmc.cprounit.constant;

import org.apache.commons.lang.StringUtils;



/**
 * @author zengyunfeng
 * @version 1.0.0
 */
public class CproUnitConstant {
	/**
	 * 广告行业分类的无效值（缺省）
	 */
	public static final int UNIT_TRADE_INVALID = 0;
	
	/**
	 *  推广单元提交类型：用户提交
	 */
	public static final int UNIT_SUBMITTYPE_USER = 0;
	
	/**
	 * 推广单元提交类型：代理提交
	 */
	public static final int UNIT_SUBMITTYPE_PROXY = 1;
	
	
	public static final String[] UNIT_SUBMITTYPE_NAME = new String[]{"用户提交","代理提交"};
	
	/**
	 * 推广单元状态-所有(用于数据库过滤，-1表示不过滤，即全部)
	 */
	public final static int UNIT_QUERY_STATE_ALL = -1;
	
	/**
	 * 推广单元状态(数据库状态) *北斗星* ：
	 */
	public static final int UNIT_STATE_NORMAL	= 0;//有效
	public static final int UNIT_STATE_PAUSE	= 1;//暂停
	public static final int UNIT_STATE_DELETE	= 2;//删除
	public static final int UNIT_STATE_AUDITING	= 3;//审核中
	public static final int UNIT_STATE_REFUSE	= 4;//审核拒绝
	
	/**
	 * 推广创意是否已从DRMC同步至UBMC：0表示未同步，1表示已同步
	 * 
	 */
	public static final int UBMC_SYNC_FLAG_NO	= 0; // 未同步
	public static final int UBMC_SYNC_FLAG_YES	= 1; // 已同步
	public static final int UBMC_MC_DEFAULT_VERSION = 1; // ubmc物料默认版本为1
	
	/**
	 * 推广创意是否已从UBMC同步至DRMC：0表示未同步，1表示已同步
	 */
	public static final int DRMC_SYNC_FLAG_NO	= 0; // 未同步
	public static final int DRMC_SYNC_FLAG_YES	= 1; // 已同步
	
	/**
	 * 推广单元显示状态 *北斗星* ：
	 */
	public static final int UNIT_VIEW_STATE_NORMAL			= 0;//有效
	public static final int UNIT_VIEW_STATE_PAUSE			= 1;//暂停
	public static final int UNIT_VIEW_STATE_DELETE			= 2;//删除
	public static final int UNIT_VIEW_STATE_AUDITING		= 3;//审核中
	public static final int UNIT_VIEW_STATE_REFUSE			= 4;//审核拒绝
	public final static int UNIT_VIEW_STATE_PLAN_NOTBEGIN 	= 5;//推广计划未开始
	public final static int UNIT_VIEW_STATE_PLAN_END 		= 6;//推广计划已结束
	public final static int UNIT_VIEW_STATE_PALN_OFFLINE 	= 7;//推广计划已下线	
	public final static int UNIT_VIEW_STATE_PLAN_PAUSE 		= 8;//推广计划已暂停
	public final static int UNIT_VIEW_STATE_PALN_DELETE 	= 9;//推广计划已删除
	public final static int UNIT_VIEW_STATE_GROUP_PAUSE 	= 10;//推广组已暂停
	public final static int UNIT_VIEW_STATE_GROUP_DELETE 	= 11;//推广组已删除
	
	
	/**
	 * 推广单元状态的显示名称 *北斗星* 
	 */
	public static final String[] UNIT_STATE_NAME = new String[]{
		"有效",
		"暂停",
		"删除",
		"审核中",
		"审核拒绝",
		"推广计划未开始", 
		"推广计划已结束", 
		"推广计划已暂停", 
		"推广计划已删除",
		"推广计划已下线",
		"推广组已暂停",
		"推广组已删除"
		};
	
	/**
	 * 推广单元状态：用于批量修改，动词
	 */
	public static final String UNIT_STATE_STRING_AVAILABLE = "available";	//有效
	public static final String UNIT_STATE_STRING_HANG = "hang";				//暂停
	public static final String UNIT_STATE_STRING_DELETE = "delete";			//删除
	
	
	/**
	 * 推广单元是否已同步：已同步
	 */
	public static final int UNIT_SYN = 1;
	
	/**
	 * 推广单元是否已同步：未同步
	 */
	public static final int UNIT_NOT_SYN = 0;
	
	/**
	 * 推广单元文字物料是否允许只出标题：允许
	 */
	public static final int UNIT_IFTITLE = 1;
	
	/**
	 * 推广单元文字物料是否允许只出标题：不允许
	 */
	public static final int UNIT_NOT_IFTITLE = 0;
	
	/**
	 * flash版本的要求 
	 */
	public static final int FLASH_VERSION = 7;
	
	
	
	/**
	 * 推广单元物料类型：文字
	 */
	public static final int MATERIAL_TYPE_LITERAL = 1;
	
	/**
	 * 推广单元物料类型：图片
	 */
	public static final int MATERIAL_TYPE_PICTURE = 2;
	
	/**
	 * 推广单元物料类型：flash
	 */
	public static final int MATERIAL_TYPE_FLASH = 3;
	
	/**
	 * 推广单元物料类型：图文混排
	 */
	public static final int MATERIAL_TYPE_LITERAL_WITH_ICON = 5;
	
	/**
	 * 推广单元物料类型：智能创意
	 */
	public static final int MATERIAL_TYPE_SMART_IDEA = 9;
	
	/**
	 * 文本智能创意使用常量
	 */
	public static final int LITERAL_SMART_PRVIEW_WIDTH = 0;											//构造文本智能创意预留请求实用
	public static final int LITERAL_SMART_PRVIEW_HEIGHT = 0;										//构造文本智能创意预留请求实用
	
	/**
     * 图文智能创意使用常量
     */
    public static final int LITERAL_WITH_ICON_SMART_PRVIEW_WIDTH = 0;                                         //构造图文智能创意预留请求实用
    public static final int LITERAL_WITH_ICON_SMART_PRVIEW_HEIGHT = 0;                                        //构造图文智能创意预留请求实用
	public static final int LITERAL_WITH_ICON_USAGE_DEFAULT = 1;                                              //图文模板中该元素未启用icon属性
	public static final int LITERAL_WITH_ICON_USAGE_ICON = 2;                                                 //图文模板中该元素启用icon属性
	/**
	 * 本文智能创意保持db时，由于das限制必须有title、desc1、desc2，但其content时保存在模板参数信息中，因此统一set默认值
	 */
	public static final String LITERAL_SMART_TITLE = "smart_text_title";										
	public static final String LITERAL_SMART_DESC_1 = "smart_text_desc1";
	public static final String LITERAL_SMART_DESC_2 = "smart_text_desc2";
	
	/**
     * 图文智能创意保持db时，由于das限制必须有title、desc1、desc2，但其content时保存在模板参数信息中，因此统一set默认值
     */
    public static final String LITERAL_WITH_ICON_SMART_TITLE = "smart_text_with_icon_title";                                        
    public static final String LITERAL_WITH_ICON_SMART_DESC_1 = "smart_text_with_icon_desc1";
    public static final String LITERAL_WITH_ICON_SMART_DESC_2 = "smart_text_with_icon_desc2";
	
	/**
	 * 智能创意标志
	 */
	public static final int IS_SMART_TRUE = 1;
	public static final int IS_SMART_FALSE = 0;
	
	/**
	 * 北斗预览工具图片类型：预览工具上传的图片或者flash等
	 */
	public static final int MATERIAL_TYPE_PREVIEW_IMAGE = 97;
	
	/**
	 * admaker物料类型：admaker制作的图片或者flash
	 */
	public static final int MATERIAL_TYPE_ADMAKER = 98;
	
	/**
	 * 推广单元物料类型：图标（业务端使用，存储于drmc，以便解析处理）
	 */
	public static final int MATERIAL_TYPE_ICON = 99;
	
	/**
	 * 图文物料默认尺寸
	 */
	public static final int LITERAL_WITH_ICON_DEFAULT_WIDTH = 60;
	public static final int LITERAL_WITH_ICON_DEFAULT_HEIGHT = 60;
	/**
	 * 图标物料默认的最大尺寸
	 */
	public static final int LITERAL_WITH_ICON_DEFAULT_SIZE = 56320;
	
	/**
	 * 推广单元创建方式:本地上传
	 */
	public static final int MATERIAL_FROM_LOCAL = 1;
	
	/**
	 * 推广单元创建方式:从工具导入
	 */
	public static final int MATERIAL_FROM_TOOL = 2;
	
	/**
	 * 是否为互动创意标示
	 * add by dongying since 492
	 */
	public static final int INTERACTIVE_AD_TRUE = 1;
	public static final int INTERACTIVE_AD_FALSE = 0;
	
	/**
	 * 物料类型的显示名称
	 * added by zhuqian 2009-03-05
	 */
	public static final String[] WULIAO_TYPE_NAME = new String[]{"","文字","图片","Flash","","文字","","","","智能"};//[5]实质为图文创意[9]为智能创意
	
		
	public static final char MC_CODE_REPLACE_SLASH= '-';
	
	
	/**
	 * 页面的缩略图的最大宽度
	 */
	public static final int PAGE_IMG_WIDTH = 240;
	
	/**
	 * 页面缩略图的最大高度
	 */
	public static final int PAGE_IMG_HEIGTH = 100;
	
	/**
	 * 同一个推广组下的最多推广单元数
	 */
	public static int MAX_UNIT_NUMBER =100;	
	
	public static final String MC_MATERIAL_NAME= "material";
	
	/**
	 * 列表数据的返回json格式的名称
	 */
	public static final String DATA_KEY = "dataList";
	
	/**
	 * 起始时间的返回json格式的名称
	 */
	public static final String DATE_START_KEY = "dateStart";
	
	/**
	 * 结束时间的返回json格式的名称
	 */
	public static final String DATE_END_KEY = "dateEnd";
	
	/**
	 * 已经进入过审核员列表中的状态辅助位,辅助位为1的推广单元不能进行修改
	 */
	public static final int UNIT_AUDITING= 1;	
	
	/**
	 * 已经进入过审核员列表中的状态辅助位,辅助位为1的推广单元不能进行修改
	 */
	public static final int UNIT_MAN_CATEGORY= 2;	
	
	/********************drmc constant****************************/
	/**
	 * drmc成功状态值
	 */
	public static final int DRMC_STATUS_OK = 0;

	/**
	 * drmc删除物料失败
	 */
	public static final int DRMC_DEL_FAIL = 1;
	
	/**
	 * drmc备份物料失败
	 */
	public static final int DRMC_BACKUP_FAIL = -1;
	
	
	/**
	 * drmc拷贝物料失败
	 */
	public static final int DRMC_COPY_FAIL = -1;
	
	/**
	 * drmc提供的beidou文字物料类型值
	 */
	public static final int DRMC_MATTYPE_LITERAL = 2022;//30;
	
	/**
	 * drmc提供的beidou图片物料类型值
	 */
	public static final int DRMC_MATTYPE_IMAGE = 2023;//31;
	
	/**
	 * drmc提供的beidou图文物料类型值
	 */
	public static final int DRMC_MATTYPE_LITERAL_WITH_ICON = 2021;//2004;

	
	/**
	 * drmc提供的beidou图标物料类型值
	 */
	public static final int DRMC_MATTYPE_ICON = 2003;
	
	/**
	 * DRMC的临时物料，用于物料删除表中的标识
	 */
	public static final int DRMC_MATGROUP_TMP=0;
	
	/**
	 * DRMC的正式物料，用于物料删除表中的标识
	 */
	public static final int DRMC_MATGROUP_FORMAL=1;
	
	/**
	 * DRMC的历史物料，用于物料删除表中的标识
	 */
	public static final int DRMC_MATGROUP_HISTORY=2;
	
	/**
	 * 与admaker之间传递info信息时的加密key
	 */
	public static final String ADMAKER_INFO_KEY = "itisMYmc";
	
	/*
	 * added by genglei
	 * 上传文件是否成功，成功为0，失败为1
	 */
	public static final int UPLOAD_SUCCESS = 1;
	public static final int UPLOAD_FAILED = 0;
	
	/*
	 * added by genglei
	 * 上传文件失败时的错误信息反馈：
	 * 1：上传图片失败；
     * 2：不支持gif格式的贴片创意；
     * 3：不支持该尺寸；
     * 4：图片颜色空间错误；
     * 5：内部处理出错；
     * 6：推广组类型未指定；
     * 7：文件类型错误。
     * -------------------------------------------
     * 以下为针对图标上传的错误信息,add by dongying
     * 8：图文创意仅支持固定推广组
     * 9：图标尺寸不为60*60
     * 10:图标文件格式不正确
	 */
	public static final int UPLOAD_ERROR_TYPE_1 = 1;
	public static final int UPLOAD_ERROR_TYPE_2 = 2;
	public static final int UPLOAD_ERROR_TYPE_3 = 3;
	public static final int UPLOAD_ERROR_TYPE_4 = 4;
	public static final int UPLOAD_ERROR_TYPE_5 = 5;
	public static final int UPLOAD_ERROR_TYPE_6 = 6;
	public static final int UPLOAD_ERROR_TYPE_7 = 7;
	//以下为针对图标上传的错误信息,add by dongying
	public static final int UPLOAD_ERROR_TYPE_8 = 8;
	public static final int UPLOAD_ERROR_TYPE_9 = 9;
	public static final int UPLOAD_ERROR_TYPE_10 = 10;
	
    // 上传请求内登录验证用的参数名
    public static final String UPLOAD_PARAMETER = "bd";
    
    // 影子创意上下线标志
    public static final String SHADOW_UNIT_ONLINE = "shadow_unit_online";
    public static final String SHADOW_UNIT_OFFLINE = "shadow_unit_offline";
    

	/**
	 * 验证目标状态是否有效
	 * 
	 * @param targetState 创意的目标状态
	 * @return
	 */
	public static boolean isValidUnitState(String targetState){
		if(targetState==null||targetState.equals("")){
			return false;
		}
		if(UNIT_STATE_STRING_DELETE.equals(targetState)||UNIT_STATE_STRING_AVAILABLE.equals(targetState)||UNIT_STATE_STRING_HANG.equals(targetState)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 根据创意的string值返回对应的int值
	 * 
	 * @param state 创意的状态
	 * @return
	 */
	public static int getUnitState(String state){
		if(state==null){
			return UNIT_QUERY_STATE_ALL;
		}else if(UNIT_STATE_STRING_AVAILABLE.equals(state)){
			return UNIT_STATE_NORMAL;
		}else if(UNIT_STATE_STRING_HANG.equals(state)){
			return UNIT_STATE_PAUSE;
		}else if(UNIT_STATE_STRING_DELETE.equals(state)){
			return UNIT_STATE_DELETE;
		}else{
			return UNIT_QUERY_STATE_ALL;
		}
	}
	
	public static final String[] BMOB_WIRELESS_TARGET_URL_PREFIXS = new String[]{
		"http://siteapp.baidu.com/app/union/?src=",
		"http://siteapp.baidu.com/site/",
		"http://page.baidu.com/"
	};
	
	public static final String[] BMOB_WIRELESS_SHOW_URL_PREFIXS = new String[]{
		"siteapp.baidu.com/app/union/?src=",
		"siteapp.baidu.com/site/",
		"page.baidu.com/"
	};

	public static String filterWirelessTargetUrlPrefix(String wirelessTargetUrl) {
		if (StringUtils.isEmpty(wirelessTargetUrl)) {
			return "";
		}
		String result = wirelessTargetUrl;
		for (String prifix : BMOB_WIRELESS_TARGET_URL_PREFIXS) {
			if (wirelessTargetUrl.startsWith(prifix)) {
				result = wirelessTargetUrl.replace(prifix, "");
				break;
			}
		}
		return result;
	}

	public static String filterWirelessShowUrlPrefix(String wirelessShowUrl) {
		if (StringUtils.isEmpty(wirelessShowUrl)) {
			return "";
		}
		String result = wirelessShowUrl;
		for (String prefix : BMOB_WIRELESS_SHOW_URL_PREFIXS) {
			if (wirelessShowUrl.startsWith(prefix)) {
				result = wirelessShowUrl.replace(prefix, "");
				break;
			}
		}
		return result;
	}
	
	/**
	 * wireless targeturl未被修改，或者被修改成和pc target url一致
	 */
	public static int WIRELESS_URL_MODIFIED_FALSE = 0;

	/**
	 * wireless targeturl被修改，且被修改成和pc target url不一致
	 */
	public static int WIRELESS_URL_MODIFIED_TRUE = 1;

	/**
	 * 对比创意的pc和wireless  target url
	 * 
	 * @param pcTargetUrl
	 * @param wirelessTargetUrl
	 * @return
	 * 
	 * 		  true: pc和wireless的target url不一致
	 * 		  false: pc和wireless的target url一致
	 */
	public static boolean isWirelessTargetUrlModified(String pcTargetUrl, String wirelessTargetUrl) {
		if (StringUtils.isEmpty(pcTargetUrl) && StringUtils.isEmpty(wirelessTargetUrl)) {
			return false;
		}
		if (StringUtils.isEmpty(pcTargetUrl) && StringUtils.isNotEmpty(wirelessTargetUrl)) {
			return true;
		}
		if (StringUtils.isNotEmpty(pcTargetUrl) && StringUtils.isEmpty(wirelessTargetUrl)) {
			return true;
		}
		String wirelessUrl = filterWirelessTargetUrlPrefix(wirelessTargetUrl);
		wirelessUrl = filterUrlPrefix(wirelessUrl);
		wirelessUrl = filterUrlSuffix(wirelessUrl);

		String targetUrl = filterUrlPrefix(pcTargetUrl);
		targetUrl = filterUrlSuffix(targetUrl);

		if (targetUrl.equalsIgnoreCase(wirelessUrl)) {
			return false;
		}
		return true;
	}

	/**
	 * 对比创意的pc和wireless  show url
	 * 
	 * @param pcShowUrl
	 * @param wirelessShowUrl
	 * @return
	 * 			true: pc和wireless的show url不一致
	 * 			false: pc和wireless的show url一致
	 */
	public static boolean isWirelessShowUrlModified(String pcShowUrl, String wirelessShowUrl) {
		if (StringUtils.isEmpty(pcShowUrl) && StringUtils.isEmpty(wirelessShowUrl)) {
			return false;
		}
		if (StringUtils.isEmpty(pcShowUrl) && StringUtils.isNotEmpty(wirelessShowUrl)) {
			return true;
		}
		if (StringUtils.isNotEmpty(pcShowUrl) && StringUtils.isEmpty(wirelessShowUrl)) {
			return true;
		}
		String wirelessUrl = filterWirelessShowUrlPrefix(wirelessShowUrl);
		wirelessUrl = filterUrlPrefix(wirelessUrl);
		wirelessUrl = filterUrlSuffix(wirelessUrl);

		String showUrl = filterUrlPrefix(pcShowUrl);
		showUrl = filterUrlSuffix(showUrl);
		if (showUrl.equalsIgnoreCase(wirelessUrl)) {
			return false;
		}
		return true;
	}

	private static String filterUrlSuffix(String wirelessUrl) {
		if (StringUtils.isEmpty(wirelessUrl)) {
			return String.valueOf("");
		}
		if (wirelessUrl.endsWith("/")) {
			wirelessUrl = wirelessUrl.substring(0, (wirelessUrl.length() - 1));
		}
		return wirelessUrl;
	}

	private static String filterUrlPrefix(String url) {
		if (StringUtils.isEmpty(url)) {
			return String.valueOf("");
		}
		if (url.startsWith("http://")) {
			url = url.substring("http://".length(), url.length());
		}
		return url;
	}

	public static void main(String[] args) {
		String targetUrl = "http://siteapp.baidu.com/app/union/?src=globeedu.com?a=3";
		System.out.println(filterWirelessTargetUrlPrefix(targetUrl));

		String showUrl = "page.baidu.com/www.abc/?s=44&s=9/";
		System.out.println(filterWirelessShowUrlPrefix(showUrl));

		System.out.println(filterUrlSuffix(showUrl));

		String pcTargetUrl = "http://baidu.com";
		String wirelessTargetUrl = "http://baidu.com/";
		System.out.println(isWirelessTargetUrlModified(pcTargetUrl, wirelessTargetUrl));

		System.out.println(filterUrlPrefix("http://siteapp.baidu.com/app/union/?src=baidu.com/"));
	}
	
}
