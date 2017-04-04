package com.baidu.beidou.cprounit.constant;

/**
 * 需要利用配置文件进行控制的配置项
 * 用spring加载
 * 
 * @author hejinggen
 *
 */
public class CproUnitConfig {
	/**
	 * drmc物料存储地址前缀
	 */
	public static String DRMC_MATPREFIX = null;
	
	/**
	 * 调用drmc批量接口时的个数限制
	 * @author yanjie
	 * @version 1.2.5
	 */
	public static int DRMC_BATCH_NUM;
	
	/**
	 * 同一个推广组下的最多推广单元数
	 */
	public static int MAX_UNIT_NUMBER =100;	

	public int getMAX_UNIT_NUMBER() {
		return MAX_UNIT_NUMBER;
	}

	public void setMAX_UNIT_NUMBER(int max_unit_number) {
		MAX_UNIT_NUMBER = max_unit_number;
	}

	public String getDRMC_MATPREFIX() {
		return CproUnitConfig.DRMC_MATPREFIX;
	}

	public void setDRMC_MATPREFIX(String drmc_matprefix) {
		CproUnitConfig.DRMC_MATPREFIX = drmc_matprefix;
	}

	public int getDRMC_BATCH_NUM() {
		return CproUnitConfig.DRMC_BATCH_NUM;
	}

	public void setDRMC_BATCH_NUM(int drmc_batch_num) {
		CproUnitConfig.DRMC_BATCH_NUM = drmc_batch_num;
	}
	
}
