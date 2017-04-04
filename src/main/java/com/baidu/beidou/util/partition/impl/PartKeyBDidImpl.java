/**
 * 
 */
package com.baidu.beidou.util.partition.impl;

import com.baidu.beidou.util.partition.PartKey;

/**
 * 
 * 根据用户在beidou中的id来拆表的策略
 * 
 * @author zengyunfeng
 * @version 1.0.0
 */
public class PartKeyBDidImpl implements PartKey {
	
	private static final long serialVersionUID = -6574520917811040151L;
	private int userid = 0;

	
	public PartKeyBDidImpl(int userid) {
		super();
		this.userid = userid;
	}


	/* (non-Javadoc)
	 * @see com.baidu.beidou.util.partition.PartKey#getKey()
	 */
	public long getKey() {
		return userid;
	}

}
