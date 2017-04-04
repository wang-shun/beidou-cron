/**
 * 2009-12-16 下午03:27:43
 * @author zengyunfeng
 */
package com.baidu.beidou.user.vo;

import java.io.Serializable;

/**
 * @author zengyunfeng
 *
 */
public class GlobalAuditerListCache implements Serializable{

	private static final long serialVersionUID = -6757016886119594742L;
	private int[] keyAuditer;	//大客户和VIP客户审核员
	private int[] normalAuditer;	//普通客户审核员
	/**
	 * @return the keyAuditer
	 */
	public int[] getKeyAuditer() {
		return keyAuditer;
	}
	/**
	 * @param keyAuditer the keyAuditer to set
	 */
	public void setKeyAuditer(int[] keyAuditer) {
		this.keyAuditer = keyAuditer;
	}
	/**
	 * @return the normalAuditer
	 */
	public int[] getNormalAuditer() {
		return normalAuditer;
	}
	/**
	 * @param normalAuditer the normalAuditer to set
	 */
	public void setNormalAuditer(int[] normalAuditer) {
		this.normalAuditer = normalAuditer;
	}
	
	
	
}
