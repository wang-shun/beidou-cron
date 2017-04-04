package com.baidu.beidou.unionsite.bo;

/**
 * @author zhuqian
 *
 */
@Deprecated
public class SiteStatExtBo extends SiteStatBo {

	private static final long serialVersionUID = 8358048614367674405L;
	
	private int dispType;		//站点支持的广告展现类型，二进制编码格式，从低位到高位依次是：固定、悬浮
	
	//纯悬浮流量的统计信息
	private long flowRetrieve = 0;	//检索量
	private long flowAds = 0;		//检索广告条数
	private int flowClicks = 0;		//点击
	private int flowCost = 0;		//消费
	
	public SiteStatExtBo(){
		//default constructor
	}
	
	public String toString(){
		return "'" + super.getCntn() + " " + super.getDomain() +"'";
	}
	
	public SiteStatExtBo(SiteStatBo bo){
		super.setCntn(bo.getCntn());
		super.setDomain(bo.getDomain());
		super.setRetrieve(bo.getRetrieve());
		super.setAds(bo.getAds());
		super.setWuliao(bo.getWuliao());
		super.setUnique_ip(bo.getUnique_ip());
		super.setUnique_cookie(bo.getUnique_cookie());
		super.setClicks(bo.getClicks());
		super.setCost(bo.getCost());
		super.setSizeFlow(bo.getSizeFlow());
	}
	
	/**
	 * @return the dispType
	 */
	public int getDispType() {
		return dispType;
	}

	/**
	 * @param dispType the dispType to set
	 */
	public void setDispType(int dispType) {
		this.dispType = dispType;
	}

	/**
	 * @return the flowRetrieve
	 */
	public long getFlowRetrieve() {
		return flowRetrieve;
	}
	/**
	 * @param flowRetrieve the flowRetrieve to set
	 */
	public void setFlowRetrieve(long flowRetrieve) {
		this.flowRetrieve = flowRetrieve;
	}
	/**
	 * @return the flowAds
	 */
	public long getFlowAds() {
		return flowAds;
	}
	/**
	 * @param flowAds the flowAds to set
	 */
	public void setFlowAds(long flowAds) {
		this.flowAds = flowAds;
	}
	/**
	 * @return the flowClicks
	 */
	public int getFlowClicks() {
		return flowClicks;
	}
	/**
	 * @param flowClicks the flowClicks to set
	 */
	public void setFlowClicks(int flowClicks) {
		this.flowClicks = flowClicks;
	}
	/**
	 * @return the flowCost
	 */
	public int getFlowCost() {
		return flowCost;
	}
	/**
	 * @param flowCost the flowCost to set
	 */
	public void setFlowCost(int flowCost) {
		this.flowCost = flowCost;
	}
	
}
