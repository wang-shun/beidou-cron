package com.baidu.beidou.util.akadriver.protocol;

import com.baidu.beidou.util.socket.NsHead;


public class AkaNsHead extends NsHead {
	
	/**
	 * unsigned int list_type	1	使用第一套词表
	 * private long reserved;
	 */

	
	public AkaNsHead(int clientId) {
		// init some fields
		/**
		 * sf-web1为0
		 * sf-web2为1
		 * fengchao为2
		 * beidou-主题词为7
		 * beidou-创意为8
		 */
		id = clientId;
		/**
		 * 当前版本为1,可以在配置文件中指定
		 */
		version = 1;
		/**
		 * sf-web1为"sf-web1"
		 * sf-web2为"sf-web2"
		 * fengchao为"fengchao"
		 * beidou为"beidou" 
		 */
		provider = "beidou";
		/**
		 * 正常词表是2 注册商标词表是64
		 * @author genglei01, cpweb-174
		 * listtype = 128 
		 */
		reserved = 2;
	}
	
	public AkaNsHead(byte[] input) {
		super(input);
	}
	
}
