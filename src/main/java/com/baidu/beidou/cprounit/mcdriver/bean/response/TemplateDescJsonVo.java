package com.baidu.beidou.cprounit.mcdriver.bean.response;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class TemplateDescJsonVo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3212729081598812592L;
	/**
	 * @return the width
	 */
	public Integer getWidth() {
		return width;
	}
	/**
	 * @param width the width to set
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}
	/**
	 * @return the height
	 */
	public Integer getHeight() {
		return height;
	}
	/**
	 * @param height the height to set
	 */
	public void setHeight(Integer height) {
		this.height = height;
	}
	/**
	 * @return the elements
	 */
	public List<Map<String, String>> getElements() {
		return elements;
	}
	/**
	 * @param elements the elements to set
	 */
	public void setElements(List<Map<String, String>> elements) {
		this.elements = elements;
	}
	
	private Integer width;
	private Integer height;
	private List<Map<String, String>> elements;
	
}
