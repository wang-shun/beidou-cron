package com.baidu.beidou.util.cdndriver.protocol;

import com.baidu.beidou.util.socket.NsHead;

public class CdnNsHead extends NsHead {
	
	public static final String PROVIDER = "beidou";

	public CdnNsHead(){
		super();
		
		provider = PROVIDER;
		version = 1;
	}
	
	public CdnNsHead(byte[] input){
		super(input);
	}
}
