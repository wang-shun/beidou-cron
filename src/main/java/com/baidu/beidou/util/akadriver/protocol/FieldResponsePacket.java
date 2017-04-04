package com.baidu.beidou.util.akadriver.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 审核字段响应包
 * @author liuzeyin
 *
 */
public class FieldResponsePacket implements Serializable {

	private static final long serialVersionUID = 6794289857330519955L;
	
	/**
	 * Info	string	结果信息。取值参见：审核返回信息（info字段）的定义
	 */
	private String Info;
	
	/**
	 * Flag	u_int	审核结果的标志位，说明触犯了哪些规则。参见：Dr-aka各flag定义
	 */
	private long Flag;
	/**
	 * Level	u_int	审核结果的等级，表示触犯了何种等级的规则，应予以何种处理。参见返回结果中的level定义。
	 */
	private long Level;
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}


	public String getInfo() {
		return Info;
	}


	public void setInfo(String info) {
		Info = info;
	}


	public long getFlag() {
		return Flag;
	}


	public void setFlag(long flag) {
		Flag = flag;
	}


	public long getLevel() {
		return Level;
	}


	public void setLevel(long level) {
		Level = level;
	}

}
