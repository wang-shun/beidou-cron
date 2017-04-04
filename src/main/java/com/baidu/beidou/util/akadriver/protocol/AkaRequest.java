package com.baidu.beidou.util.akadriver.protocol;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class AkaRequest {
	private List<AdvRequestPacket> ReqCon = new ArrayList<AdvRequestPacket>();

	public List<AdvRequestPacket> getReqCon() {
		return ReqCon;
	}

	public void setReqCon(List<AdvRequestPacket> reqCon) {
		ReqCon = reqCon;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
