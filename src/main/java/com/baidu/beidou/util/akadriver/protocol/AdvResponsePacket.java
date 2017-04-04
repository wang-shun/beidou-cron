package com.baidu.beidou.util.akadriver.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class AdvResponsePacket implements Serializable {

	private static final long serialVersionUID = -8774250603653016398L;
	
	/**
	 * UserID	u_int	用户ID，和请求一致。	4
	 */
	private long UserID;
	
	/**
	 * UserRslt	u_int	用户黑名单检查结果，0通过，1不通过	4
	 */
	private long UserRslt;
	
	
	/**
	 * 字段审核结果
	 */
	private List<FieldResponsePacket> RsltArray = new ArrayList<FieldResponsePacket>();
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}


	public long getUserID() {
		return UserID;
	}


	public void setUserID(long userID) {
		UserID = userID;
	}


	public long getUserRslt() {
		return UserRslt;
	}


	public void setUserRslt(long userRslt) {
		UserRslt = userRslt;
	}


	public List<FieldResponsePacket> getRsltArray() {
		return RsltArray;
	}


	public void setRsltArray(List<FieldResponsePacket> rsltArray) {
		RsltArray = rsltArray;
	}
	
}
