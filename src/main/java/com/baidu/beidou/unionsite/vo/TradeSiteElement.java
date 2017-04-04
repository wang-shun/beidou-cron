package com.baidu.beidou.unionsite.vo;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.KeyValue;

/**
 * @author zhuqian
 *
 */
public class TradeSiteElement implements Serializable {

	private static final long serialVersionUID = -4743667785598523209L;
	
	private int id;
	private String name;
	List<SiteElement> sites;
	
	public TradeSiteElement(){}
	
	public TradeSiteElement(int id, String name, List<SiteElement> sites){
		this.id = id;
		this.name = name;
		this.sites = sites;
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
	/**
	 * @return the sites
	 */
	public List<SiteElement> getSites() {
		return sites;
	}
	/**
	 * @param sites the sites to set
	 */
	public void setSites(List<SiteElement> sites) {
		this.sites = sites;
	}

}
