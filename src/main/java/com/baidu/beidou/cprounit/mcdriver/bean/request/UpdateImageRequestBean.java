package com.baidu.beidou.cprounit.mcdriver.bean.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.bo.request.RequestImageMaterial;



/**
 * 向dr-mc修改图片/Flash物料时所用结构体
 * 以字节数组形式提交
 * 
 * @author yanjie
 *
 */
public class UpdateImageRequestBean extends UpdateRequestBeanBase {
	/*物料信息*/
	private RequestImageMaterial value;

	public UpdateImageRequestBean(RequestImageMaterial value, int oldtype){
		super.setMcid(value.getMcid());
		super.setOldtype(oldtype);
		super.setNewtype(CproUnitConstant.DRMC_MATTYPE_IMAGE);
		
		this.value = value;
	}
	
	public RequestImageMaterial getValue() {
		return value;
	}

	public void setValue(RequestImageMaterial value) {
		this.value = value;
	}
}
