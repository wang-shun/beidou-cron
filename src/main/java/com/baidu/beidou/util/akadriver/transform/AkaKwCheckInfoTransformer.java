package com.baidu.beidou.util.akadriver.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.Transformer;

import com.baidu.beidou.util.akadriver.bo.AkaKwCheckInfo;
import com.baidu.beidou.util.akadriver.constant.Constant;
import com.baidu.beidou.util.akadriver.protocol.AdvRequestPacket;
import com.baidu.beidou.util.akadriver.protocol.FieldRequestPacket;

/**
 * 将bo转成协议
 * @author liuzeyin
 *
 */
public class AkaKwCheckInfoTransformer implements Transformer {

	
	/**
	 * 将AkaCheckInfo转成AdvRequestPacket
	 */
	public Object transform(Object obj) {
		AkaKwCheckInfo checkinfo = (AkaKwCheckInfo)obj;
		AdvRequestPacket requestPacket = new AdvRequestPacket();
		requestPacket.setUserID(checkinfo.getUserid());
		requestPacket.setCheckUser(Constant.CHECK_USER_FILTER);//需要检查用户黑名单
		List<FieldRequestPacket> fields = new ArrayList<FieldRequestPacket>();
		//keyword
		FieldRequestPacket field1 = new FieldRequestPacket();
		field1.setAdFlag(keywordAuditflag());
		field1.setPreFlag(keywordPreproc());
		field1.setCont(checkinfo.getKeyWord());
		
		Collections.addAll(fields, field1);
		requestPacket.setSecArray(fields);
		
		return requestPacket;
	}
	
	
	/**
	 * 关键词支持的预处理规则
	 *	0x0001	去除不可见字符
	 *	0x0002	去除两边空格，将一个或多个连续空格转换为一个空格
	 *	0x0004	全角转半角
	 *	0x0008	繁体转简体
	 *	0x0010	去除html标签
	 * @return
	 * liuzeyin
	 * 2009-12-5
	 */
	protected int keywordPreproc() {
		return Constant.PREPROC_OFF;
	}
	/**
	 * 关键词支持的审核位
	 *	0x0001	特殊字符审核
	 *	0x0002	黑名单审核
	 *	0x0004	注册商标审核
	 * @return
	 * liuzeyin
	 * 2009-12-5
	 */
	protected int keywordAuditflag() {
		return Constant.AUDIT_SPECIAL_CHAR 
			| Constant.AUDIT_BLACK_WORD 
			| Constant.AUDIT_BRAND_WORD 
			| Constant.AUDIT_COMPETITIVE_WORD;
	}
	
}
