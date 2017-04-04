package com.baidu.beidou.cprounit.mcdriver.bean.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.bo.request.RequestLiteralMaterial;


/**
 * 向dr-mc修改文字物料时所用结构体
 * 
 * @author yanjie
 *
 */
public class UpdateLiteralRequestBean extends UpdateRequestBeanBase {
	/*物料信息*/
	private RequestLiteralMaterial value;

	public UpdateLiteralRequestBean(RequestLiteralMaterial value, int oldtype){
		super.setMcid(value.getMcid());
		super.setOldtype(oldtype);
		super.setNewtype(CproUnitConstant.DRMC_MATTYPE_LITERAL);
		
		this.value = value;
	}
	
	public RequestLiteralMaterial getValue() {
		return value;
	}

	public void setValue(RequestLiteralMaterial value) {
		this.value = value;
	}
}
