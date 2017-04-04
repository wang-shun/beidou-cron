/**
 * 
 */
package com.baidu.beidou.cache.bo;

import java.math.BigDecimal;

import com.baidu.beidou.olap.constant.Constants;
import com.baidu.unbiz.olap.annotation.OlapColumn;
import com.baidu.unbiz.olap.obj.BaseItem;
import com.baidu.unbiz.olap.util.NumberUtil;

public class StatInfo extends BaseItem {
	private static final long serialVersionUID = -2759467000568708939L;

	// 属性名与Constants中对应值相同
	/**
	 * 基础统计数据
	 */
	@OlapColumn(Constants.COLUMN.USERID)
	protected String userid;// 展现

	@OlapColumn(Constants.COLUMN.SRCHS)
	protected long srchs;// 展现

	@OlapColumn(Constants.COLUMN.CLKS)
	protected long clks;// 点击

	@OlapColumn(Constants.COLUMN.COST)
	protected double cost;// 单位分

	@OlapColumn(Constants.COLUMN.CTR)
	protected BigDecimal ctr;// 点击率

	@OlapColumn(Constants.COLUMN.ACP)
	protected BigDecimal acp;// 平均点击价格

	@OlapColumn(Constants.COLUMN.CPM)
	protected BigDecimal cpm;// 千次展现成本

	@Override
	public void afterAssemble(int timeUnit) {
		super.afterAssemble(timeUnit);
		this.buildCostField();
	}

	private void buildCostField() {
		// 注意：由于从DailyUserStats表中取出的是分,且为千次，因此要/1000,单位仍然是分
		this.cost = this.cost / (1000.0d);
	}

	public StatInfo() {
	};

	public StatInfo(Integer useid, long srchs, long clks, long cost) {
		this.userid = useid.toString();
		this.srchs = srchs;
		this.clks = clks;
		this.cost = (double)cost;
	}

	public StatInfo(long srchs, long clks, long cost) {
		this.srchs = srchs;
		this.clks = clks;
		this.cost = (double)cost;
	}

	public StatInfo merge(long srchs, long clks, long cost) {
		this.srchs += srchs;
		this.clks += clks;
		this.cost += (double)cost;

		return this;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public BigDecimal getAcp() {
		return acp;
	}

	public void setAcp(double acp) {
		this.acp = NumberUtil.doubleToBigDecimal(acp, 2);
	}

	public long getClks() {
		return clks;
	}

	public void setClks(long clks) {
		this.clks = clks;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public BigDecimal getCpm() {
		return cpm;
	}

	public void setCpm(double cpm) {
		this.cpm = NumberUtil.doubleToBigDecimal(cpm, 2);
	}

	public BigDecimal getCtr() {
		return ctr;
	}

	public void setCtr(double ctr) {
		this.ctr = NumberUtil.doubleToBigDecimal(ctr);
	}

	public long getSrchs() {
		return srchs;
	}

	public void setSrchs(long srchs) {
		this.srchs = srchs;
	}

	public void setCtr(BigDecimal ctr) {
		this.ctr = ctr;
	}

	public void setAcp(BigDecimal acp) {
		this.acp = acp;
	}

	public void setCpm(BigDecimal cpm) {
		this.cpm = cpm;
	}

}
