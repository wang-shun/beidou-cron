package com.baidu.beidou.accountmove.table;

/**
 * hold table column ,the order is import
 * the order userd to control the db sql
 * @author work
 *
 */
public class TableSchemaInfo {

	public static final String[] cproplan = { "planid", "userid", "planname",
			"consumetype", "planstate", "budget", "budgetover", "sqltype",
			"startdate", "hasenddate", "enddate", "mondayscheme",
			"tuesdayscheme", "wednesdayscheme", "thursdayscheme",
			"fridayscheme", "saturdayscheme", "sundayscheme", "addtime",
			"modtime", "adduserid", "moduserid", "promotion_type",
			"wireless_bid_ratio", "device_opera_system", "plan_type" };

	public static final String[] cprogroup = { "groupid", "planid", "userid",
			"groupname", "grouptype", "groupstate", "addtime", "modtime",
			"adduserid", "moduserid", "targettype", "qtalivedays", "is_smart",
			"activity_state", "similar_flag"};

	public static final String[] cprogroupinfo = { "groupid", "userid",
			"price", "isallregion", "reglist", "regsum", "isallsite",
			"sitetradelist", "sitelist", "sitesum", "cmplevel", "genderinfo",
			"medialevel", "attach_mask", "attach_ubmc_id",
			"attach_ubmc_version_id" };

	public static final String[] cprokeyword = { "keywordid", "groupid",
			"planid", "userid", "keyword", "wordid", "state", "matchtype",
			"refuseReason", "audittime", "adduserid", "addtime", "moduserid",
			"modtime", "patterntype" };

	public static final String[] cprogroupit = { "itid", "groupid", "planid",
			"userid", "iid", "type", "addtime", "adduser" };

	public static final String[] cprogroupvt = { "vtid", "groupid", "planid",
			"userid", "targettype", "pid", "relatetype", "addtime",
			"modtime", "adduser", "moduser" };

	public static final String[] group_pack = { "gpid", "groupid", "planid",
			"userid", "pack_id", "pack_type", "price", "addtime", "adduser",
			"modtime", "moduser" };

	public static final String[] group_atright_info = { "groupid", "planid",
			"userid", "business_keywords", "refer_url" };

	public static final String[] cprogroup_atright = { "atid", "groupid",
			"planid", "userid", "cluster_ids", "crowd_weight" };

	public static final String[] cprogroup_atleft = { "atleftid", "groupid",
			"planid", "userid", "atwordid", "atword", "category", "atwordtype",
			"adduserid", "addtime", "moduserid", "modtime" };

	public static final String[] cprounitstate = { "id", "gid", "pid", "uid",
			"state", "submitType", "submiter", "changer", "subTime", "chaTime",
			"helpstatus", "audittime" };

    public static final String[] cprounitmater = { "id", "userid", "wid", "wuliaoType", "keyword", "iftitle", "title",
            "description1", "description2", "showUrl", "targetUrl", "fileSrc", "player", "height", "width", "syncflag",
            "adtradeid", "fwid", "interactive", "wireless_show_url", "wireless_target_url", "ubmcsyncflag", "mcId",
            "mcVersionId", "file_src_md5", "new_adtradeid", "confidence_level", "beauty_level", "cheat_level",
            "vulgar_level", "drmcsyncflag", "wireless_url_modified", "am_template_id", "is_smart", "template_id",
            "shadow_mc_version_id", "tag_mask", "tpl_conf_id", "unit_source" };

	public static final String[] online_unit = { "id", "groupid", "planid", "userid", "wuliao_type","title", "description1",
		"description2", "show_url", "target_url", "player", "height",
		"width", "adtradeid", "interactive", "wireless_show_url", "wireless_target_url", "mc_id",
		"mc_version_id", "file_src_md5", "new_adtradeid", "confidence_level",
		"beauty_level", "cheat_level", "vulgar_level", "am_template_id",
		"is_smart", "template_id", "modtime", "tag_mask", "tpl_conf_id" };
	
	public static final String[] uniticon = { "id", "userid", "unitId", "iconId" };
	
	public static final String[] attach_info = { "id", "groupid", "planid", "userid", "attach_id","attach_type", "attach_content",
		"state", "addtime", "modtime", "adduser", "moduser"  };
	
	public static final String[] smartidea_product_filter = { "id", "groupid", "planid", "userid", "is_empty","filter_condition", "adduser",
		"addtime", "moduser", "modtime"  };
	
	public static final String[] smartidea_template_element_conf = { "id", "groupid", "planid", "userid", "template_id","property_mapping", "adduser",
		"addtime", "moduser", "modtime"  };
	
	public static final String[] smartidea_template_element_url = { "id", "groupid", "planid", "userid", "template_id","template_element_id", "target_url",
		"wireless_target_url", "addtime","adduser", "moduser", "modtime"  };
	
	public static final String[] smartidea_keyword = { "keywordid", "groupid", "planid", "userid", "keyword","wordid", "word_type",
		"addtime", "adduser", "moduser", "modtime"  };
	
	public static final String[] grouptradeprice = { "id", "groupid", "planid", "userid", "tradeid", "price"  };
	
