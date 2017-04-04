package com.baidu.beidou.util.akadriver.transform;

import java.util.List;

import org.apache.commons.collections.Transformer;

import com.baidu.beidou.util.akadriver.bo.AkaBeidouResult;
import com.baidu.beidou.util.akadriver.constant.Constant;
import com.baidu.beidou.util.akadriver.protocol.AdvResponsePacket;
import com.baidu.beidou.util.akadriver.protocol.FieldResponsePacket;

public class AkaPicResultInfoTransformer implements Transformer{
	
	private static final String INFO_SEPARATE_TOKEN = "，";
	
	/**
	 * 将返回AdvResponsePacket结果转成Map
	 */
	public Object transform(Object obj) {
		AdvResponsePacket adv = (AdvResponsePacket)obj;
		AkaBeidouResult resinfo = new AkaBeidouResult();
		List<FieldResponsePacket> fields = adv.getRsltArray();
		
		long level = fields.get(0).getLevel()
				| fields.get(1).getLevel()
				| fields.get(2).getLevel()
				| fields.get(3).getLevel();
		if(level==Constant.RESULT_LEVEL_PASS){//如果全通过
			resinfo.setToken(0L);
			return resinfo;
		} 
		
		//target url
		StringBuilder msg = new StringBuilder();
		long token = 0L;
		AkaBeidouResult sectionResult = Constant.buildResult(fields.get(0).getFlag(), Constant.URL_AUDIT);
		if(sectionResult.getToken() != 0){
			token |= sectionResult.getToken();
			msg.append(sectionResult.getMsg());
		}
		
		//SHOWurl
		sectionResult = Constant.buildResult(fields.get(1).getFlag(), Constant.SHOWURL_AUDIT);
		if(sectionResult.getToken() != 0){
			token |= sectionResult.getToken();
			if(msg.length()>0){
				msg.append(INFO_SEPARATE_TOKEN);
			}
			msg.append(sectionResult.getMsg());
		}
		
		//wireless targeturl
		sectionResult = Constant.buildResult(fields.get(2).getFlag(), Constant.WIRELESS_TARGETURL_AUDIT);
		if (sectionResult.getToken() != 0) {
			token |= sectionResult.getToken();
			if (msg.length() > 0) {
				msg.append(INFO_SEPARATE_TOKEN);
			}
			msg.append(sectionResult.getMsg());
		}

		//wireless showurl
		sectionResult = Constant.buildResult(fields.get(3).getFlag(), Constant.WIRELESS_SHOWURL_AUDIT);
		if (sectionResult.getToken() != 0) {
			token |= sectionResult.getToken();
			if (msg.length() > 0) {
				msg.append(INFO_SEPARATE_TOKEN);
			}
			msg.append(sectionResult.getMsg());
		}
		
		resinfo.setToken(token);
		resinfo.setMsg(msg.toString());
		
		return resinfo;
	}
}
