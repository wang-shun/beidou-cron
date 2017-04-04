package com.baidu.beidou.cprounit.mcdriver.bean.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.bo.request.RequestLiteralWithIconMaterial2;


/**
 * 向dr-mc提交图文物料时所用结构体
 * 
 * @author yanjie
 *
 */
public class InsertLiteralWithIconRequestBean2 extends InsertRequestBeanBase {
	/*物料信息*/
	private RequestLiteralWithIconMaterial2 value;

	public InsertLiteralWithIconRequestBean2(RequestLiteralWithIconMaterial2 value){
		super.setType(CproUnitConstant.DRMC_MATTYPE_LITERAL_WITH_ICON);
		
		this.value = value;
	}
	
	public RequestLiteralWithIconMaterial2 getValue() {
		return value;
	}

	public void setValue(RequestLiteralWithIconMaterial2 value) {
		this.value = value;
	}
}
