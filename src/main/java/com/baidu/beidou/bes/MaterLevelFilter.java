/**
 * MaterLevelFilter.java 
 */
package com.baidu.beidou.bes;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.google.common.collect.Lists;

/**
 * 根据美观度, 置信度, 欺诈度, 低俗度分类过滤物料<br/>
 * 阀值负数表明忽略该维度的过滤
 * 
 * @author lixukun
 * @date 2014-01-16
 */
public class MaterLevelFilter implements MaterFilter {
	private static final Log log = LogFactory.getLog(MaterFilter.class);
	private int confidence_level = -1;	// 置信度分档：0代表未评定，1代表低，2代表中，3代表高，负数代表不参与过滤
	private int beauty_level = -1;		// 美观度分档：0代表未评定，1代表低，2代表中，3代表高，负数代表不参与过滤
	private int cheat_level = -1;		// 欺诈度分档：0代表未评定，1代表是，2代表否，负数代表不参与过滤
	private int vulgar_level = -1;		// 低俗度分档：0代表未评定，1代表低，2代表中，3代表最低俗，负数代表不参与过滤
	
	@Override
	public void doFilter(MaterContext context) {
		if (context == null || context.getUnitMaters() == null) {
			return;
		}
		List<UnitMaterView> list = Lists.newArrayListWithCapacity(context.getUnitMaters().size());
		
		for (UnitMaterView unit : context.getUnitMaters()) {
			if (confidence_level >= 0 && unit.getConfidence_level() < confidence_level) {
				continue;
			}
			
			if (beauty_level >= 0 && unit.getBeauty_level() < beauty_level) {
				continue;
			}
			
			// 欺诈度，没有大小之分，在cheat_level有效(>=0)的情况下，不符合的，直接过滤掉
			if (cheat_level >= 0 && unit.getCheat_level() != cheat_level) {
				continue;
			}
			
			//低俗度过滤是反过来的，从未评定到最不低俗的顺序：0，3，2，1；
			//这里的设定是：当设置低俗度阀值为0与设置低俗度阀值为-1是一样的效果，相当于不判定低俗度
			if (vulgar_level > 0 && 
				(unit.getVulgar_level() <= 0 || unit.getVulgar_level() > vulgar_level)) {
				continue;
			}
			
			list.add(unit);
		}
		
		context.setUnitMaters(list);
	}

	/**
	 * @return the confidence_level
	 */
	public int getConfidence_level() {
		return confidence_level;
	}

	/**
	 * @param confidence_level the confidence_level to set
	 */
	public void setConfidence_level(int confidence_level) {
		this.confidence_level = confidence_level;
	}

	/**
	 * @return the beauty_level
	 */
	public int getBeauty_level() {
		return beauty_level;
	}

	/**
	 * @param beauty_level the beauty_level to set
	 */
	public void setBeauty_level(int beauty_level) {
		this.beauty_level = beauty_level;
	}

	/**
	 * @return the cheat_level
	 */
	public int getCheat_level() {
		return cheat_level;
	}

	/**
	 * @param cheat_level the cheat_level to set
	 */
	public void setCheat_level(int cheat_level) {
		this.cheat_level = cheat_level;
	}

	/**
	 * @return the vulgar_level
	 */
	public int getVulgar_level() {
		return vulgar_level;
	}

	/**
	 * @param vulgar_level the vulgar_level to set
	 */
	public void setVulgar_level(int vulgar_level) {
		this.vulgar_level = vulgar_level;
	}
}
