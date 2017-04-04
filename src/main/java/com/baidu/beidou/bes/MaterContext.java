/**
 * MaterContext.java 
 */
package com.baidu.beidou.bes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.beidou.cprounit.bo.UnitMaterView;

/**
 * 过滤时候的上下文类，maters是过滤源，每个过滤器应该对该源做相应的修改<br/>
 * 一个简单的attributes用于上下文时属性的设置<br/>
 * 支持批量过滤<br/>
 * 
 * @author lixukun
 * @date 2013-12-24
 */
public class MaterContext {
	private List<UnitMaterView> maters;
	private Map<String, Object> attributes = new HashMap<String, Object>();
	
	public MaterContext(List<UnitMaterView> maters) {
		if (maters == null) {
			throw new IllegalArgumentException("UnitMater should not be null");
		}
		this.maters = maters;
	}
	
	public List<UnitMaterView> getUnitMaters() {
		return maters;
	}
	
	public void setUnitMaters(List<UnitMaterView> maters) {
		this.maters = maters;
	}
	
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}
	
	public Object getAttribute(String key) {
		return attributes.get(key);
	}
}
