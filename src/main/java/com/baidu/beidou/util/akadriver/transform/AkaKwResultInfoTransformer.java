package com.baidu.beidou.util.akadriver.transform;

import java.util.List;

import org.apache.commons.collections.Transformer;

import com.baidu.beidou.util.akadriver.bo.AkaBeidouResult;
import com.baidu.beidou.util.akadriver.constant.Constant;
import com.baidu.beidou.util.akadriver.protocol.AdvResponsePacket;
import com.baidu.beidou.util.akadriver.protocol.FieldResponsePacket;

public class AkaKwResultInfoTransformer implements Transformer{
	
	/**
	 * 将返回AdvResponsePacket结果转成Map
	 */
	public Object transform(Object obj) {
		AdvResponsePacket adv = (AdvResponsePacket)obj;
		AkaBeidouResult resinfo = null;
		List<FieldResponsePacket> fields = adv.getRsltArray();
		
		//keyword
		FieldResponsePacket field1 = fields.get(0);
		long flag1 = field1.getFlag();
		
		if(field1.getLevel()==Constant.RESULT_LEVEL_PASS){//如果全通过
			resinfo = new AkaBeidouResult();
			resinfo.setToken(0L);
		} else {
			resinfo = Constant.buildResult(flag1, Constant.KEYWORD_AUDIT);
		}
		
		return resinfo;
	}
	
	
	
	
}
