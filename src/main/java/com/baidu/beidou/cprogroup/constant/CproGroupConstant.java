package com.baidu.beidou.cprogroup.constant;


public class CproGroupConstant {

	/**
	 * 推广组状态-有效
	 */
	public final static int GROUP_QUERY_STATE_ALL = -1;
	
	/**
	 * 推广组状态-有效
	 */
	public final static int GROUP_STATE_NORMAL = 0;
	/**
	 * 推广组状态-暂停
	 */
	public final static int GROUP_STATE_PAUSE = 1;
	/**
	 * 推广组状态-删除
	 */
	public final static int GROUP_STATE_DELETE = 2;

	/**
	 * 推广组显示状态-有效
	 */
	public final static int GROUP_VIEW_STATE_NORMAL = 0;

	/**
	 * 推广组显示状态-暂停
	 */
	public final static int GROUP_VIEW_STATE_PAUSE = 1;

	/**
	 * 推广组显示状态-删除
	 */
	public final static int GROUP_VIEW_STATE_DELETE = 2;

	/**
	 * 推广组显示状态-推广计划未开始
	 */
	public final static int GROUP_VIEW_STATE_PLAN_NOTBEGIN = 3;
	/**
	 * 推广组显示状态-推广计划已结束
	 */
	public final static int GROUP_VIEW_STATE_PLAN_END = 4;
	/**
	 * 推广组显示状态-推广计划已暂停
	 */
	public final static int GROUP_VIEW_STATE_PLAN_PAUSE = 5;

	/**
	 * 推广组显示状态-推广计划已删除
	 */
	public final static int GROUP_VIEW_STATE_PALN_DELETE = 6;

	public final static String[] GROUP_VIEW_STATE_NAME = new String[] { "有效",
			"暂停", "删除", "推广计划未开始", "推广计划已结束", "推广计划已暂停", "推广计划已删除" };
	
	public final static String[] GROUP_UI_STATE_NAME = new String[]{"available", "hang", "delete", "plan-non-begin", "plan-end", "plan-hang", "plan-delete"};

	//update by zhangpingan cpweb-443(北斗3.0)
	public static final int GROUP_TARGET_TYPE_NONE=0;//不定向，默认选项
	public static final int GROUP_TARGET_TYPE_KT=1;//主题词定向策略
	public static final int GROUP_TARGET_TYPE_QT=2;//搜索定向策略
	public static final int GROUP_TARGET_TYPE_HCT=4;
	public static final int GROUP_TARGET_TYPE_RT=8;//人群定向策略
	public static final int GROUP_TARGET_TYPE_VT=16;//人群定向-到访定向策略
	public static final int GROUP_TARGET_TYPE_IT=32; //兴趣定向
	public static final int GROUP_TARGET_TYPE_CT = 1; // 基于当前流量页面主题词的定向
    public static final int GROUP_TARGET_TYPE_PACK = 64; // 受众定向
    public static final int GROUP_TARGET_TYPE_AT_RIGHT = 128; // AT右定向
    public static final int GROUP_TARGET_TYPE_AT_LEFT = 256; // AT左定向
	
	/**
	 * 推广组配置的投放站点和分类的分隔符
	 */
	public final static String FIELD_SEPERATOR = "|";	
	
	public final static int GROUP_OP_TYPE_TMPPLAN = 0;
	
	public final static int GROUP_OP_TYPE_PLAN = 1;
	
	public final static int GROUP_FROM_FLAG_NORMAL = 0;
	
	public final static int GROUP_FROM_FLAG_CONFIRM = 1;
	
	public static int GROUPLIST_PAGESIZE = 20;
	
	/**
	 * 单个推广计划下在线（有效、暂停）推广组个数
	 */
	public static int GROUP_EFFECTIVE_MAX_NUM = 200;
	/**
	 * 单个计划下全部推广组的最多个数
	 */
	public static int GROUP_ALL_MAX_NUM = 1000;
	
	/**
	 * 单个用户下全部临时推广组的最多个数
	 */
	public static int TMPGROUP_ALL_MAX_NUM = 2000;
	
