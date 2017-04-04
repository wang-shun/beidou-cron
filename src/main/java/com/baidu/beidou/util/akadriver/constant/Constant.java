package com.baidu.beidou.util.akadriver.constant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.baidu.beidou.util.akadriver.bo.AkaBeidouResult;

/**
 * ClassName:Constant
 * Function: aka审核接口的相关常量，添加竞品/侵权审核
 *
 * @author   <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version  
 * @since    TODO
 * @Date	 2010	Sep 6, 2010		8:00:02 PM
 *
 * @see 	 
 */
public abstract class Constant {
	public static final String configFileName = "aka";
	public static Properties CONFIG_MEM_POP;
	
	public static final String WILDCARD = "[Wildcard]";
	
	public static final int STATUS_OK = 0;
	
	/**
	 * CHECK用户黑名单: 0:不检查； 1:检查
	 */
	public static final int CHECK_USER_FILTER = 0;
	/**
	 *	preproc_flag	含义
	 *	0	不预处理
	 *	0x0001	去除不可见字符
	 *	0x0002	去除两边空格，将一个或多个连续空格转换为一个空格
	 *	0x0004	全角转半角
	 *	0x0008	繁体转简体
	 *	0x0010	去除html标签
	 *	0x0020	url归一化
	 */
	public static final int PREPROC_OFF = 0;
	public static final int PREPROC_BLANK = 0x0001;
	public static final int PREPROC_TRIM = 0x0002;
	public static final int PREPROC_SBC2DBC = 0x0004;
	public static final int PREPROC_SIMPLIFIED = 0x0008;
	public static final int PREPROC_HTMLFILT = 0x0010;
	public static final int PREPROC_URLUNIFY = 0x0020;
	
	/**
	 * @author genglei01
	 * @2010-09-06, cpweb-174
 	 * Audit_flag	含义
	 *	0		不审核
	 *	0x0001	特殊字符审核
	 *	0x0002	黑名单审核
	 *	0x0004	注册商标审核
	 *	0x0008	标题+描述触犯黑名单审核
	 *	0x0010	标题+描述触犯注册商标
	 *	0x0020	描述中没有关键词
	 *	0x0040	标题+描述未包含关键词
	 *	0x0080	非法前缀审核
	 *	0x0100	非法后缀审核
	 *	0x0200	限制词1
	 *	0x0400	限制词2
	 *	0x0800	竞品/侵权审核
	 *	0x1000	标题+描述触犯竞品/侵权审核
	 */
	public static final int AUDIT_OFF = 0;
	public static final int AUDIT_SPECIAL_CHAR = 0x0001;
	public static final int AUDIT_BLACK_WORD = 0x0002;
	public static final int AUDIT_BRAND_WORD = 0x0004;
	public static final int AUDIT_BLACK_IDEA = 0x0008;
	public static final int AUDIT_BRAND_IDEA = 0x0010;
	public static final int AUDIT_NOKEYWORD_TITLE = 0x0020;
	public static final int AUDIT_NOKEYWORD_IDEA = 0x0040;
	public static final int AUDIT_ILLEGAL_PREFIX = 0x0080;
	public static final int AUDIT_ILLEGAL_SUFFIX = 0x0100;
	public static final int AUDIT_RESTRICT_WORD1 = 0x0200;
	public static final int AUDIT_RESTRICT_WORD2 = 0x0400;
	public static final int AUDIT_COMPETITIVE_WORD = 0x0800;
	public static final int AUDIT_COMPETITIVE_IDEA = 0x1000;
	
	
	/**
	 *	取值	含义
	 *	0	审核通过
	 *	1	触犯规则，审核不通过
	 */
	public static final long RESULT_LEVEL_PASS = 0L;
	public static final long RESULT_LEVEL_FAIL= 1L;
	
	/**
	 * 词表类型
	 * 2 普通词表 64 注册商标词表
	 */
	public static final int LIST_TYPE_COMMON = 2;
	public static final int LIST_TYPE_BRAND = 64;
	
	
	
	//-----------------------以下定义各字段可以支持的审核位-------------------
	
