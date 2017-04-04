/**
 * 2010-3-16 下午05:08:03
 */
package com.baidu.beidou.auditmanager.service;

import java.io.IOException;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public interface AuditMonitorMgr {

	/**
	 * 跟进审核历史生成监控文件output
	 * @author zengyunfeng
	 * @param output 输出的文件名
	 * @param monitorFile 监控的拒绝理由
	 * @throws IOException 
	 */
	public boolean generateMonitorFile(final String output, final String monitorFile) throws IOException;
}
