/**
 * beidou-cron-640#com.baidu.beidou.cprounit.dao.impl.UnitAdxDaoImpl.java
 * 下午3:29:10 created by kanghongwei
 */
package com.baidu.beidou.cprounit.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.cprounit.bo.UnitAdx;
import com.baidu.beidou.cprounit.bo.UnitAdxGoogleApiVo;
import com.baidu.beidou.cprounit.bo.UnitAdxSnapshotVo;
import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.cprounit.dao.UnitAdxDao;
import com.baidu.beidou.cprounit.dao.rowmap.UnitAdxGoogleApiRowMapping;
import com.baidu.beidou.cprounit.dao.rowmap.UnitAdxSnapshotRowMapping;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;
import com.baidu.beidou.util.partition.impl.PartKeyUseridImpl;
import com.baidu.beidou.util.partition.strategy.PartitionStrategy;
import com.baidu.beidou.util.string.StringUtil;

/**
 * 
 * @author kanghongwei
 * @fileName UnitAdxDaoImpl.java
 * @dateTime 2013-10-16 下午3:29:10
 */

public class UnitAdxDaoImpl extends MultiDataSourceDaoImpl<UnitAdx> implements UnitAdxDao {
//	private static final Log log = LogFactory.getLog(UnitAdxDao.class);
	private PartitionStrategy strategy;
	
	final static int GOOGLE_COMPANY_TAG = 1;
	final static int IFENG_COMPANY_TAG = 2;
	final static int SOHU_COMPANY_TAG = 4;
	final static int TENCENT_COMPANY_TAG = 8;

	public List<UnitAdxSnapshotVo> getGoogleAdxSnapshotUnitList(String updateDate) {

		if (StringUtil.isEmpty(updateDate)) {
			return Collections.emptyList();
		}

		List<UnitAdxSnapshotVo> result = new ArrayList<UnitAdxSnapshotVo>();

		StringBuilder sql = new StringBuilder();

		for (int i = 0; i < TAB_UNIT_SLICE; i++) {
			sql.setLength(0);
			sql.append("select ");
			sql.append("	 m.userid as userid,m.id as id,m.wuliaoType as wuliaoType,m.width as width,m.height as height,m.mcId as mcId,m.mcVersionId as mcVersionId ");
			sql.append("from ");
			sql.append("	 beidou.cprounitadx" + i + " as a, beidou.cprounitmater" + i + " as m,beidou.cprounitstate" + i + " as s ");
			sql.append("where ");
			sql.append("	 a.adid = m.id and ");
			sql.append("     a.adid = s.id and ");
			sql.append("     s.state = 0 and ");
			sql.append("     m.wuliaoType = 3 and ");
			sql.append("     a.adx_type & 1 = 1 and ");
			sql.append("     ( (a.google_snapshot = 1) or  (a.google_snapshot in (2,3) and DATE_FORMAT(s.chaTime,'%Y-%m-%d') >= ? ) ) and ");
			sql.append(MultiDataSourceSupport.geneateUseridStr("a.userid"));

			List<UnitAdxSnapshotVo> sliceResult = super.findBySql(new UnitAdxSnapshotRowMapping(), sql.toString(), new Object[] { updateDate }, new int[] { Types.VARCHAR });

			result.addAll(sliceResult);
		}
		return result;
	}

	public List<UnitAdxGoogleApiVo> getGoogleAdxApiUnitList(String updateDate) {

		if (StringUtil.isEmpty(updateDate)) {
			return Collections.emptyList();
		}

		List<UnitAdxGoogleApiVo> result = new ArrayList<UnitAdxGoogleApiVo>();

		StringBuilder sql = new StringBuilder();
		long stateValue = calcStateValue(AUDIT_INITIAL, GOOGLE_COMPANY_TAG);
		long mask = calcStateValue(3, GOOGLE_COMPANY_TAG);	// 0000..11..000状态，与运算后，可获取相应位的值

		for (int i = 0; i < TAB_UNIT_SLICE; i++) {
			// sql: 取创意已经发生变更的和audit_state_0的相应状态位为初始化状态的
			sql.setLength(0);
			sql.append("select ");
			sql.append("	 m.userid as userid,m.id as id,m.width as width,m.height as height,m.targetUrl as targetUrl ");
			sql.append("from ");
			sql.append("	 beidou.cprounitadx" + i + " as a, beidou.cprounitmater" + i + " as m,beidou.cprounitstate" + i + " as s ");
			sql.append("where ");
			sql.append("	 a.adid = m.id and ");
			sql.append("     a.adid = s.id and ");
			sql.append("     s.state = 0 and ");
			sql.append("     a.adx_type & 1 = 1 and ");
			sql.append("     ( (a.audit_state_0&").append(mask).append("=").append(stateValue).append(")");
			sql.append("       or  ");
			sql.append("       DATE_FORMAT(s.chaTime,'%Y-%m-%d') >= ? ) and ");
			sql.append(MultiDataSourceSupport.geneateUseridStr("a.userid"));

			List<UnitAdxGoogleApiVo> sliceResult = super.findBySql(new UnitAdxGoogleApiRowMapping(), sql.toString(), new Object[] { updateDate }, new int[] { Types.VARCHAR });

			result.addAll(sliceResult);
		}

		return result;
	}

