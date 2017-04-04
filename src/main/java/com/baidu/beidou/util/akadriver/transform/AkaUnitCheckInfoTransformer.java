package com.baidu.beidou.util.akadriver.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.Transformer;

import com.baidu.beidou.util.akadriver.bo.AkaUnitCheckInfo;
import com.baidu.beidou.util.akadriver.constant.Constant;
import com.baidu.beidou.util.akadriver.protocol.AdvRequestPacket;
import com.baidu.beidou.util.akadriver.protocol.FieldRequestPacket;

/**
 * 将bo转成协议
 * @author liuzeyin
 *
 */
public class AkaUnitCheckInfoTransformer implements Transformer {

	
	/**
	 * 将AkaCheckInfo转成AdvRequestPacket
	 */
	public Object transform(Object obj) {
		AkaUnitCheckInfo checkinfo = (AkaUnitCheckInfo)obj;
		AdvRequestPacket requestPacket = new AdvRequestPacket();
		requestPacket.setUserID(checkinfo.getUserid());
		requestPacket.setCheckUser(Constant.CHECK_USER_FILTER);
		List<FieldRequestPacket> fields = new ArrayList<FieldRequestPacket>();
		
		//title
		FieldRequestPacket field2 = new FieldRequestPacket();
		field2.setAdFlag(titleAuditflag());
		field2.setPreFlag(titlePreproc());
		field2.setCont(checkinfo.getIdeaTitle());
		
		//desc1
		FieldRequestPacket field3 = new FieldRequestPacket();
		field3.setAdFlag(desc1Auditflag());
		field3.setPreFlag(desc1Preproc());
		field3.setCont(checkinfo.getIdeaDesc1());
		
		//desc2
		FieldRequestPacket field4 = new FieldRequestPacket();
		field4.setAdFlag(desc2Auditflag());
		field4.setPreFlag(desc2Preproc());
		field4.setCont(checkinfo.getIdeaDesc2());
		
		//url
		FieldRequestPacket field5 = new FieldRequestPacket();
		field5.setAdFlag(urlAuditflag());
		field5.setPreFlag(urlPreproc());
		field5.setCont(checkinfo.getIdeaUrl());
		
		//showurl
		FieldRequestPacket field6 = new FieldRequestPacket();
		field6.setAdFlag(showurlAuditflag());
		field6.setPreFlag(showurlPreproc());
		field6.setCont(checkinfo.getIdeaShowUrl());
		
		//wireless targeturl
		FieldRequestPacket field7 = new FieldRequestPacket();
		field7.setAdFlag(wirelessTargetUrlAuditflag());
		field7.setPreFlag(wirelessTargetUrlPreproc());
		String wirelessTargetUrl = checkinfo.getWirelessTargetUrl();
		if (wirelessTargetUrl == null) {
			field7.setCont("");
		} else {
			field7.setCont(wirelessTargetUrl);
		}
		
		//wireless showurl
		FieldRequestPacket field8 = new FieldRequestPacket();
		field8.setAdFlag(wirelessShowUrlAuditflag());
		field8.setPreFlag(wirelessShowUrlPreproc());
		String wirelessShowUrl = checkinfo.getWirelessShowUrl();
		if (wirelessShowUrl == null) {
			field8.setCont("");
		} else {
			field8.setCont(wirelessShowUrl);
		}
		
		Collections.addAll(fields, field2, field3, field4, field5, field6, field7, field8);
		requestPacket.setSecArray(fields);
		
		return requestPacket;
	}
	
	
	/**
	 * 标题支持的预处理规则
	 *	0x0001	去除不可见字符
	 *	0x0004	全角转半角
	 * @return
	 * liuzeyin
	 * 2009-12-5
	 */
	protected int titlePreproc() {
		return Constant.PREPROC_OFF;
	}
	
	/**
	 * 标题支持的审核位
	 *	0x0002	黑名单审核
	 *	0x0004	注册商标审核
	 *	0x0008	标题+描述触犯黑名单审核
	 *	0x0010	标题+描述触犯注册商标
	 *	0x0800	竞品/侵权审核
	 *	0x1000	标题+描述触犯竞品/侵权审核
	 * @see Constant#AUDIT_BLACK_WORD
	 * @return
	 * liuzeyin
	 * 2009-12-5
	 * @author genglei01
	 * @2010-09-06, cpweb-174
	 */
	protected int titleAuditflag() {
		return Constant.AUDIT_BLACK_WORD 
			| Constant.AUDIT_BRAND_WORD 
			| Constant.AUDIT_BLACK_IDEA 
			| Constant.AUDIT_BRAND_IDEA
			| Constant.AUDIT_COMPETITIVE_WORD
			| Constant.AUDIT_COMPETITIVE_IDEA;
	}

	/**
	 * 描述1支持的预处理规则
	 *	0x0001	去除不可见字符
	 *	0x0004	全角转半角
	 * @return
	 * liuzeyin
	 * 2009-12-5
	 */
	protected int desc1Preproc() {
		return Constant.PREPROC_OFF;
	}
	
