package com.baidu.beidou.unionsite.vo;

import java.io.Serializable;

/**
 * @author zhuqian
 *
 */
public class SiteElement implements Serializable {

	private static final long serialVersionUID = 6593538231417656901L;
	
	private int id;
	private String name;
	
	public SiteElement(){}
	
	public SiteElement(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
