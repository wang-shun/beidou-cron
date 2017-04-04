package com.baidu.beidou.shrink.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.shrink.service.DelayTask;
import com.baidu.beidou.shrink.service.ShrinkApplyService;
import com.baidu.beidou.shrink.service.ShrinkApplyService.ApplyResult;
import com.baidu.beidou.shrink.service.ShrinkMonitor;
import com.baidu.beidou.shrink.service.SimpleTransProxy;
import com.baidu.beidou.shrink.service.TransTask;
import com.baidu.beidou.shrink.vo.ShrinkUnit;

/**
 * 辅助工具类
 * @author hexiufeng
 *
 */
public class ShrinkHelper {
	private static final Log LOG = LogFactory.getLog(ShrinkHelper.class);

	public static List<ShrinkUnit> load(String file, int dbindex) {
		try {
			@SuppressWarnings("unchecked")
			List<String> lineList = FileUtils
					.readLines(new File(file), "utf-8");
			if (lineList == null || lineList.size() == 0) {
				return new LinkedList<ShrinkUnit>();
			}
			List<ShrinkUnit> list = new ArrayList<ShrinkUnit>(lineList.size());
			for (String line : lineList) {
				if (StringUtils.isEmpty(line))
					continue;
				String[] items = line.split(" ");

				ShrinkUnit unit = new ShrinkUnit();
				unit.setGroupId(Long.parseLong(items[0]));
				unit.setCount(Integer.parseInt(items[1]));
				unit.setUserId(Integer.parseInt(items[2]));
				if (MultiDataSourceSupport.calculateDatabaseNo(unit.getUserId()) != dbindex) {
					LOG.info("this user don't belong to this db(groupid,userid,db):"
							+ unit.getGroupId()
							+ ","
							+ unit.getUserId()
							+ ":"
							+ dbindex);
					continue;
				}
				list.add(unit);
			}
			return list;
		} catch (IOException e) {
			LOG.error(e);
		}
		return null;
	}

	static class ValueHolder {
		ApplyResult affact;
	}

	/**
	 * 执行数据的转移，每一个执行单元放入一个事务
	 * @param apply
	 * @param transProxy
	 * @param monitor
	 * @param delay
	 * @param unitlist
	 */
	public static void shrink(final ShrinkApplyService apply,
			SimpleTransProxy transProxy, ShrinkMonitor monitor,
			DelayTask delay, List<ShrinkUnit> unitlist) {
		final ValueHolder holder = new ValueHolder();
		int count = 0;
		boolean hasOutput = false;
		LOG.info("all group count["+apply.getName()+"]:" + unitlist.size());
		for (final ShrinkUnit unit : unitlist) {
			transProxy.commitTask(new TransTask() {

				@Override
				public void execute() {
					holder.affact = apply.apply(unit);
				}

			});
			if (monitor != null) {
				monitor.accept(holder.affact);
				hasOutput = monitor.output(apply.getName());
			}
			count += holder.affact.getAffectVirtualRows();
			// 是否需要sleep一下，防止主从延迟
			if (delay.delay(count)) {
				count = 0;
			}
		}
		if (monitor != null && !hasOutput) {
			monitor.output(apply.getName(), true);
		}
	}

	// public static DelayTask factory(final long delay){
	// return new DelayTask(){
	//
	// @Override
	// public boolean delay(int count) {
	// // for keyword
	// if(count >= 5000){
	// try {
	// LOG.debug("to delay[ms]:"+delay);
	// Thread.sleep(delay);
	// } catch (InterruptedException e) {
	// // ignore
	// // 单进程运行，不会出现该问题
	// }
	// return true;
	// }
	// return false;
	// }
	//
	// };
	// }
	/**
	 * 
	 * @param delay 每执行500条需要sleep的毫秒数
	 * @param maxCount 写入maxCount多条数据时需要sleep, sleep time=(maxCount+499)/500 * delay
	 * @return
	 */
	public static DelayTask factory(final long delay, final int maxCount) {
		return new DelayTask() {

			@Override
			public boolean delay(int count) {
				if (count >= maxCount) {
					try {
						LOG.debug("to delay[ms]:" + delay);
						Thread.sleep(calSleepMS(delay,count));
					} catch (InterruptedException e) {
						// ignore
						// 单进程运行，不会出现该问题
					}
					return true;
				}
				return false;
			}

		};
	}
	
	private static long calSleepMS(long delay, long count){
		return ((count + 499)/500)*delay;
	}
}
