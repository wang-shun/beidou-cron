package com.baidu.beidou.util.akadriver.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
/**
 * 待审核aka广告
 * @author liuzeyin
 *
 */
public class AdvRequestPacket implements Serializable {

	private static final long serialVersionUID = -5221156817617907049L;
	
	/**
	 * UserID	u_int	4
	 */
	private long UserID;
	
	/**
	 * CheckUser	u_int	是否检查用户黑名单，0不检查，1检查  	4
	 */
	private long CheckUser = 0;
	
	/**
	 * 待审核字段
	 */
	private List<FieldRequestPacket> SecArray = new ArrayList<FieldRequestPacket>();

	
	
	
	
	
	public long getUserID() {
		return UserID;
	}






	public void setUserID(long userID) {
		UserID = userID;
	}






	public long getCheckUser() {
		return CheckUser;
	}






	public void setCheckUser(long checkUser) {
		CheckUser = checkUser;
	}






	public List<FieldRequestPacket> getSecArray() {
		return SecArray;
	}






	public void setSecArray(List<FieldRequestPacket> secArray) {
		SecArray = secArray;
	}






	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
