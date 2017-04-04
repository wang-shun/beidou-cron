/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.impl.GoogleAdxSnapshotMgrImpl.java
 * 下午2:26:42 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.context.ApplicationContext;

import com.baidu.beidou.cprounit.bo.UnitAdxSnapshotVo;
import com.baidu.beidou.cprounit.service.UnitAdxMgrOnRead;
import com.baidu.beidou.cprounit.service.google.GoogleAdxSnapshotMgr;
import com.baidu.beidou.cprounit.service.google.executor.GoogleAdxSnapshotExecutor;
import com.baidu.beidou.util.page.DataPage;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxSnapshotMgrImpl.java
 * @dateTime 2013-10-16 下午2:26:42
 */

public class GoogleAdxSnapshotMgrImpl implements GoogleAdxSnapshotMgr {

	private int userBatchNumber;

	private long userBatchSleepTime;

	private UnitAdxMgrOnRead unitAdxMgrOnRead;

	public void snapshot4Google(String updateDate, ApplicationContext context) {

		// 获取全部待截图的创意
		List<UnitAdxSnapshotVo> snapshotList = unitAdxMgrOnRead.getGoogleAdxSnapshotUnitList(updateDate);
		if (CollectionUtils.isEmpty(snapshotList)) {
			return;
		}

		int pageNo = 1;
		boolean next = false;
		// 分页“待截图创意”
		do {
			DataPage<UnitAdxSnapshotVo> page = DataPage.getByList(snapshotList, userBatchNumber, pageNo);
			List<UnitAdxSnapshotVo> innerSnapshotList = page.getRecord();
			next = page.hasNextPage();
			pageNo++;

			// 分页提交“截图”

			// 分组用户
			Map<Integer, List<UnitAdxSnapshotVo>> snapshotMap = groupSnapshotByUser(innerSnapshotList);
			if (MapUtils.isEmpty(snapshotMap)) {
				continue;
			}

			// 提交服务
			for (int userid : snapshotMap.keySet()) {
				GoogleAdxSnapshotExecutor.submit(userid, snapshotMap.get(userid), context);
			}

			try {
				TimeUnit.MILLISECONDS.sleep(userBatchSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (next);

		// 关闭服务
		GoogleAdxSnapshotExecutor.shutdown();
	}

	private Map<Integer, List<UnitAdxSnapshotVo>> groupSnapshotByUser(List<UnitAdxSnapshotVo> snapshotList) {
		if (CollectionUtils.isEmpty(snapshotList)) {
			return Collections.emptyMap();
		}
		Map<Integer, List<UnitAdxSnapshotVo>> snapshotMap = new HashMap<Integer, List<UnitAdxSnapshotVo>>(snapshotList.size());
		for (UnitAdxSnapshotVo vo : snapshotList) {
			int userid = vo.getUserid();
			if (snapshotMap.containsKey(userid)) {
				snapshotMap.get(userid).add(vo);
			} else {
				List<UnitAdxSnapshotVo> innerList = new ArrayList<UnitAdxSnapshotVo>();
				innerList.add(vo);
				snapshotMap.put(userid, innerList);
			}
		}
		return snapshotMap;
	}

	public void setUnitAdxMgrOnRead(UnitAdxMgrOnRead unitAdxMgrOnRead) {
		this.unitAdxMgrOnRead = unitAdxMgrOnRead;
	}

	public void setUserBatchNumber(int userBatchNumber) {
		this.userBatchNumber = userBatchNumber;
	}

	public void setUserBatchSleepTime(long userBatchSleepTime) {
		this.userBatchSleepTime = userBatchSleepTime;
	}

}
