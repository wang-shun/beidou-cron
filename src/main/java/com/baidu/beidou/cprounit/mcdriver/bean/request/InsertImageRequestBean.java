package com.baidu.beidou.cprounit.mcdriver.bean.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.bo.request.RequestImageMaterial;



/**
 * 向dr-mc提交图片/Flash物料时所用结构体
 * 以字节数组形式提交
 * 
 * @author yanjie
 *
 */
public class InsertImageRequestBean extends InsertRequestBeanBase {
	/*物料信息*/
	private RequestImageMaterial value;

	public InsertImageRequestBean(RequestImageMaterial value){
		super.setType(CproUnitConstant.DRMC_MATTYPE_IMAGE);
		
		this.value = value;
	}
	
	public RequestImageMaterial getValue() {
		return value;
	}

	public void setValue(RequestImageMaterial value) {
		this.value = value;
	}
}
