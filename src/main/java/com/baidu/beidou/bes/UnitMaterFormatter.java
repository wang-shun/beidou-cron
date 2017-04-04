/**
 * IFengUnitMaterFormatter.java 
 */
package com.baidu.beidou.bes;

import com.baidu.beidou.cprounit.bo.UnitMaterView;

/**
 * adx生成物料数据过程中的UnitMaterView格式化器
 * 
 * @author lixukun
 * @date 2014-03-10
 */
public class UnitMaterFormatter implements SimpleObjectFormatter<UnitMaterView> {

	@Override
	public String formatObject(UnitMaterView obj) {
		if (obj == null) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(obj.getUserId()).append("\t")
		  .append(obj.getId()).append("\t")
		  .append(obj.getWuliaoType()).append("\t")
		  .append(obj.getMcId()).append("\t")
		  .append(obj.getMcVersionId());
		
		return sb.toString();
	}

}
