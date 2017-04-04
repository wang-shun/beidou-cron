package com.baidu.beidou.cprounit.mcdriver.bean.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.bo.request.RequestImageMaterial2;



/**
 * 向dr-mc提交图片/Flash物料时所用结构体
 * 以dr-mc提供的url形式提交
 * 
 * @author yanjie
 *
 */
public class InsertImageRequestBean2 extends InsertRequestBeanBase {
	/*物料信息*/
	private RequestImageMaterial2 value;

	public InsertImageRequestBean2(RequestImageMaterial2 value){
		super.setType(CproUnitConstant.DRMC_MATTYPE_IMAGE);
		
		this.value = value;
	}

	public RequestImageMaterial2 getValue() {
		return value;
	}

	public void setValue(RequestImageMaterial2 value) {
		this.value = value;
	}
}
