package com.baidu.beidou.cprounit.mcdriver.bean.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.bo.request.RequestImageMaterial2;



/**
 * 向dr-mc修改图片/Flash物料时所用结构体
 * 以dr-mc提供的url形式提交
 * 
 * @author yanjie
 *
 */
public class UpdateImageRequestBean2 extends UpdateRequestBeanBase {
	/*物料信息*/
	private RequestImageMaterial2 value;

	public UpdateImageRequestBean2(RequestImageMaterial2 value, int oldtype){
		super.setMcid(value.getMcid());
		super.setOldtype(oldtype);
		super.setNewtype(CproUnitConstant.DRMC_MATTYPE_IMAGE);
		
		this.value = value;
	}
	
	public RequestImageMaterial2 getValue() {
		return value;
	}

	public void setValue(RequestImageMaterial2 value) {
		this.value = value;
	}
}
