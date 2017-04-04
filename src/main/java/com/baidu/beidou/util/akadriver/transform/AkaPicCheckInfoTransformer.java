package com.baidu.beidou.util.akadriver.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.Transformer;

import com.baidu.beidou.util.akadriver.bo.AkaCheckInfo;
import com.baidu.beidou.util.akadriver.constant.Constant;
import com.baidu.beidou.util.akadriver.protocol.AdvRequestPacket;
import com.baidu.beidou.util.akadriver.protocol.FieldRequestPacket;

/**
 * 将bo转成协议
 * @author liuzeyin
 *
 */
public class AkaPicCheckInfoTransformer implements Transformer {

	
	/**
	 * 将AkaCheckInfo转成AdvRequestPacket
	 */
	public Object transform(Object obj) {
		AkaCheckInfo checkinfo = (AkaCheckInfo)obj;
		AdvRequestPacket requestPacket = new AdvRequestPacket();
		requestPacket.setUserID(checkinfo.getUserid());
		requestPacket.setCheckUser(Constant.CHECK_USER_FILTER);
		List<FieldRequestPacket> fields = new ArrayList<FieldRequestPacket>();
		
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
		
		Collections.addAll(fields, field5, field6, field7, field8);
		requestPacket.setSecArray(fields);
		
		return requestPacket;
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
	 * @return
	 * liuzeyin
	 * 2009-12-5
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
