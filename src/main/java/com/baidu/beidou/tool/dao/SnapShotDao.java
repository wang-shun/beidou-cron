package com.baidu.beidou.tool.dao;

import java.util.List;

import com.baidu.beidou.tool.bo.SnapShot;

/**
 * @author chenlu
 *
 */
public interface SnapShotDao{
	
	/**
	 * 把超过制定时间，且状态未成功的记录更新为失败状态，并返回
	 * @param days
	 * @return
	 */
	public List<SnapShot> setExpireFailedStatus(int days);

	
	/**
	 * 得到需要导出的订阅，返回结果按照优先级, snapid排序
	 * @return
	 */
	public List<SnapShot> getOutputSnap();
	
	public boolean upSnapPriority(List<Integer> ids);
	
	public SnapShot getSnapShot(int id);
	
	public boolean saveStatus(int status, int id);
	
	public boolean deleteFirst(int userid);
	
	public int getSnapCount(int userid, int status);
	
	public boolean resetSnapPriority(List<Integer> ids);

}
