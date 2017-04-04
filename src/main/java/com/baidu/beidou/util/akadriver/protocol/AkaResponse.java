package com.baidu.beidou.util.akadriver.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class AkaResponse implements Serializable {
	private static final long serialVersionUID = 2886175097871936581L;
	
	/**
	 * 0表示返回正常
	 */
	private long Status;
	
	private List<AdvResponsePacket> ResCon = new ArrayList<AdvResponsePacket>();
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public long getStatus() {
		return Status;
	}

	public void setStatus(long status) {
		Status = status;
	}

	public List<AdvResponsePacket> getResCon() {
		return ResCon;
	}

	public void setResCon(List<AdvResponsePacket> resCon) {
		ResCon = resCon;
	}

}
