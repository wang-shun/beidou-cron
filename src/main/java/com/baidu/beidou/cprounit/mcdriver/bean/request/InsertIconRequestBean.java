package com.baidu.beidou.cprounit.mcdriver.bean.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.bo.request.RequestIconMaterial;



/**
 * 向dr-mc提交图标物料时所用结构体
 * 以字节数组形式提交
 * 
 * @author yanjie
 *
 */
public class InsertIconRequestBean extends InsertRequestBeanBase {
	/*物料信息*/
	private RequestIconMaterial value;

	public InsertIconRequestBean(RequestIconMaterial value){
		super.setType(CproUnitConstant.DRMC_MATTYPE_ICON);
		
		this.value = value;
	}
	
	public RequestIconMaterial getValue() {
		return value;
	}

	public void setValue(RequestIconMaterial value) {
		this.value = value;
	}
}
