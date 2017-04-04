package com.baidu.beidou.cprounit.mcdriver.bean.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.bo.request.RequestLiteralWithIconMaterial;


/**
 * 向dr-mc提交图文物料时所用结构体
 * 
 * @author yanjie
 *
 */
public class InsertLiteralWithIconRequestBean extends InsertRequestBeanBase {
	/*物料信息*/
	private RequestLiteralWithIconMaterial value;

	public InsertLiteralWithIconRequestBean(RequestLiteralWithIconMaterial value){
		super.setType(CproUnitConstant.DRMC_MATTYPE_LITERAL_WITH_ICON);
		
		this.value = value;
	}
	
	public RequestLiteralWithIconMaterial getValue() {
		return value;
	}

	public void setValue(RequestLiteralWithIconMaterial value) {
		this.value = value;
	}
}