	/**
	 * 单个推广组下过滤IP的最大个数
	 */
	public static int IPFILTER_ALL_MAX_NUM = 50;
	
	/**
	 * 单个推广组下过滤网站的最大个数
	 */
	public static int SITEFILTER_ALL_MAX_NUM = 10000;
	
	/**
	 * 单个推广组下过滤网站的一次新增的最大个数
	 */
	public static int SITEFILTER_ONCE_MAX_NUM = 1000;
	
	/**
	 * 单个推广组下分站点价格和链接的最大个数
	 */
	public static int SITEPRICE_ALL_MAX_NUM = 1000;
	
	/**
	 * 单个推广组下分站点价格和链接的一次新增的最大个数
	 */
	public static int SITEPRICE_ONCE_MAX_NUM = 1000;
	
	/**
	 * 地域、网站信息js文件生成地址
	 */
	public static String SITEINFO_JS_TAGFILE_PATH = "d:\\";
	
	public static int SITEINFO_LIST_PAGESIZE = 20;
	/**
	 * 往内存中加载联盟网站、地域信息时，需要检查是否为空
	 */
	public final static int RELOAD_SITEINFO_NEED_CHECK = 1;
	
	/**
	 * 全站点
	 */
	public static final int GROUP_ALLSITE = 1; 
	
	/**
	 * 全地域
	 */
	public static final int GROUP_ALLREGION = 1; 
	
	/**
	 * 推广组类型
	 */
	public static final int GROUP_TYPE_FIXED = 1; 	//固定-bit表示，最低位
	public static final int GROUP_TYPE_FLOW = 2; 	//悬浮-bit表示，次低位
	public static final int GROUP_TYPE_FILM = 4;	//贴片-bit表示，第三位

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
	
	//推广组维护中选择网站可排序的title--流量
	public static final String SORTCOLUMN_FLUX = "siteFlux";
//	推广组维护中选择网站可排序的title--物料类型
	public static final String SORTCOLUMN_MATERIAL = "material";
//	推广组维护中选择网站可排序的title--网站行业
	public static final String SORTCOLUMN_LEVEL = "level";	
	
	//q值合法性的阀值
	public static int SITEINFO_Q1_THRESHOLD = 0;
	public static int SITEINFO_Q2_THRESHOLD = 0;
	
	/**
	 * 用户可选择网站个数限制
	 */
	public static int SITE_SELECTED_MAX_NUM = 3000;
	
	//=============================================================	
	/**
	 * @return the gROUP_ALL_MAX_NUM
	 */
	public static int getGROUP_ALL_MAX_NUM() {
		return GROUP_ALL_MAX_NUM;
	}

	/**
	 * @param group_all_max_num the gROUP_ALL_MAX_NUM to set
	 */
	public static void setGROUP_ALL_MAX_NUM(int group_all_max_num) {
		GROUP_ALL_MAX_NUM = group_all_max_num;
	}

	/**
	 * @return the gROUP_EFFECTIVE_MAX_NUM
	 */
	public static int getGROUP_EFFECTIVE_MAX_NUM() {
		return GROUP_EFFECTIVE_MAX_NUM;
	}

	/**
	 * @param group_effective_max_num the gROUP_EFFECTIVE_MAX_NUM to set
	 */
	public static void setGROUP_EFFECTIVE_MAX_NUM(int group_effective_max_num) {
		GROUP_EFFECTIVE_MAX_NUM = group_effective_max_num;
	}

	/**
	 * @return the gROUPLIST_PAGESIZE
	 */
	public static int getGROUPLIST_PAGESIZE() {
		return GROUPLIST_PAGESIZE;
	}

	/**
	 * @param grouplist_pagesize the gROUPLIST_PAGESIZE to set
	 */
	public static void setGROUPLIST_PAGESIZE(int grouplist_pagesize) {
		GROUPLIST_PAGESIZE = grouplist_pagesize;
	}

	/**
	 * @return the tMPGROUP_ALL_MAX_NUM
	 */
	public static int getTMPGROUP_ALL_MAX_NUM() {
		return TMPGROUP_ALL_MAX_NUM;
	}

