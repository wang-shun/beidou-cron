/**
 * 2009-7-2
 * zengyunfeng
 * @version 1.2.0
 */
package com.baidu.beidou.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 通过spring管理的全局常量对象
 */
public class BeidouConfig {
	private static final Log LOG = LogFactory.getLog(BeidouConfig.class);

	/**
	 * beidou缓存地址
	 * 挂接用户中心 
	 * @author zengyunfeng
	 */
	public static String MASTER_CACHE_SERVER = null;
	
	/**
	 * beidou缓存地址
	 * 挂接用户中心 
	 * @author zengyunfeng
	 */
	public static String SLAVE_CACHE_SERVER = null;
	
	/**
	 * 单位为秒
	 */
	public static int CACHE_OPERATION_TIMEOUT = 1;
	/**
	 * 数据缓冲的大小:1M
	 */
	public static int CACHE_READ_BUFFER_SIZE = 1024*1024;
	/**
	 * 读写队列
	 */
	public static int CACHE_OP_QUEUE_LEN = 16384;
	
	/**
	 * AKA的server配置
	 */
	public static String AKA_SERVIER = null;
	
	/**
	 * 文字创意的审核规则
	 */
	public static int AKA_LITERAL_CLIENT = 6;
	
	/**
	 * 图片创意的审核规则：只审核点击url
	 */
	public static int AKA_PICTURE_CLIENT = 7;
	/**
	 * 主题词创意的审核规则
	 */
	public static int AKA_KW_CLIENT = 8;
	/**
	 * beidou黑名单的词表类型
	 */
	public static int AKA_LIST_TYPE = 4;
	
	/**
	 * SHIFEN的appid,用于获取一级管理员岗位下管辖的用户
	 */
	public static int UC_POST_APPID;
	
	/**
	 * beidou的grouptype,用于获取二级管理员岗位下管辖的用户
	 */
	public static String UC_BEIDOU_GROUPTYPE;
	
	/**
	 * shifen的分组,用于获取非网盟专员的二级管理员岗位下管辖的用户
	 */
	public static String UC_SHIFEN_GROUPTYPE;
	
	/**
	 * shifen的分组，用于获取管辖关系中的父用户 
	 */
	public static String UC_SHIFEN_GETLEADER_GROUPTYPE;
	
	/**
	 * @param master_cache_server the mASTER_CACHE_SERVER to set
	 */
	public void setMASTER_CACHE_SERVER(String master_cache_server) {
		MASTER_CACHE_SERVER = master_cache_server;
	}


	/**
	 * @param slave_cache_server the sLAVE_CACHE_SERVER to set
	 */
	public void setSLAVE_CACHE_SERVER(String slave_cache_server) {
		SLAVE_CACHE_SERVER = slave_cache_server;
	}


	/**
	 * @param cache_operation_timeout the cACHE_OPERATION_TIMEOUT to set
	 */
	public void setCACHE_OPERATION_TIMEOUT(int cache_operation_timeout) {
		CACHE_OPERATION_TIMEOUT = cache_operation_timeout;
	}


	/**
	 * @param cache_read_buffer_size the cACHE_READ_BUFFER_SIZE to set
	 */
	public void setCACHE_READ_BUFFER_SIZE(int cache_read_buffer_size) {
		CACHE_READ_BUFFER_SIZE = cache_read_buffer_size;
	}


	/**
	 * @param cache_op_queue_len the cACHE_OP_QUEUE_LEN to set
	 */
	public void setCACHE_OP_QUEUE_LEN(int cache_op_queue_len) {
		CACHE_OP_QUEUE_LEN = cache_op_queue_len;
	}


	/**
	 * @param uc_post_appid the uC_POST_APPID to set
	 */
	public void setUC_POST_APPID(int uc_post_appid) {
		UC_POST_APPID = uc_post_appid;
	}


	/**
	 * @param uc_beidou_grouptype the uC_BEIDOU_GROUPTYPE to set
	 */
	public void setUC_BEIDOU_GROUPTYPE(String uc_beidou_grouptype) {
		UC_BEIDOU_GROUPTYPE = uc_beidou_grouptype;
	}


	public String getUC_SHIFEN_GROUPTYPE() {
		return UC_SHIFEN_GROUPTYPE;
	}


	public void setUC_SHIFEN_GROUPTYPE(String uCSHIFENGROUPTYPE) {
		UC_SHIFEN_GROUPTYPE = uCSHIFENGROUPTYPE;
	}


	public String getUC_SHIFEN_GETLEADER_GROUPTYPE() {
		return UC_SHIFEN_GETLEADER_GROUPTYPE;
	}


	public void setUC_SHIFEN_GETLEADER_GROUPTYPE(String uCSHIFENGETLEADERGROUPTYPE) {
		UC_SHIFEN_GETLEADER_GROUPTYPE = uCSHIFENGETLEADERGROUPTYPE;
	}
	
	/**
	 * @param akaServier the akaServier to set
	 */
	public void setAkaServier(String akaServier) {
		BeidouConfig.AKA_SERVIER = akaServier;
	}

	public void setAKA_LITERAL_CLIENT(int aKALITERALCLIENT) {
		AKA_LITERAL_CLIENT = aKALITERALCLIENT;
	}

	public void setAKA_PICTURE_CLIENT(int aKAPICTURECLIENT) {
		AKA_PICTURE_CLIENT = aKAPICTURECLIENT;
	}

	public void setAKA_KW_CLIENT(int aKAKWCLIENT) {
		AKA_KW_CLIENT = aKAKWCLIENT;
	}

	public void setAKA_LIST_TYPE(int aKALISTTYPE) {
		AKA_LIST_TYPE = aKALISTTYPE;
	}
}
