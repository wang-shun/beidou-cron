package com.baidu.beidou.tool.service;

import java.util.List;

import com.baidu.beidou.tool.bo.SnapShotFile;
import com.baidu.beidou.tool.vo.OrderListRow;

public interface SnapShotMgr {
	
	/**
	 * 删除标记为删除文件，更新删除时间
	 * @return
	 */
	public boolean deleteFiles();
	
	/**
	 * 把超过截图周期，状态为截图中的标记为失败，并发出失败邮件
	 * @return
	 */
	public boolean sendFailedMails();
	
	/**
	 * 得到需要导出的订阅
	 * @return
	 */
	public List<OrderListRow> getOutputOrders();
	
	/**
	 * 处理一个截屏任务
	 * @param orderid
	 * @param adid
	 * @param site
	 * @return
	 */
	public boolean dealSnapShot(Integer orderid, String tu, String site, String ads, String day);

	/**
	 * 向window发起截图请求，返回图片的相对地址
	 * @param tu
	 * @param orderid
	 * @param site
	 * @return
	 */
	public String sendSnapRequest(String tu, String orderid, String site);
	
	
	public boolean saveResult(SnapShotFile snapFile);

	public boolean downloadIndex(String day);

	public String mergeLine(String line);
	
	/**
	 * 检索端需要截图的冗余数量
	 */
	public int getSnapOrderCount();
}