	/**
	 * 关键词支持的审核单位
	 */
	public static Map<Integer, AKA_RES_CODE> KEYWORD_AUDIT = new LinkedHashMap<Integer, AKA_RES_CODE>();
	static {
		KEYWORD_AUDIT.put(AUDIT_SPECIAL_CHAR, AKA_RES_CODE.KEYSC);
		KEYWORD_AUDIT.put(AUDIT_BLACK_WORD, AKA_RES_CODE.KEYI);
		KEYWORD_AUDIT.put(AUDIT_BRAND_WORD, AKA_RES_CODE.KEYM);
		KEYWORD_AUDIT.put(AUDIT_COMPETITIVE_WORD, AKA_RES_CODE.KEYC);
		//-------关于独立网盟用户的还是要问俊杰----------------
	}
	/**
	 * 标题支持的审核单位
	 */
	public static Map<Integer, AKA_RES_CODE> TITLE_AUDIT = new LinkedHashMap<Integer, AKA_RES_CODE>();
	static {
		TITLE_AUDIT.put(AUDIT_BLACK_WORD, AKA_RES_CODE.TITI);
		TITLE_AUDIT.put(AUDIT_BRAND_WORD, AKA_RES_CODE.TITM);
		TITLE_AUDIT.put(AUDIT_COMPETITIVE_WORD, AKA_RES_CODE.TITC);
	}
	
	/**
	 * 描述1支持的审核单位
	 */
	public static Map<Integer, AKA_RES_CODE> DESC1_AUDIT = new LinkedHashMap<Integer, AKA_RES_CODE>();
	static {
		DESC1_AUDIT.put(AUDIT_BLACK_WORD, AKA_RES_CODE.DESC1I);
		DESC1_AUDIT.put(AUDIT_BRAND_WORD, AKA_RES_CODE.DESC1M);
		DESC1_AUDIT.put(AUDIT_COMPETITIVE_WORD, AKA_RES_CODE.DESC1C);
	}
	/**
	 * 描述2支持的审核单位
	 */
	public static Map<Integer, AKA_RES_CODE> DESC2_AUDIT = new LinkedHashMap<Integer, AKA_RES_CODE>();
	static {
		DESC2_AUDIT.put(AUDIT_BLACK_WORD, AKA_RES_CODE.DESC2I);
		DESC2_AUDIT.put(AUDIT_BRAND_WORD, AKA_RES_CODE.DESC2M);
		DESC2_AUDIT.put(AUDIT_COMPETITIVE_WORD, AKA_RES_CODE.DESC2C);
	}
	/**
	 * 点击URL支持审核
	 */
	public static Map<Integer, AKA_RES_CODE> URL_AUDIT = new LinkedHashMap<Integer, AKA_RES_CODE>();
	static {
		URL_AUDIT.put(AUDIT_SPECIAL_CHAR, AKA_RES_CODE.URLSC);
		URL_AUDIT.put(AUDIT_ILLEGAL_SUFFIX, AKA_RES_CODE.URLEND);
		URL_AUDIT.put(AUDIT_ILLEGAL_PREFIX, AKA_RES_CODE.URLHEAD);
	}
	/**
	 * 显示URL支持审核位
	 */
	public static Map<Integer, AKA_RES_CODE> SHOWURL_AUDIT = new LinkedHashMap<Integer, AKA_RES_CODE>();
	static {
		SHOWURL_AUDIT.put(AUDIT_BLACK_WORD, AKA_RES_CODE.SHOWURLBLACK);
		SHOWURL_AUDIT.put(AUDIT_BRAND_WORD, AKA_RES_CODE.SHOWURLBLAND);
		SHOWURL_AUDIT.put(AUDIT_COMPETITIVE_WORD, AKA_RES_CODE.SHOWURLCOMPETITIVE);
	}
	
	/**
	 * 无线点击URL支持审核
	 */
	public static Map<Integer, AKA_RES_CODE> WIRELESS_TARGETURL_AUDIT = new LinkedHashMap<Integer, AKA_RES_CODE>();
	static {
		WIRELESS_TARGETURL_AUDIT.put(AUDIT_SPECIAL_CHAR, AKA_RES_CODE.WIRELESS_URLSC);
		WIRELESS_TARGETURL_AUDIT.put(AUDIT_ILLEGAL_SUFFIX, AKA_RES_CODE.WIRELESS_URLEND);
		WIRELESS_TARGETURL_AUDIT.put(AUDIT_ILLEGAL_PREFIX, AKA_RES_CODE.WIRELESS_URLHEAD);
	}
	/**
	 * 无线显示URL支持审核位
	 */
	public static Map<Integer, AKA_RES_CODE> WIRELESS_SHOWURL_AUDIT = new LinkedHashMap<Integer, AKA_RES_CODE>();
	static {
		WIRELESS_SHOWURL_AUDIT.put(AUDIT_BLACK_WORD, AKA_RES_CODE.WIRELESS_SHOWURLBLACK);
		WIRELESS_SHOWURL_AUDIT.put(AUDIT_BRAND_WORD, AKA_RES_CODE.WIRELESS_SHOWURLBLAND);
		WIRELESS_SHOWURL_AUDIT.put(AUDIT_COMPETITIVE_WORD, AKA_RES_CODE.WIRELESS_SHOWURLCOMPETITIVE);
	}
	
