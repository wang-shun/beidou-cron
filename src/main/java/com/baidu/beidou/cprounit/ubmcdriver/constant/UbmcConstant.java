package com.baidu.beidou.cprounit.ubmcdriver.constant;

/**
 * ClassName: UbmcConstant
 * Function: UBMC相关的一些常量定义
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 25, 2013
 */
public class UbmcConstant {
	
	/**
	 * UBMC批量接口的一次读写请求最大限制个数
	 */
	public static final Integer UBMC_BATCH_READ_MAX = 50;
	public static final Integer UBMC_BATCH_WRITE_MAX = 10;
	public static final Integer UBMC_BATCH_GET_DATA_MAX = 5;
	
	/**
	 * response中ResultBean内map中的key
	 */
	public static final String RESULT_MAP_MCID = "mcId";
	public static final String RESULT_MAP_VERSIONID = "versionId";
	public static final String RESULT_MAP_VALUE = "value";
	public static final String RESULT_MAP_MEDIAIDS = "mediaIds";
	public static final String RESULT_MAP_BINARY_DATA = "binaryData";
	
	/**
	 * value中字段中key与value分隔符为:
	 */
	public static final String VALUE_ITEM_KV_DELIMITER = ":";
	
	/**
	 * value中各个字段的分隔符为\t
	 */
	public static final String VALUE_ITEM_DELIMITER = "\t";

    /**
     * value中某个item项中以逗号分隔
     */
    public static final String VALUE_ITEM_ELEMENT_DELIMITER = ",";

    /**
     * value中attribute多个媒体占位符以逗号分隔
     */
    public static final String VALUE_ATTRIBUTE_ITEM_DELIMITER = VALUE_ITEM_ELEMENT_DELIMITER;

	/**
	 * value中各个字段的名称：
	 * 		wuliaoType : 物料类型，1（文字），2（图片），3（flash），5（图文），99（图标，检索端不适用该id）
	 * 		title : 标题
	 * 		description1 : 描述1，文字或者图文物料中存在
	 * 		description2 : 描述2，文字或者图文物料中存在
	 * 		showUrl : 显示URL
	 * 		targetUrl : 点击URL
	 * 		wirelessShowUrl : 无线显示URL
	 * 		wirelessTargetUrl : 无线点击URL
	 * 		width : 宽度
	 * 		height : 高度
	 * 		fileSrc : ubmc存储的占位符（%%BEGIN_MEDIA%%...%%END_MEDIA%%）或者预览URL
	 * 		fileSrcMd5 : fileSrc存储物料的md5值
	 * 		attribute : admaker那边制作的flash物料时保存的素材表，
	 * 				其格式为一批占位符（%%BEGIN_MEDIA%%...%%END_MEDIA%%）或者预览URL，其中以逗号隔开
	 * 		refMcId : admaker那边制作的flash物料时保存的素材mcId表，其格式为一批mcId，其中以逗号隔开
	 * 		descInfo : admaker那边制作的多媒体物料保存的文字描述信息
	 * 		snapshot ： admaker那边制作的多媒体物料投放google的截图
	 * 		templateId ：智能创意模板ID
	 * 
	 * 	推广组附加信息属性：
	 * 		groupId ：推广组ID
	 * 		phoneId ：电话ID
	 * 		phone ：电话
	 *      msgPhoneId : 短信电话ID
	 *      msgPhone ： 短信电话
	 *      msgContent ： 短信内容
	 *      subUrlParam : 子链统计参数
     *      subUrlTitle : 静态子链标题
     *      subUrlLink : 静态子链URL
     *      subUrlWirelessLink : 静态子链移动URL
	 */
	public static final String VALUE_ITEM_WULIAO_TYPE = "wuliaoType";
	public static final String VALUE_ITEM_TITLE = "title";
	public static final String VALUE_ITEM_DESC1 = "description1";
	public static final String VALUE_ITEM_DESC2 = "description2";
	public static final String VALUE_ITEM_SHOW_URL = "showUrl";
	public static final String VALUE_ITEM_TARGET_URL = "targetUrl";
	public static final String VALUE_ITEM_WIRELESS_SHOW_URL = "wirelessShowUrl";
	public static final String VALUE_ITEM_WIRELESS_TARGET_URL = "wirelessTargetUrl";
	public static final String VALUE_ITEM_WIDTH = "width";
	public static final String VALUE_ITEM_HEIGHT = "height";
	public static final String VALUE_ITEM_FILESRC = "fileSrc";
	public static final String VALUE_ITEM_FILESRC_MD5 = "fileSrcMd5";
	public static final String VALUE_ITEM_ATTRIBUTE = "attribute";
	public static final String VALUE_ITEM_REF_MCID = "refMcId";
	public static final String VALUE_ITEM_DESC_INFO = "descInfo";
	public static final String VALUE_ITEM_SNAPSHOT = "snapshot";
	public static final String VALUE_ITEM_TEMPLATEID = "templateId";
    public static final String VALUE_ITEM_TOOL_TYPE = "toolType";
	
	public static final String VALUE_ITEM_GROUPID = "groupId";
	public static final String VALUE_ITEM_PHONEID = "phoneId";
	public static final String VALUE_ITEM_PHONE = "phone";
	public static final String VALUE_ITEM_MSG_PHONEID = "msgPhoneId";
	public static final String VALUE_ITEM_MSG_PHONE = "msgPhone";
	public static final String VALUE_ITEM_MSG_CONTENT = "msgContent";
    public static final String VALUE_ITEM_SUB_URL_PARAM = "subUrlParam";
    public static final String VALUE_ITEM_SUB_URL_TITLE = "subUrlTitle";
    public static final String VALUE_ITEM_SUB_URL_LINK = "subUrlLink";
    public static final String VALUE_ITEM_SUB_WIRELESS_URL_LINK = "subUrlWirelessLink";
    
    public static final int VALUE_ITEM_TOOL_TYPE_ADMAKER = 0;   // 新增的，admaker暂未使用
    public static final int VALUE_ITEM_TOOL_TYPE_NICHANG = 1;   // 霓裳入门版标记

    /**
     * 用于测试
     */
    public static final String VALUE_ITEM_TEST = "test";
    
	/**
	 * value中空的多媒体占位符
	 */
	public static final String VALUE_MEDIA_PLACEHOLDER = "%%BEGIN_MEDIA%%%%END_MEDIA%%";
	public static final String VALUE_MEDIA_PLACEHOLDER_IMAGE = "%%BEGIN_MEDIA%%type=jpg%%END_MEDIA%%";
	public static final String VALUE_MEDIA_PLACEHOLDER_FLASH = "%%BEGIN_MEDIA%%type=swf%%END_MEDIA%%";
}