	/**
	 * 描述1支持的审核位
	 *	0x0001	特殊字符审核
	 *	0x0002	黑名单审核
	 *	0x0004	注册商标审核
	 * @see Constant#AUDIT_BLACK_WORD
	 * @return
	 * liuzeyin
	 * 2009-12-5
	 * @author genglei01
	 * @2010-09-06, cpweb-174
	 */
	protected int desc1Auditflag() {
		return Constant.AUDIT_BLACK_WORD 
			| Constant.AUDIT_BRAND_WORD 
			| Constant.AUDIT_BLACK_IDEA 
			| Constant.AUDIT_BRAND_IDEA
			| Constant.AUDIT_COMPETITIVE_WORD
			| Constant.AUDIT_COMPETITIVE_IDEA;
	}
	
	/**
	 * 描述2支持的预处理规则
	 *	0x0001	去除不可见字符
	 *	0x0004	全角转半角
	 *	0x0010	去除html标签
	 * @return
	 * liuzeyin
	 * 2009-12-5
	 */
	protected int desc2Preproc() {
		return Constant.PREPROC_OFF;
	}
	
	/**
	 * 描述2支持的审核位
	 *	0x0001	特殊字符审核
	 *	0x0002	黑名单审核
	 *	0x0004	注册商标审核
	 * @see Constant#AUDIT_BLACK_WORD
	 * @return
	 * liuzeyin
	 * 2009-12-5
	 * @author genglei01
	 * @2010-09-06, cpweb-174
	 */
	protected int desc2Auditflag() {
		return Constant.AUDIT_BLACK_WORD 
			| Constant.AUDIT_BRAND_WORD 
			| Constant.AUDIT_BLACK_IDEA 
			| Constant.AUDIT_BRAND_IDEA
			| Constant.AUDIT_COMPETITIVE_WORD
			| Constant.AUDIT_COMPETITIVE_IDEA;
	}
	
	/**
	 * 点击url支持的预处理规则
	 *	0x0001	去除不可见字符
	 *	0x0020	url归一化
	 * @return
	 * liuzeyin
	 * 2009-12-5
	 */
	protected int urlPreproc() {
		return Constant.PREPROC_OFF;
	}
	
	/**
	 * 点击url支持的审核位
	 *	0x0001	特殊字符审核
	 *	0x0100	非法后缀审核
	 *	0x0080	非法前缀审核
	 * @return
	 * liuzeyin
	 * 2009-12-5
	 */
	protected int urlAuditflag() {
		return Constant.AUDIT_SPECIAL_CHAR | Constant.AUDIT_ILLEGAL_SUFFIX | Constant.AUDIT_ILLEGAL_PREFIX;
	}
	
	/**
	 * 显示url支持的预处理规则
	 *	0x0001	去除不可见字符
	 *	0x0020	url归一化
	 * @return
	 * liuzeyin
	 * 2009-12-5
	 */
	protected int showurlPreproc() {
		return Constant.PREPROC_OFF;
	}
	
	/**
	 * 显示url支持的审核位
	 *	0x0001	特殊字符审核
	 *	0x0100	非法后缀审核
	 *	0x0080	非法前缀审核
	 * @see Constant#AUDIT_BLACK_WORD
	 * @return
	 * liuzeyin
	 * 2009-12-5
	 * @author genglei01
	 * @2010-09-06, cpweb-174
	 */
	protected int showurlAuditflag() {
		return Constant.AUDIT_BLACK_WORD | Constant.AUDIT_BRAND_WORD | Constant.AUDIT_COMPETITIVE_WORD;
	}
	
	/**
	 * wirelessTargetUrlPreproc: 无线点击url支持的预处理规则
	 *	0x0001	去除不可见字符
	 *	0x0020	url归一化
	 * @version cpweb-587
	 * @author genglei01
	 * @date Apr 17, 2013
	 */
	protected int wirelessTargetUrlPreproc() {
		return Constant.PREPROC_OFF;
	}
	
	/**
	 * wirelessTargetUrlAuditflag: 无线点击url支持的审核位
	 *	0x0001	特殊字符审核
	 *	0x0100	非法后缀审核
	 *	0x0080	非法前缀审核
	 * @version cpweb-587
	 * @author genglei01
	 * @date Apr 17, 2013
	 */
	protected int wirelessTargetUrlAuditflag() {
		return Constant.AUDIT_SPECIAL_CHAR | Constant.AUDIT_ILLEGAL_SUFFIX | Constant.AUDIT_ILLEGAL_PREFIX;
	}
	
	/**
	 * wirelessShowUrlPreproc: 无线显示url支持的预处理规则
	 *	0x0001	去除不可见字符
	 *	0x0020	url归一化
	 * @version cpweb-587
	 * @author genglei01
	 * @date Apr 17, 2013
	 */
	protected int wirelessShowUrlPreproc() {
		return Constant.PREPROC_OFF;
	}
	
	/**
	 * wirelessShowUrlAuditflag: 无线显示url支持的审核位
	 *	0x0001	特殊字符审核
	 *	0x0100	非法后缀审核
	 *	0x0080	非法前缀审核
	 * @version cpweb-587
	 * @author genglei01
	 * @date Apr 17, 2013
	 */
	protected int wirelessShowUrlAuditflag() {
		return Constant.AUDIT_BLACK_WORD | Constant.AUDIT_BRAND_WORD | Constant.AUDIT_COMPETITIVE_WORD;
	}
}