	/**
	 * 标题+描述支持审核位
	 */
	public static Map<Integer, AKA_RES_CODE> IDEA_AUDIT = new LinkedHashMap<Integer, AKA_RES_CODE>();
	static {
		IDEA_AUDIT.put(AUDIT_BLACK_IDEA, AKA_RES_CODE.CORI);
		IDEA_AUDIT.put(AUDIT_BRAND_IDEA, AKA_RES_CODE.CORM);
		IDEA_AUDIT.put(AUDIT_COMPETITIVE_IDEA, AKA_RES_CODE.CORC);
	}
	
	private static final String INFO_SEPARATE_TOKEN = "，";

	public static AkaBeidouResult buildResult(long flag, Map<Integer, AKA_RES_CODE> dict){
		AkaBeidouResult result = new AkaBeidouResult();
		long token = 0L;
		StringBuilder msg = new StringBuilder();
		int i =0;
		for(Map.Entry<Integer, AKA_RES_CODE> ele : dict.entrySet()){
			if((flag&ele.getKey()) == ele.getKey()){
				token = token | ele.getValue().getValue();
				if (++i > 1) {
					msg.append(INFO_SEPARATE_TOKEN);
				}
				msg.append(ele.getValue().getString());
			}
		}
		result.setToken(token);
		result.setMsg(msg.toString());
		return result;
	}
	
	// 为aka轮询增加黑名单审核
	public static final int BLACK_AUDIT_BIT = 1;
	public static List<Integer> BLACK_AUDIT = new ArrayList<Integer>();
	static {
		BLACK_AUDIT.add(AUDIT_BLACK_WORD);
		BLACK_AUDIT.add(AUDIT_BLACK_IDEA);
	}
	
	// 为aka轮询增加注册商标审核
	public static final int BRAND_AUDIT_BIT = 2;
	public static List<Integer> BRAND_AUDIT = new ArrayList<Integer>();
	static {
		BRAND_AUDIT.add(AUDIT_BRAND_WORD);
		BRAND_AUDIT.add(AUDIT_BRAND_IDEA);
	}
	
	// 为aka轮询增加竞品侵权审核
	public static final int COMPETITIVE_AUDIT_BIT = 4;
	public static List<Integer> COMPETITIVE_AUDIT = new ArrayList<Integer>();
	static {
		COMPETITIVE_AUDIT.add(AUDIT_COMPETITIVE_WORD);
		COMPETITIVE_AUDIT.add(AUDIT_COMPETITIVE_IDEA);
	}
	
	// 返回aka轮询的结果，优先级：黑名单>注册商标>竞品侵权
	public static AkaBeidouResult buildResultForPatrol(long flag){
		AkaBeidouResult result = new AkaBeidouResult();
		int patrolFlag = 0;

		// 判断是否触犯黑名单规则
		for (Integer auditKey : BLACK_AUDIT) {
			if ((flag & auditKey) == auditKey) {
				patrolFlag |= BLACK_AUDIT_BIT;
				result.setPatrolFlag(patrolFlag);
				return result;
			}
		}
		
		// 判断是否触犯注册商标规则
		for (Integer auditKey : BRAND_AUDIT) {
			if ((flag & auditKey) == auditKey) {
				patrolFlag |= BRAND_AUDIT_BIT;
				result.setPatrolFlag(patrolFlag);
				return result;
			}
		}
		
		// 判断是否触犯竞品侵权规则
		for (Integer auditKey : COMPETITIVE_AUDIT) {
			if ((flag & auditKey) == auditKey) {
				patrolFlag |= COMPETITIVE_AUDIT_BIT;
				result.setPatrolFlag(patrolFlag);
				return result;
			}
		}

		result.setPatrolFlag(patrolFlag);
		return result;
	}
}
