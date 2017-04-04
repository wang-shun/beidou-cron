/**
 * MaterTradeRange.java 
 */
package com.baidu.beidou.bes;

/**
 * 行业id范围<br/>
 * min: included<br/>
 * max: excluded<br/>
 * 
 * @author lixukun
 * @date 2014-03-23
 */
public class MaterTradeRange {
	private int minTradeId;		// included
	private int maxTradeId;		// excluded
	
	public MaterTradeRange() {
		
	}
	
	public boolean inRange(int tradeId) {
		return tradeId >= minTradeId && tradeId < maxTradeId;
	}

	/**
	 * @return the minTradeId, included
	 */
	public int getMinTradeId() {
		return minTradeId;
	}

	/**
	 * @param minTradeId the minTradeId to set
	 */
	public void setMinTradeId(int minTradeId) {
		this.minTradeId = minTradeId;
	}

	/**
	 * @return the maxTradeId
	 */
	public int getMaxTradeId() {
		return maxTradeId;
	}

	/**
	 * @param maxTradeId the maxTradeId to set
	 */
	public void setMaxTradeId(int maxTradeId) {
		this.maxTradeId = maxTradeId;
	}
}
