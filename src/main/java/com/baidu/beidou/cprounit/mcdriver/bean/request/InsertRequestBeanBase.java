package com.baidu.beidou.cprounit.mcdriver.bean.request;


/**
 * 向dr-mc提交物料信息时所用结构体的基类
 * 
 * @author yanjie
 *
 */
abstract public class InsertRequestBeanBase {
	/*物料类型*/
	private int type;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
