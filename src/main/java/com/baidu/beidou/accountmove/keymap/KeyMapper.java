package com.baidu.beidou.accountmove.keymap;

public interface KeyMapper {

	/**
	 * save the key map relation, 
	 * this mapping relation will saved in map and persistent to file.
	 * the file will be at the path:projectPath/data/userid/idMap/keyName
	 * 
	 * @param keyName keyName
	 * @param oldKey oldKey
	 * @param newKey newKey
	 * @return result result
	 */
	boolean addKeyMap(String keyName, String oldKey, String newKey, int userId );
	
	/**
	 * query new key from mapping relation, 
	 * if the key map is null, will reload map relation from file
	 * 
	 * @param keyName keyName
	 * @param oldKey oldKey
	 * @return result result
	 */
	String getMappedKey(String keyName, String oldKey, int userId );
	
	
	static final String USERID = "userid";
	static final String PLANID = "planid";
	static final String GROUPID = "groupid";
	static final String KEYWORDID = "keywordid";
	static final String ITID = "itid";
	static final String VTID = "vtid";
	static final String GPID = "gpid";
	static final String PACKID = "pack_id";
	static final String ATID = "atid";
	static final String AT_LEFTID = "atleftid";
	static final String UNITID = "unitid";
	static final String ONLINE_UNITID = "onlineunitid";
	
	static final String UNIT_ICONID = "iconid";
	static final String ATTACH_INFO_ID = "attach_info_id";
	static final String SMARTIDEA_PRODUCT_FILTER_ID = "smartidea_product_filter_id";
	static final String SMARTIDEA_TEMPLATE_ELEMENT_CONF_ID = "smartidea_template_element_conf_id";
	static final String SMARTIDEA_TEMPLATE_ELEMENT_URL_ID = "smartidea_template_element_url_id";
	static final String SMARTIDEA_KEYWORD_ID = "smartidea_keyword_id";
	static final String GROUPTRADEPRICE_ID = "grouptradeprice_id";
	static final String GROUP_INTEREST_PRICE_ID = "group_interest_price_id";
	static final String GROUPSITEPRICE_ID = "groupsiteprice_id";
	static final String WORD_EXCLUDE_ID = "word_exclude_id";
	static final String WORD_PACK_EXCLUDE_ID = "word_pack_exclude_id";
	static final String GROUPMULTICHAINFILTER_ID = "groupmultichainfilter_id";
	static final String CPROGROUPIT_EXCLUDE_ID = "cprogroupit_exclude_id";
	static final String GROUPIPFILTER_ID = "groupipfilter_id";
	static final String APP_EXCLUDE_ID = "app_exclude_id";
	static final String GROUPSITEFILTER_ID = "groupsitefilter_id";
	static final String WORD_PACK_KEYWORD_ID = "word_pack_keyword_id";
	static final String CUSTOMINTEREST_ID = "custominterest_id";
	static final String JSID = "jsid";
	static final String VTURL_ID = "jsid";
	static final String VTPEOPLE_ID = "pid";
	
	static final String USERUPLOADICONS_ID = "useruploadicons_id";
	static final String ATRIGHT_USER_ID = "atright_user_id";
	
}
