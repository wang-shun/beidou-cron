package com.baidu.beidou.util.akadriver.constant;

import java.nio.ByteOrder;
import java.util.Properties;

public class ConstantFile {
	
	public static String configFileName="aka";
	
	public static Properties CONFIG_MEM_POP;
	
	public static final int INT_BYTE_LEN=4;
	
	
	public static final long REQUEST_CHECK_FLAG=Long.MAX_VALUE;
	
	public static final int REQUEST_PREPRC_FLAG=Integer.MAX_VALUE;
	
	
	public static final long REQUEST_NEGATIVE_CHECK_FLAG=Long.parseLong("0001", 16);
	
	public static final int REQUEST_NEGATIVE_PREPRC_FLAG=Integer.parseInt("10000", 16);
	
	
	public static final long REQUEST_IDEAPREP_CHECK_FLAG=Long.parseLong("0", 16);
	
	public static final int REQUEST_IDEAPREP_PREPRC_FLAG=Integer.parseInt("20000", 16)+Integer.parseInt("80000", 16)+Integer.parseInt("100000", 16);	
	
	public static final long REQUEST_KEYWORDURL_CHECK_FLAG=Long.parseLong("4000000", 16)+Long.parseLong("1000000", 16)+Long.parseLong("2000000", 16);
	
	public static final int REQUEST_KEYWORDURL_PREPRC_FLAG=Integer.parseInt("200000", 16);	
	
	
	public static final ByteOrder AKA_PROTOCOL_NETBYTEORDER=ByteOrder.LITTLE_ENDIAN;
	
	// URLSC 0x04000000 URL中含有特殊字符
	// TITI 0x0200 标题触犯黑名单
	// CORI 0x00100000 标题+描述触犯黑名单规则（左右侧都适用）
	// URLEND 0x01000000 URL中含有非法后缀
	// URLHEAD 0x02000000 URL中含有非法前缀
	// DESC1I 0x200000000 右侧描述1触犯黑名单规则（仅右侧）
	// DESC2I 0x20000000000 右侧描述2触犯黑名单规则（仅右侧）
	public static final long BEIDOU_REQUEST_FLAG_LIT = AKA_RES_CODE.URLSC
			.getValue()
			| AKA_RES_CODE.TITI.getValue()
			| AKA_RES_CODE.TITM.getValue() // 黑名单和注册商标必须是同一个规则，要么不审核，要么全部审核
			| AKA_RES_CODE.CORI.getValue()
			| AKA_RES_CODE.CORM.getValue() // 黑名单和注册商标必须是同一个规则，要么不审核，要么全部审核
			| AKA_RES_CODE.URLEND.getValue()
			| AKA_RES_CODE.URLHEAD.getValue()
			| AKA_RES_CODE.DESC1I.getValue() | AKA_RES_CODE.DESC1M.getValue() // 黑名单和注册商标必须是同一个规则，要么不审核，要么全部审核
			| AKA_RES_CODE.DESC2I.getValue() | AKA_RES_CODE.DESC2M.getValue(); // 黑名单和注册商标必须是同一个规则，要么不审核，要么全部审核

	public static final long BEIDOU_REQUEST_FLAG_PIC = AKA_RES_CODE.URLSC
			.getValue()
			| AKA_RES_CODE.URLEND.getValue() | AKA_RES_CODE.URLHEAD.getValue();

	/**
	 * beidou关键词aka审核标识
	 */
	public static final long BEIDOU_REQUEST_FLAG_KEYWORD = AKA_RES_CODE.KEYSC
			.getValue()
			| AKA_RES_CODE.KEYI.getValue() | AKA_RES_CODE.KEYM.getValue();

	public static final int BEIDOU_REQUEST_PREPRC_FLAG = 0;

}
