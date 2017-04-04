/**
 * 2008-12-19 下午04:57:45
 * @author zengyunfeng
 * @version 1.1.0
 */
package com.baidu.beidou.util.akadriver.constant;

/**
 * @author zengyunfeng
 * aka_online预处理规则
 */
public enum AKA_PREPC_CODE {

	/**
	 * 关键字需要繁转简
	 */
	KEYTTOS(0x01), 
	/**
	 * 关键字需要预处理
	 */
	KEYPREPRC(0x10000),
	/**
	 * 标题需要预处理
	 */
	TITLEPREPRC(0x20000),
	/**
	 * 描述需要预处理（仅左侧）
	 */
	DESCPREPRC(0x40000), 
	/**
	 * 描述1需要预处理（仅右侧）
	 */
	DESC1PREPRC(0x80000), 
	/**
	 * 描述2需要预处理（仅右侧）
	 */
	DESC2PREPRC(0x100000),
	/**
	 * URL需要预处理
	 */
	URLPREPRC(0x200000), 
	/**
	 * 显示URL需要预处理
	 */
	SHOWURLPREPRC(0x400000);

	private final int value;

	private AKA_PREPC_CODE(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
