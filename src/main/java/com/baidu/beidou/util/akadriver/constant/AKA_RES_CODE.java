/**
 * 2008-12-19 下午04:57:45
 * @author zengyunfeng
 * @version 1.1.0
 */
package com.baidu.beidou.util.akadriver.constant;

/**
 * @author zengyunfeng aka_online对于的检查规则设置
 */
public enum AKA_RES_CODE {
	
	KEYSC(0x0001L, "主题词中含有特殊字符"),
	KEYI(0x0002L, "主题词触犯黑名单规则"),
	KEYM(0x0004L, "主题词触犯注册商标规则"),
	KEYC(0x0008L, "主题词触犯侵权词汇"),
	TITSC(0x0010L, "标题中含有特殊字符"),
	TITI(0x0020L, "\"" + Constant.WILDCARD + "\"触犯黑名单"),
	TITM(0x0040L, "\"" + Constant.WILDCARD + "\"触犯注册商标"),
	TITC(0x0080L, "\"" + Constant.WILDCARD + "\"触犯侵权词汇"),
	CORI(0x0100L, "标题+描述触犯黑名单规则"),
	CORM(0x0200L, "标题+描述触犯注册商标"),
	CORC(0x0400L, "标题+描述触犯侵权词汇"),
	DESC1SC(0x1000L, "描述第一行中含有特殊字符"),
	DESC1I(0x2000L, "\"" + Constant.WILDCARD + "\"触犯黑名单规则"),
	DESC1M(0x4000L, "\"" + Constant.WILDCARD + "\"触犯注册商标规则"),
	DESC1C(0x8000L, "\"" + Constant.WILDCARD + "\"触犯侵权词汇"),
	DESC2SC(0x00010000L, "右侧描述2中含有特殊字符"),
	DESC2I(0x00020000L, "\"" + Constant.WILDCARD + "\"触犯黑名单规则"),
	DESC2M(0x00040000L, "\"" + Constant.WILDCARD + "\"触犯注册商标规则"),
	DESC2C(0x00080000L, "\"" + Constant.WILDCARD + "\"触犯侵权词汇"),
	
	URLEND(0x00100000L, "点击链接中含有非法后缀"),
	URLHEAD(0x00200000L, "点击链接中含有非法前缀"),
	URLSC(0x00400000L, "点击链接中含有特殊字符"),
	SHOWURLBLACK(0x01000000L, "\"" + Constant.WILDCARD + "\"触犯黑名单规则"),
	SHOWURLBLAND(0x02000000L, "\"" + Constant.WILDCARD + "\"触犯商标规则"),
	SHOWURLCOMPETITIVE(0x04000000L, "\"" + Constant.WILDCARD + "\"触犯侵权词汇"),
	
	WIRELESS_URLEND(0x10000000L, "移动点击链接中含有非法后缀"),
	WIRELESS_URLHEAD(0x20000000L, "移动点击链接中含有非法前缀"),
	WIRELESS_URLSC(0x40000000L, "移动点击链接中含有特殊字符"),
	WIRELESS_SHOWURLBLACK(0x000100000000L, "移动显示链接\"" + Constant.WILDCARD + "\"触犯黑名单规则"),
	WIRELESS_SHOWURLBLAND(0x000200000000L, "移动显示链接\"" + Constant.WILDCARD + "\"触犯商标规则"),
	WIRELESS_SHOWURLCOMPETITIVE(0x000400000000L, "移动显示链接\"" + Constant.WILDCARD + "\"触犯侵权词汇");

	private final long value;
	private final String des;

	private AKA_RES_CODE(long value, String des) {
		this.value = value;
		this.des = des;
	}

	public long getValue() {
		return this.value;
	}

	public String getString() {
		return this.des;
	}
}
