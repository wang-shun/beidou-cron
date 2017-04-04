package com.baidu.beidou.util.akadriver.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 审核字段请求包
 * @author liuzeyin
 *
 */
public class FieldRequestPacket implements Serializable {

	private static final long serialVersionUID = 2160796701378611710L;
	
	/**
	 * Cont	string	Section	内容变长，最大长度由双方约定
	 */
	private String Cont = "";
	
	/**
	 * PreFlag	u_int	预处理设置:数值二进制的每一位均表示一种预处理，该字段取值参见：Dr-aka各flag定义
	 */
	private long PreFlag;
	
	/**
	 * AdFlag	u_int	审核设置：数值的每一位均表示作用于该字段的一种审核规则，该字段取值参见：Dr-aka各flag定义
	 */
	private long AdFlag;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public String getCont() {
		return Cont;
	}

	public void setCont(String cont) {
		Cont = cont;
	}

	public long getPreFlag() {
		return PreFlag;
	}

	public void setPreFlag(long preFlag) {
		PreFlag = preFlag;
	}

	public long getAdFlag() {
		return AdFlag;
	}

	public void setAdFlag(long adFlag) {
		AdFlag = adFlag;
	}

	
}
