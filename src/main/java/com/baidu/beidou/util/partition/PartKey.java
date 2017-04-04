/**
 * 
 */
package com.baidu.beidou.util.partition;

import java.io.Serializable;

/**
 * 
 * 拆表的hash值
 * @author zengyunfeng
 * @version 1.0.0
 */
public interface PartKey extends Serializable {

	public long getKey();
	
}
