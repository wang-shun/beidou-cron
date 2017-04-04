package com.baidu.beidou.account.constant;

/**
 * 需要利用配置文件进行控制的配置项
 * 用spring加载
 * 
 * @author zhangp
 *
 */
public class AccountConfig {

	/**
	 * 财务中心给beidou分配的产品ID
	 */
	public static Integer  MFC_BEIDOU_PRODUCTID = 0;
	
	/**
	 * 财务中心给凤巢分配的产品ID
	 */
	public static Integer  MFC_FENGCHAO_PRODUCTID = 0;

	/**
	 * 财务中心操作人ID默认值：0
	 */
	public static final Integer MFC_OPUID_DEFAULT = 0;
	
	public Integer getMFC_BEIDOU_PRODUCTID() {
		return MFC_BEIDOU_PRODUCTID;
	}

	public void setMFC_BEIDOU_PRODUCTID(Integer mfc_beidou_productid) {
		MFC_BEIDOU_PRODUCTID = mfc_beidou_productid;
	}

	public Integer getMFC_FENGCHAO_PRODUCTID() {
		return MFC_FENGCHAO_PRODUCTID;
	}

	public void setMFC_FENGCHAO_PRODUCTID(Integer mfc_fengchao_productid) {
		MFC_FENGCHAO_PRODUCTID = mfc_fengchao_productid;
	}	
}