	/**
	 * @param tmpgroup_all_max_num the tMPGROUP_ALL_MAX_NUM to set
	 */
	public static void setTMPGROUP_ALL_MAX_NUM(int tmpgroup_all_max_num) {
		TMPGROUP_ALL_MAX_NUM = tmpgroup_all_max_num;
	}

	/**
	 * @return the sITEINFO_JS_TAGFILE_PATH
	 */
	public static String getSITEINFO_JS_TAGFILE_PATH() {
		return SITEINFO_JS_TAGFILE_PATH;
	}

	/**
	 * @param siteinfo_js_tagfile_path the sITEINFO_JS_TAGFILE_PATH to set
	 */
	public static void setSITEINFO_JS_TAGFILE_PATH(String siteinfo_js_tagfile_path) {
		SITEINFO_JS_TAGFILE_PATH = siteinfo_js_tagfile_path;
	}

	/**
	 * @return the sITEINFO_LIST_PAGESIZE
	 */
	public static int getSITEINFO_LIST_PAGESIZE() {
		return SITEINFO_LIST_PAGESIZE;
	}

	/**
	 * @param siteinfo_list_pagesize the sITEINFO_LIST_PAGESIZE to set
	 */
	public static void setSITEINFO_LIST_PAGESIZE(int siteinfo_list_pagesize) {
		SITEINFO_LIST_PAGESIZE = siteinfo_list_pagesize;
	}

	/**
	 * 返回给前台的，推广组分网站价格和链接设置的网站无效<br>
	 * ADD BY zengyunfeng <br>
	 * @version 1.1.2
	 */
	public final static int SITE_PRICE_INVALID=1;
	
	/**
	 * 返回给前台的，推广组分网站价格和链接设置的网站有效效<br>
	 * ADD BY zengyunfeng <br>
	 * @version 1.1.2 
	 */
	public final static int SITE_PRICE_VALID=0;
	
	/**
	 * 网站不在有效（不是beidou站点全库中）
	 */
	public final static int UNION_SITE_INVALID= 0;
	
	
	/**
	 * 网站热度级别
	 * added by zhuqian
	 * @version 1.1.3
	 */
	public final static int CMP_LEVEL_NONE = 0; 		//无网站热度（全网推广）
	public final static int CMP_LEVEL_LOW = 1;   		//竞争较少
	public final static int CMP_LEVEL_MODERATE = 2;		//竞争比较缓和
	public final static int CMP_LEVEL_MEDIAN = 3;		//竞争度一般
	public final static int CMP_LEVEL_HIGH = 4;			//竞争比较激烈
	public final static int CMP_LEVEL_HEATED = 5;		//竞争非常激烈
	/**
	 * 网站热度级别的文章描述
	 * added by zhuqian
	 * @version 1.1.3
	 */
	public final static String CMP_NAME_NONE = ""; 					//无网站热度（全网推广）
	public final static String CMP_NAME_LOW = "竞争较少";   		//竞争较少
	public final static String CMP_NAME_MODERATE = "竞争比较缓和";		//竞争比较缓和
	public final static String CMP_NAME_MEDIAN = "竞争度一般";		//竞争度一般
	public final static String CMP_NAME_HIGH = "竞争比较激烈";			//竞争比较激烈
	public final static String CMP_NAME_HEATED = "竞争非常激烈";		//竞争非常激烈
	/**
	 * 网站热度的默认显示长度，[0,100]间的整数
	 * added by zhuqian
	 * @version 1.1.3
	 */
	public final static int CMP_SIZE_NONE = 0; 			//无网站热度（全网推广）
	public final static int CMP_SIZE_LOW = 20;   		//竞争较少
	public final static int CMP_SIZE_MODERATE = 40;		//竞争比较缓和
	public final static int CMP_SIZE_MEDIAN = 60;		//竞争度一般
	public final static int CMP_SIZE_HIGH = 80;			//竞争比较激烈
	public final static int CMP_SIZE_HEATED = 95;		//竞争非常激烈
	
