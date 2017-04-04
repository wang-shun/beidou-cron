/**
 * 2009-8-24 下午05:33:02
 * @author zengyunfeng
 */
package com.baidu.beidou.cprounit.mcdriver.bean.response;

import java.io.Serializable;
import java.util.Map;

/**
 * @author zengyunfeng
 * @version 1.2.3
 * 
 * 对应DRMC生效临时物料的bean
 */
public class ActiveBean implements Serializable{
	private static final long serialVersionUID = -2381345270831350432L;
	
	private long tmpmcid;
	private long mcid;
	private Map<String, String> value;
	/**
	 * @return the tmpmcid
	 */
	public long getTmpmcid() {
		return tmpmcid;
	}
	/**
	 * @param tmpmcid the tmpmcid to set
	 */
	public void setTmpmcid(long tmpmcid) {
		this.tmpmcid = tmpmcid;
	}
	/**
	 * @return the mcid
	 */
	public long getMcid() {
		return mcid;
	}
	/**
	 * @param mcid the mcid to set
	 */
	public void setMcid(long mcid) {
		this.mcid = mcid;
	}
	/**
	 * @return the value
	 */
	public Map<String, String> getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Map<String, String> value) {
		this.value = value;
	}

}
