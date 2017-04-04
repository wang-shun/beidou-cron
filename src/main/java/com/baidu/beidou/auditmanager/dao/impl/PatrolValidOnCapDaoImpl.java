/**
 * beidou-cron-trunk#com.baidu.beidou.auditmanager.dao.impl.PatrolValidOnCapDaoImpl.java
 * 上午12:26:23 created by kanghongwei
 */
package com.baidu.beidou.auditmanager.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.auditmanager.constant.AuditConstant;
import com.baidu.beidou.auditmanager.dao.PatrolValidOnCapDao;
import com.baidu.beidou.auditmanager.vo.Reason;
import com.baidu.beidou.util.JdbcTypeCast;
import com.baidu.beidou.util.dao.GenericDaoImpl;

/**
 * 
 * @author kanghongwei
 */

public class PatrolValidOnCapDaoImpl extends GenericDaoImpl implements PatrolValidOnCapDao {

	private static final Log log = LogFactory.getLog(PatrolValidOnCapDaoImpl.class);

	public void loadRefuseReasonMap() {
		List<Map<String, Object>> result = super.findBySql("select id, manager,client,type, isdeleted from beidoucap.refusereason", new Object[0], new int[0]);

		if (CollectionUtils.isEmpty(result)) {
			log.error("Unable to load refuse reason map!");
			return;
		}

		Map<Integer, Reason> reasonMap = new HashMap<Integer, Reason>(result.size());
		for (Map<String, Object> row : result) {
			Reason reason = new Reason();

			int id = JdbcTypeCast.CastToInt(row.get("id"));
			reason.setId(id);
			reason.setManager((String) row.get("manager"));
			reason.setClient((String) row.get("client"));
			reason.setType(JdbcTypeCast.CastToInt(row.get("type")));
			reason.setIsDeleted(JdbcTypeCast.CastToInt(row.get("isdeleted")));

			reasonMap.put(id, reason);
		}

		// 更新常量
		AuditConstant.setReasonMap(reasonMap);
	}

}