	public static final int TRADE_NORMAL = 0;	//常规行业分类
	public static final int TRADE_OTHER = 1;	//站点为其他的标识
	public static final int TRADE_WHITELIST = 2; //白名单行业分类
	
	public static int getDefaultCmpSizeByLevel(Integer level){
		if(level == null){
			return CMP_SIZE_NONE;
		}
		switch(level){
			case CMP_LEVEL_LOW:
				return CMP_SIZE_LOW;
			case CMP_LEVEL_MODERATE:
				return CMP_SIZE_MODERATE;
			case CMP_LEVEL_MEDIAN:
				return CMP_SIZE_MEDIAN;
			case CMP_LEVEL_HIGH:
				return CMP_SIZE_HIGH;
			case CMP_LEVEL_HEATED:
				return CMP_SIZE_HEATED;
			default:
				return CMP_SIZE_NONE;
		}
	}
	
	public static String getCmpNameByLevel(Integer level){
		if(level == null){
			return CMP_NAME_NONE;
		}
		switch(level){
			case CMP_LEVEL_LOW:
				return CMP_NAME_LOW;
			case CMP_LEVEL_MODERATE:
				return CMP_NAME_MODERATE;
			case CMP_LEVEL_MEDIAN:
				return CMP_NAME_MEDIAN;
			case CMP_LEVEL_HIGH:
				return CMP_NAME_HIGH;
			case CMP_LEVEL_HEATED:
				return CMP_NAME_HEATED;
			default:
				return CMP_NAME_NONE;
		}
	}

	public static int getSITEINFO_Q1_THRESHOLD() {
		return SITEINFO_Q1_THRESHOLD;
	}

	public static void setSITEINFO_Q1_THRESHOLD(int siteinfo_q1_threshold) {
		SITEINFO_Q1_THRESHOLD = siteinfo_q1_threshold;
	}

	public static int getSITEINFO_Q2_THRESHOLD() {
		return SITEINFO_Q2_THRESHOLD;
	}

	public static void setSITEINFO_Q2_THRESHOLD(int siteinfo_q2_threshold) {
		SITEINFO_Q2_THRESHOLD = siteinfo_q2_threshold;
	}

	public static int getSITE_SELECTED_MAX_NUM() {
		return SITE_SELECTED_MAX_NUM;
	}

	public static void setSITE_SELECTED_MAX_NUM(int site_selected_max_num) {
		SITE_SELECTED_MAX_NUM = site_selected_max_num;
	}	
	
	
	public static class Keyword {
		
		public static int MAX_COUNT_PER_GROUP = 5000;
		
		public static int MAX_COUNT_ONCE_COMMIT = 1000;
		
		//主题词状态
		public static int STATE_NORMAL = 0; 	//正常
		public static int STATE_PAUSE = 1;		//暂停
		public static int STATE_REFUSED = 2;	//审核拒绝

		public static final String[] STATE_NAMES = new String[]{"正常","暂停","审核拒绝"};
		//主题词匹配模式
		public static int MATCH_TYPE_NO_DEF = 0; 	//无定义
		public static int MATCH_TYPE_EXACT = 1; 	//精确匹配
		public static int MATCH_TYPE_PHRASE = 2; 	//短语匹配
		public static int MATCH_TYPE_VAGUE = 3; 	//广泛匹配
		public static int MATCH_TYPE_BEIDOU_DEFAULT = MATCH_TYPE_NO_DEF; 	//默认为无定义		
	}
	
	public static final int SUMMARY_DESC_SIZE = 2;  //推广组综述信息显示数据个数
	
	//推广组配置限制：GroupConfigValidator中需要用到的常量参数
	public static int GROUP_CONFIG_PRICE_MIN = 5;			//点击价格最小值，单位：分
	public static int GROUP_CONFIG_PRICE_MAX = 99999;		//点击价格最大值，单位：分
	public static int GROUP_CONFIG_TARGETURL_MAX = 1024;
	public static int GROUP_CONFIG_FILTER_SITE_MAX = 500;
	
	//网盟推广复制工具，每日成功复制的推广组个数上限
	public static int GROUP_MAX_CLONE_NUMBER = 30;
	
}