	public Map<Long, Integer> getAdUserRelation() {

		Map<Long, Integer> result = new HashMap<Long, Integer>();

		StringBuilder sql = new StringBuilder();

		for (int i = 0; i < TAB_UNIT_SLICE; i++) {
			sql.setLength(0);
			sql.append("select ");
			sql.append("	 a.adid as adid, a.userid as userid ");
			sql.append("from ");
			sql.append("	 beidou.cprounitadx" + i + " as a ");
			sql.append("where ");
			sql.append("     a.adx_type & ").append(GOOGLE_COMPANY_TAG).append(" = 1 and ");
			sql.append("     and   ");
			sql.append(MultiDataSourceSupport.geneateUseridStr("a.userid"));

			List<Map<String, Object>> sliceResult = super.findBySql(sql.toString(), null, null);

			if (CollectionUtils.isNotEmpty(sliceResult)) {
				for (Map<String, Object> map : sliceResult) {
					long adid = Long.valueOf(map.get("adid").toString());
					int userid = Integer.valueOf(map.get("userid").toString());
					result.put(adid, userid);
				}
			}
		}

		return result;
	}

	public void updateGoogleAdxSnapshotState(int userId, List<Long> adIdList, int snapshotState) {
		if (CollectionUtils.isEmpty(adIdList)) {
			return;
		}
		String adIdInLine = StringUtil.join(",", adIdList);

		StringBuilder sql = new StringBuilder();
		sql.append("update ");
		sql.append("       beidou." + strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() + " ");
		sql.append("set  ");
		sql.append("       google_snapshot =? ");
		sql.append("where ");
		sql.append("       adid in ( ");
		sql.append(adIdInLine);
		sql.append(" )");

		super.updateBySql(userId, sql.toString(), new Object[] { snapshotState }, new int[] { Types.INTEGER });
	}

	public void updateGoogleAdxAPiState(int userId, List<Long> adIdList, int googleAuditState) {
		if (CollectionUtils.isEmpty(adIdList)) {
			return;
		}
		String adIdInLine = StringUtil.join(",", adIdList);

		StringBuilder sql = new StringBuilder();
		sql.append("update ");
		sql.append("       beidou." + strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() + " ");
		sql.append("set  ");
		sql.append("       audit_state_0=");
		
		if (googleAuditState == AUDIT_APPROVED) {
			// 如果审核通过，需要标记audit_adx_type
			sql.append(getStateFieldExpression("audit_state_0", AUDIT_APPROVED, GOOGLE_COMPANY_TAG)).append(", ");
			sql.append("audit_adx_type=audit_adx_type|").append(GOOGLE_COMPANY_TAG).append(" ");
		} else {
			sql.append(getStateFieldExpression("audit_state_0", googleAuditState, GOOGLE_COMPANY_TAG)).append(" ");
		}
		sql.append("where ");
		sql.append("       adid in ( ");
		sql.append(adIdInLine);
		sql.append(" )");

		super.updateBySql(userId, sql.toString(), new Object[] { googleAuditState }, new int[] { Types.INTEGER });
	}
	
	@Override
	public void updateUnitAdxState(int userId, long adId, long companyTag, Map<String, String> valuePairs) {
		if (valuePairs == null || valuePairs.size() == 0) {
			return;
		}
		boolean directApproved = false; // 是否不需要审核直接通过，若为真，会直接写入audit_adx_type
		
		StringBuilder sql = new StringBuilder();
		// 使用replace into，避免并发时出现写入错误O
		sql.append("replace into ");
		sql.append("       beidou." + strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() + " ");
		// 默认写入的字段：adid,userid,adx_type,mtime
		sql.append("(adid,userid,adx_type,");
		
		for (Map.Entry<String, String> entry : valuePairs.entrySet()) {
			sql.append(entry.getKey()).append(",");
			if (entry.getKey().startsWith("audit_state") && entry.getValue().equals("$AUDIT_APPROVED")) {
				directApproved = true;
			}
		}
		if (directApproved) {
			sql.append("audit_adx_type,");
		}
		sql.append("mtime").append(") values (");
		sql.append(adId).append(",").append(userId).append(",").append("adx_type|").append(companyTag).append(",");
		
		for (Map.Entry<String, String> entry : valuePairs.entrySet()) {
			sql.append(getFieldValue(entry.getKey(), entry.getValue(), companyTag)).append(",");
		}
		
		if (directApproved) {
			sql.append("audit_adx_type|").append(companyTag).append(",");
		}
		sql.append("now())");
		
		super.updateBySql(userId, sql.toString(), null, null);
	}
	
