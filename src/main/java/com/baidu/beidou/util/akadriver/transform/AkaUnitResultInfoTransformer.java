package com.baidu.beidou.util.akadriver.transform;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;

import com.baidu.beidou.util.akadriver.bo.AkaBeidouResult;
import com.baidu.beidou.util.akadriver.constant.AKA_RES_CODE;
import com.baidu.beidou.util.akadriver.constant.Constant;
import com.baidu.beidou.util.akadriver.protocol.AdvResponsePacket;
import com.baidu.beidou.util.akadriver.protocol.FieldResponsePacket;

public class AkaUnitResultInfoTransformer implements Transformer{
	
	private static final String INFO_SEPARATE_TOKEN = "，";
	
	/**
	 * 将返回AdvResponsePacket结果转成Map
	 */
	public Object transform(Object obj) {
		AdvResponsePacket adv = (AdvResponsePacket)obj;
		AkaBeidouResult resinfo = new AkaBeidouResult();
		StringBuilder msg = new StringBuilder();
		long token = 0L;
		List<FieldResponsePacket> fields = adv.getRsltArray();
		
		long level = fields.get(0).getLevel() 
			| fields.get(1).getLevel() 
			| fields.get(2).getLevel() 
			| fields.get(3).getLevel()
			| fields.get(4).getLevel()
			| fields.get(5).getLevel()
			| fields.get(6).getLevel();
		if(level == Constant.RESULT_LEVEL_PASS){
			resinfo.setToken(0L);
			return resinfo;
		}
		
		//拼装错误提示
		//title
		AkaBeidouResult sectionResult = buildResult(fields.get(0), Constant.TITLE_AUDIT);
		if(sectionResult.getToken() != 0){
			token |= sectionResult.getToken();
			resinfo.setTitleError(sectionResult.getMsg());
			msg.append(sectionResult.getMsg());
		}
		
		//desc1
		//long level3 = field3.getLevel();
		sectionResult = buildResult(fields.get(1), Constant.DESC1_AUDIT);
		if(sectionResult.getToken() != 0){
			token |= sectionResult.getToken();
			resinfo.setDesc1Error(sectionResult.getMsg());
			if(msg.length()>0){
				msg.append(INFO_SEPARATE_TOKEN);
			}
			msg.append(sectionResult.getMsg());
		}
		
		//desc2
		//long level4 = field4.getLevel();
		sectionResult = buildResult(fields.get(2), Constant.DESC2_AUDIT);
		if(sectionResult.getToken() !=0){
			token |= sectionResult.getToken();
			resinfo.setDesc2Error(sectionResult.getMsg());
			if(msg.length()>0){
				msg.append(INFO_SEPARATE_TOKEN);
			}
			msg.append(sectionResult.getMsg());
		}
		
		//title+desc1+desc2
		sectionResult = Constant.buildResult(fields.get(0).getFlag()|fields.get(1).getFlag()|fields.get(2).getFlag(), Constant.IDEA_AUDIT);
		if(sectionResult.getToken() != 0){
			token |= sectionResult.getToken();
			if(msg.length()>0){
				msg.append(INFO_SEPARATE_TOKEN);
			}
			msg.append(sectionResult.getMsg());
		}
		
		//url
		sectionResult = Constant.buildResult(fields.get(3).getFlag(), Constant.URL_AUDIT);
		if(sectionResult.getToken() != 0){
			token |= sectionResult.getToken();
			if(msg.length()>0){
				msg.append(INFO_SEPARATE_TOKEN);
			}
			msg.append(sectionResult.getMsg());
		}
		
		//SHOWurl
		sectionResult = Constant.buildResult(fields.get(4).getFlag(), Constant.SHOWURL_AUDIT);
		if(sectionResult.getToken() != 0){
			token |= sectionResult.getToken();
			if(msg.length()>0){
				msg.append(INFO_SEPARATE_TOKEN);
			}
			msg.append(sectionResult.getMsg());
		}
		
		//wireless targeturl
		sectionResult = Constant.buildResult(fields.get(5).getFlag(), Constant.WIRELESS_TARGETURL_AUDIT);
		if (sectionResult.getToken() != 0) {
			token |= sectionResult.getToken();
			if (msg.length() > 0) {
				msg.append(INFO_SEPARATE_TOKEN);
			}
			msg.append(sectionResult.getMsg());
		}

		//wireless showurl
		sectionResult = Constant.buildResult(fields.get(6).getFlag(), Constant.WIRELESS_SHOWURL_AUDIT);
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
	
	private static AkaBeidouResult buildResult(FieldResponsePacket packet, Map<Integer, AKA_RES_CODE> dict){
		AkaBeidouResult result = new AkaBeidouResult();
		long token = 0L;
		StringBuilder msg = new StringBuilder();
		int i =0;
		for(Map.Entry<Integer, AKA_RES_CODE> ele : dict.entrySet()){
			if((packet.getFlag()&ele.getKey()) == ele.getKey()){
				token = token | ele.getValue().getValue();
				if (++i > 1) {
					msg.append(INFO_SEPARATE_TOKEN);
				}
				//msg.append(ele.getValue().getString());
				msg.append(ele.getValue().getString().replace(Constant.WILDCARD, packet.getInfo()));
			}
		}
		result.setToken(token);
		result.setMsg(msg.toString());
		return result;
	}
}
