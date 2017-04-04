/**
 * 
 */
package com.baidu.beidou.util.partition;

import java.io.Serializable;

/**
 * 
 * 拆表的分表标识
 * @author zengyunfeng
 * @version 1.0.0
 */
public class PartID implements Serializable {

	private static final long serialVersionUID = -8208846271601409111L;
	private int id;
	private String poname;
	private String tablename;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPoname() {
		return poname;
	}

	public void setPoname(String poname) {
		this.poname = poname;
	}

	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	
}
