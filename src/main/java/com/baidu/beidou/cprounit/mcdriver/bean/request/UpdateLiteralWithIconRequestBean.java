package com.baidu.beidou.cprounit.mcdriver.bean.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.bo.request.RequestLiteralWithIconMaterial;


/**
 * 向dr-mc修改图文物料时所用结构体
 * 
 * @author hejinggen
 *
 */
public class UpdateLiteralWithIconRequestBean extends UpdateRequestBeanBase {
	/*物料信息*/
	private RequestLiteralWithIconMaterial value;

	public UpdateLiteralWithIconRequestBean(RequestLiteralWithIconMaterial value, int oldtype){
		super.setMcid(value.getMcid());
		super.setOldtype(oldtype);
		super.setNewtype(CproUnitConstant.DRMC_MATTYPE_LITERAL_WITH_ICON);
		
		this.value = value;
	}
	
	public RequestLiteralWithIconMaterial getValue() {
		return value;
	}

	public void setValue(RequestLiteralWithIconMaterial value) {
		this.value = value;
	}
}
