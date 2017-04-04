package com.baidu.beidou.util.akadriver.transform;

import java.util.List;

import org.apache.commons.collections.Transformer;

import com.baidu.beidou.util.akadriver.bo.AkaBeidouResult;
import com.baidu.beidou.util.akadriver.constant.Constant;
import com.baidu.beidou.util.akadriver.protocol.AdvResponsePacket;
import com.baidu.beidou.util.akadriver.protocol.FieldResponsePacket;

/**
 * ClassName: AkaPatrolUnitResultInfoTransformer
 * Function: 为aka轮询创意使用，将aka返回响应包包装
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 
 * @since TODO
 * @date Jul 21, 2011
 * @see 
 */
public class AkaPatrolUnitResultInfoTransformer implements Transformer {

	/**
	 * 将返回AdvResponsePacket结果转成Map
	 */
	public Object transform(Object obj) {
		AdvResponsePacket adv = (AdvResponsePacket) obj;
		AkaBeidouResult resinfo = new AkaBeidouResult();
		List<FieldResponsePacket> fields = adv.getRsltArray();

		long level = fields.get(0).getLevel() 
				| fields.get(1).getLevel()
				| fields.get(2).getLevel() 
				| fields.get(3).getLevel()
				| fields.get(4).getLevel()
				| fields.get(5).getLevel()
				| fields.get(6).getLevel();
		if (level == Constant.RESULT_LEVEL_PASS) {
			resinfo.setPatrolFlag(0);
			return resinfo;
		}

		// 检查触犯何种规则
		AkaBeidouResult sectionResult = Constant.buildResultForPatrol(
				fields.get(0).getFlag()
				| fields.get(1).getFlag()
				| fields.get(2).getFlag()
				| fields.get(3).getFlag()
				| fields.get(4).getFlag()
				| fields.get(5).getFlag()
				| fields.get(6).getFlag());
		resinfo.setPatrolFlag(sectionResult.getPatrolFlag());

		return resinfo;
	}

}