	public static final String[] group_interest_price = { "gip_id", "groupid",
			"planid", "userid", "iid", "type", "price", "addtime", "adduser",
			"modtime", "moduser" };
	
	public static final String[] groupsiteprice = { "id", "groupid",
		"planid", "userid", "siteid", "siteurl", "price", "targeturl" };
	
	public static final String[] word_exclude = { "keywordid", "groupid",
		"planid", "userid", "keyword", "wordid", "patterntype", "addtime", "adduser",
		"modtime", "moduser" };
	
	public static final String[] word_pack_exclude = { "wpe_id", "groupid",
		"planid", "userid", "pack_id", "addtime", "adduser", "modtime", "moduser" };
	
	public static final String[] groupmultichainfilter = { "id", "groupid",
		"planid", "userid", "filter_type", "content", "addtime", "modtime"};
	
	public static final String[] cprogroupit_exclude = { "itid", "groupid",
			"planid", "userid", "iid", "type", "addtime", "adduser"};
	
	public static final String[] groupipfilter = { "id", "groupid", "ip", "userid"};
	
	public static final String[] app_exclude = { "id", "groupid", "planid", "userid", "app_sid", "addtime", "adduser", "modtime", "moduser"};
	
	public static final String[] groupsitefilter = { "id", "groupid", "planid", "userid", "site" };
	
    public static final String[] word_pack = { "pack_id", "ref_pack_id", "userid", "name", "targettype", "alivedays",
            "addtime", "adduser", "modtime", "moduser" };
	
	public static final String[] word_pack_keyword = { "wpk_id", "pack_id", "keywordid", "keyword", "wordid", "patterntype", "userid", "dasstatus", "addtime", "adduser", "modtime", "moduser" };
	
	public static final String[] custominterest = { "cid", "name", "expression", "userid", "addtime", "adduser", "modtime", "moduser" };
	
    public static final String[] vtcode = { "jsid", "name", "sign", "userid", "addtime", "modtime", "adduser",
            "moduser", "is_all_site" };

    public static final String[] vturl = { "id", "pid", "url", "userid" };

    public static final String[] vtpeople = { "id", "pid", "name", "jsid", "stat", "alivedays", "cookienum", "userid",
            "activetime", "addtime", "modtime", "adduser", "moduser", "type", "hpid", "hsiteid" };

	// xdb
	public static final String[] useruploadicons = { "id", "userid", "mcId", "wid", "fileSrc", "hight", "width", "addtime", "ubmcsyncflag" };
	public static final String[] atright_user = { "userid","addtime" };
	
	// table name static
	public static final String PLAN = "cproplan";
	public static final String GROUP = "cprogroup";
	public static final String GROUPINFO = "cprogroupinfo";
	public static final String KEYWORD = "cprokeyword";
	public static final String GROUPIT = "cprogroupit";
	public static final String GROUPVT = "cprogroupvt";
	public static final String GROUPPACK = "group_pack";
	public static final String GROUP_ATRIGHT_INFO = "group_atright_info";
	public static final String CPROGROUP_ATRIGHT = "cprogroup_atright";
	public static final String CPROGROUP_ATLEFT = "cprogroupatleft";
	public static final String CPROUNIT_STATE = "cprounitstate";
	public static final String CPROUNIT_MATER = "cprounitmater";
	public static final String ONLINE_UNIT = "online_unit";
	public static final String UNIT_ICON = "uniticon";
	public static final String ATTACH_INFO = "attach_info";
	public static final String SMARTIDEA_PRODUCT_FILTER = "smartidea_product_filter";
	public static final String SMARTIDEA_TEMPLATE_ELEMENT_CONF = "smartidea_template_element_conf";
	public static final String SMARTIDEA_TEMPLATE_ELEMENT_URL = "smartidea_template_element_url";
	public static final String SMARTIDEA_KEYWORD = "smartidea_keyword";
	public static final String GROUPTRADEPRICE = "grouptradeprice";
	public static final String GROUP_INTEREST_PRICE = "group_interest_price";
	public static final String GROUPSITEPRICE = "groupsiteprice";
	public static final String WORD_EXCLUDE = "word_exclude";
	public static final String WORD_PACK_EXCLUDE = "word_pack_exclude";
	public static final String GROUPMULTICHAINFILTER = "groupmultichainfilter";
	public static final String CPROGROUPIT_EXCLUDE = "cprogroupit_exclude";
	public static final String GROUPIPFILTER = "groupipfilter";
	public static final String APP_EXCLUDE = "app_exclude";
	public static final String GROUPSITEFILTER = "groupsitefilter";
	public static final String WORD_PACK = "word_pack";
	public static final String WORD_PACK_KEYWORD = "word_pack_keyword";
	public static final String CUSTOMINTEREST = "custominterest";
	public static final String VTCODE = "vtcode";
	public static final String VTURL = "vturl";
	public static final String VTPEOPLE = "vtpeople";
	
	//XDB
	public static final String USERUPLOADICONS = "useruploadicons";
	public static final String ATRIGHT_USER = "atright_user";
	
}