	@Override
	public void setUnitAdxInvalid(int userId, long adId, long companyTag) {
		StringBuilder sql = new StringBuilder();
		
		long complement = ~companyTag; // 反码 1111...0...1111
		
		// 使用replace into，避免并发时出现写入错误O
		sql.append("update ");
		sql.append("       beidou." + strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() + " ");
		sql.append("set ")
		   .append("       audit_state_0=").append(getStateFieldExpression("audit_state_0", AUDIT_DISAPPROVED, companyTag)).append(",")
		   .append("       audit_adx_type=audit_adx_type&").append(complement).append(",")
		   .append("       mtime=now() ");
		
		sql.append("where adid=").append(adId);
		
		super.updateBySql(userId, sql.toString(), null, null);
	}

	@Override
	public List<Long> getUnitAdxUnderAudit(int userId, long companyTag) {
		StringBuilder sql = new StringBuilder();
		
		long mask = calcStateValue(3, companyTag);
		long stateValue = calcStateValue(AUDIT_NOT_CHECKED, companyTag);
		
		sql.append("select adid ")
		   .append("    beidou.").append(strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename()).append(" ")
		   .append("where ")
		   .append("    audit_state_0&").append(mask).append("=").append(stateValue)
		   .append("    and    ")
		   .append("    adx_type&").append(companyTag).append(">0");
		
		return super.findBySql(userId, new GenericRowMapping<Long>(){

			@Override
			public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong(1);
			}
		
		}, sql.toString(), null, null);
		
	}
	
	@Override
	public void updateAdxState(int userId, long adId, int auditState, long companyTag) {
		StringBuilder sql = new StringBuilder();
		sql.append("update ");
		sql.append("       beidou." + strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() + " ");
		sql.append("set  ");
		sql.append("       audit_state_0=");
		
		if (auditState == AUDIT_APPROVED) {
			// 如果审核通过，需要标记audit_adx_type
			sql.append(getStateFieldExpression("audit_state_0", AUDIT_APPROVED, companyTag)).append(", ");
			sql.append("audit_adx_type=audit_adx_type|").append(companyTag).append(" ");
		} else {
			sql.append(getStateFieldExpression("audit_state_0", auditState, companyTag)).append(" ");
		}
		sql.append("where ");
		sql.append("       adid=");
		sql.append(adId);

		super.updateBySql(userId, sql.toString(), null, null);
	}

	public PartitionStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(PartitionStrategy strategy) {
		this.strategy = strategy;
	}
	
	/**
	 * 获取置位sql表达式，适用于赋值，经过运算让field相应位的值转为state
	 * 
	 * @param field
	 * @param state
	 * @param companyTag
	 * @return
	 */
	private String getStateFieldExpression(String field, int state, long companyTag) {
		StringBuilder sb = new StringBuilder();
		if (state == AUDIT_APPROVED) {
			sb.append(field).append("&").append(calcStateValue(AUDIT_APPROVED, companyTag));
		} else {
			// 先&将相应位置0，然后按位|
			sb.append(field).append("&").append(calcStateValue(0, companyTag)).append("|").append(calcStateValue(state, companyTag));
		}
		
		return sb.toString();
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 * @param companyTag
	 * @return 数字和字符串都采用''方式返回
	 */
	private String getFieldValue(String key, String value, long companyTag) {
		if (key == null || value == null) {
			return null;
		}
		
		if (value.equals("$AUDIT_DISAPPROVED")) {
			return getStateFieldExpression(key, AUDIT_DISAPPROVED, companyTag);
		}
		
		if (value.equals("$AUDIT_NOT_CHECKED")) {
			return getStateFieldExpression(key, AUDIT_NOT_CHECKED, companyTag);
		}
		
		if (value.equals("$AUDIT_INITIAL")) {
			return getStateFieldExpression(key, AUDIT_INITIAL, companyTag);
		}
		
		if (value.equals("$AUDIT_APPROVED")) {
			return getStateFieldExpression(key, AUDIT_APPROVED, companyTag);
		}
		
		return String.format("'%s'", value);
	}
	
	/**
	 * 计算mask码
	 * 
	 * @param state 0,1,2,3四个值
	 * @param companyTag 1,2,4...
	 * @return
	 */
	private long calcStateValue(int state, long companyTag) {
		if (state < 0 || state > 3) {
			throw new RuntimeException("invalid state" + state);
		}
		
		// 大于0直接移位
		if (state > 0) {
			long ret = state;
			int bitCount = 0;
			do {
				companyTag /= 2;
				bitCount++;
			} while (companyTag > 0);
			
			return ret << ((bitCount - 1) * 2);
		}
		
		// state=0需要使用反码，生成1111..00..1111这样的mask码
		long ret = 0;
		long initial = ~0; // all 1
		long mask1 = initial << (companyTag * 2); // 1111...00...
		if (companyTag > 1) {
			long mask2 = ~(initial << ((companyTag - 1) * 2));
			ret = mask1 | mask2;
		} else {
			ret = mask1;
		}
		
		return ret;
	}
}
