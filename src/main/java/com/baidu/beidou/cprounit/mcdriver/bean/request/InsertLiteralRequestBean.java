package com.baidu.beidou.cprounit.mcdriver.bean.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.bo.request.RequestLiteralMaterial;


/**
 * 向dr-mc提交文字物料时所用结构体
 * 
 * @author yanjie
 *
 */
public class InsertLiteralRequestBean extends InsertRequestBeanBase {
	/*物料信息*/
	private RequestLiteralMaterial value;

	public InsertLiteralRequestBean(RequestLiteralMaterial value){
		super.setType(CproUnitConstant.DRMC_MATTYPE_LITERAL);
		
		this.value = value;
	}
	
	public RequestLiteralMaterial getValue() {
		return value;
	}

	public void setValue(RequestLiteralMaterial value) {
		this.value = value;
	}
}
