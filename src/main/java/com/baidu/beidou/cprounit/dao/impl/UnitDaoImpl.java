/**
 * 
 */
package com.baidu.beidou.cprounit.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import com.baidu.beidou.auditmanager.vo.DelMaterial;
import com.baidu.beidou.cprounit.bo.CproUnit;
import com.baidu.beidou.cprounit.bo.Unit;
import com.baidu.beidou.cprounit.bo.UnitMater;
import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.cprounit.dao.rowmap.CproUnitRowMapping;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.stat.vo.AdLevelInfo;
import com.baidu.beidou.util.DateUtils;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;
import com.baidu.beidou.util.page.DataPage;
import com.baidu.beidou.util.partition.impl.PartKeyUseridImpl;
import com.baidu.beidou.util.partition.strategy.PartitionStrategy;
import com.baidu.beidou.util.string.StringUtil;

/**
 * @author zengyunfeng
 * @version 1.0.0
 */
public class UnitDaoImpl extends MultiDataSourceDaoImpl<CproUnit> implements UnitDao {
	
	private static final Log log = LogFactory.getLog(UnitDaoImpl.class);

	private PartitionStrategy strategy = null;
	private PartitionStrategy materStrategy = null;
	public static final String UNITAUDITING_NAME = "com.baidu.beidou.cprounit.bo.UnitAuditing";
	public static final String UNITMATER_TABLENAME = "cprounitmater";
	public static final String UNITMATER_PONAME = "com.baidu.beidou.cprounit.bo.UnitMater";

	public AdLevelInfo findById(int userId, Long id) {

		String sql = "SELECT gid, pid FROM beidou." + strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() + " WHERE id=?";
		sql = sql + " and " + MultiDataSourceSupport.geneateUseridStr("uid");
		
		List<Map<String, Object>> result = super.findBySql(userId, sql, new Object[] { id }, new int[] { Types.BIGINT });
		if (result == null || result.size() == 0) {
			return null;
		}
		AdLevelInfo adLevelInfo = new AdLevelInfo();
		adLevelInfo.setGroupId((Integer) result.get(0).get("gid"));
		adLevelInfo.setPlanId((Integer) result.get(0).get("pid"));
		return adLevelInfo;
	}

	/**
	 * 获得一个推广组下非删除状态的推广单元
	 * 
	 * @param groupId
	 * @param userId
	 * @return 下午03:16:24
	 */
	public List<CproUnit> findUnDeletedUnitbyGroupId(final Integer groupId, final int userId) {

		StringBuilder builder = new StringBuilder(200);

		builder.append("SELECT h.wuliaoType, h.title, h.description1, h.description2 FROM beidou.");
		builder.append(strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename());
		builder.append(" t, beidou.");
		builder.append(UNITMATER_TABLENAME);
		builder.append(strategy.getPartitions(new PartKeyUseridImpl(userId)).getId());
		builder.append(" h ");
		builder.append("where t.id = h.id and t.gid = ? and t.state != ?");
		builder.append(" and ").append(MultiDataSourceSupport.geneateUseridStr("t.uid"));

		return super.findBySql(userId, new CproUnitRowMapping(), builder.toString(), new Object[] { groupId, CproUnitConstant.UNIT_STATE_DELETE }, new int[] { Types.INTEGER, Types.INTEGER });
	}

	/**
	 * 根据一批推广组ID，获取下属的所有创意IDs
	 * 
	 * @param groupIds
	 * @return下午02:47:01
	 */
	public List<Long> getAllUnitIdsByGroupId(List<Integer> groupIds) {

		if (groupIds.size() == 0) {
			return new ArrayList<Long>(0);
		}

		String strList = StringUtil.join(",", groupIds);

		List<Long> resList = new ArrayList<Long>();

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < 8; i++) {
			builder.setLength(0);
			builder.append("select id from beidou.cprounitstate");
			builder.append(i);
			builder.append(" where gid in (");
			builder.append(strList);
			builder.append(')');
			builder.append(" and ").append(MultiDataSourceSupport.geneateUseridStr("uid"));

			List<Map<String, Object>> tmpList = super.findBySql(builder.toString(), null, null);
			for (Map<String, Object> map : tmpList) {
				Long tmpId = (Long) map.get("id");
				resList.add(tmpId);
			}
		}

		return resList;
	}

	public Unit findUnitById(Integer userId, Long unitid) {

		String unitSql = "SELECT id, gid, pid, uid, state FROM beidou." + strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() + " WHERE id=?";
		unitSql = unitSql + " and " + MultiDataSourceSupport.geneateUseridStr("uid");
		List<Map<String, Object>> result = super.findBySql(userId, unitSql, new Object[] { unitid }, new int[] { Types.BIGINT });
		if (result == null || result.size() == 0) {
			return null;
		}
		Unit unit = new Unit();
		unit.setId((Long) result.get(0).get("id"));
		unit.setState((Integer) result.get(0).get("state"));

		String matersql = "SELECT id, wid, wuliaotype, title, description1, description2," 
				+ " showUrl, targetUrl, fileSrc, height, width, syncflag, adtradeid, fwid,"
				+ " ubmcsyncflag, mcId, mcVersionId, file_src_md5 FROM beidou." 
				+ materStrategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() + " WHERE id=?";
		List<Map<String, Object>> materresult = super.findBySql(userId, matersql, new Object[] { unitid }, new int[] { Types.BIGINT });
		if (materresult == null || materresult.size() == 0) {
			return null;
		}

		UnitMater unitMater = new UnitMater();
		unitMater.setId((Long) materresult.get(0).get("id"));
		unitMater.setWid((Long) materresult.get(0).get("wid"));
		unitMater.setWuliaoType((Integer) materresult.get(0).get("wuliaotype"));
		unitMater.setTitle((String) materresult.get(0).get("title"));
		unitMater.setDescription1((String) materresult.get(0).get("description1"));
		unitMater.setDescription2((String) materresult.get(0).get("description2"));
		unitMater.setShowUrl((String) materresult.get(0).get("showUrl"));
		unitMater.setTargetUrl((String) materresult.get(0).get("targetUrl"));
		unitMater.setFileSrc((String) materresult.get(0).get("fileSrc"));
		unitMater.setHeight((Integer) materresult.get(0).get("height"));
		unitMater.setWidth((Integer) materresult.get(0).get("width"));
		unitMater.setSyncflag((Integer) materresult.get(0).get("syncflag"));
		unitMater.setAdtradeid((Integer) materresult.get(0).get("adtradeid"));
		unitMater.setFwid((Long) materresult.get(0).get("fwid"));
		unitMater.setWirelessShowUrl((String)materresult.get(0).get("wireless_show_url"));
		unitMater.setWirelessTargetUrl((String)materresult.get(0).get("wireless_target_url"));
		unitMater.setUbmcsyncflag((Integer)materresult.get(0).get("ubmcsyncflag"));
		unitMater.setMcId((Long)materresult.get(0).get("mcId"));
		unitMater.setMcVersionId((Integer)materresult.get(0).get("mcVersionId"));
		unitMater.setFileSrcMd5((String)materresult.get(0).get("file_src_md5"));
		unit.setMaterial(unitMater);

		return unit;
	}
	
	public Date findUnitChaTimeById(Integer userId, Long unitid) {

		String unitSql = "SELECT chaTime FROM beidou." + strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() + " WHERE id=?";
		unitSql = unitSql + " and " + MultiDataSourceSupport.geneateUseridStr("uid");
		List<Map<String, Object>> result = super.findBySql(userId, unitSql, new Object[] { unitid }, new int[] { Types.BIGINT });
		if (result == null || result.size() == 0) {
			return null;
		}
		Date reuslt = (Date) result.get(0).get("chaTime");
		return reuslt;
	}
	
	/**
	 * findNotSyncUnit: 获取未同步到ubmc的物料
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<UnitMaterView> findNotSyncUnit(int index, int maxMaterNum) {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from beidou.cprounitstate" + index
				+ " s join beidou.cprounitmater" + index
				+ " m on s.id=m.id where state!=2 and ubmcsyncflag=0");
		sql.append(" limit ?");
		
		return super.findBySql(new UnitMaterViewRowMapping(), sql.toString(), 
				new Object[] { maxMaterNum }, new int[] { Types.INTEGER });
	}
	
	public void updateUnitBatch(int dbIndex, List<Long> ids, List<Long> mcIds, List<Integer> mcVersionIds, Integer dbSlice) {
		StringBuilder sql = new StringBuilder();
		
		if (CollectionUtils.isEmpty(ids)
				|| CollectionUtils.isEmpty(mcIds)
				|| CollectionUtils.isEmpty(mcVersionIds)) {
			return;
		}
		int size = ids.size();
		if (size != mcIds.size()
				|| size != mcVersionIds.size()) {
			log.error("updateUnits failed[ids.size() != mcIds.size() || ids.size() != mcVersionIds.size()]");
		}
		
		Object[] params = new Object[size * 3];
		int[] paramTypes = new int[size * 3];
		for (int i = 0; i < ids.size(); i++) {
			sql.append("update beidou.cprounitmater" + dbIndex + " set mcId=?, mcVersionId=? where id=?;");
			params[i * 3 + 0] = mcIds.get(i);
			params[i * 3 + 1] = mcVersionIds.get(i);
			params[i * 3 + 2] = ids.get(i);
			
			paramTypes[i * 3 + 0] = Types.BIGINT;
			paramTypes[i * 3 + 1] = Types.INTEGER;
			paramTypes[i * 3 + 2] = Types.BIGINT;
		}
		
		int userId = MultiDataSourceSupport.DB_INDEX[dbSlice];
		super.updateBySql(userId, sql.toString(), params, paramTypes);
	}
	
	/**
	 * updateUnit: 更新物料mcId、mcVersionId
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateUnit(int index, Long id, Long mcId, Integer mcVersionId, Integer userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("update beidou.cprounitmater" + index
				+ " set mcId=?, mcVersionId=? where id=?");

		Object[] params = new Object[] { mcId, mcVersionId, id };
		int[] paramTypes = new int[]{ Types.BIGINT, Types.INTEGER, Types.BIGINT };

		super.updateBySql(userId, sql.toString(), params, paramTypes);
	}
	
	public void updateUnitSyncFlagBath(int dbIndex, List<Long> ids, List<String> fileSrcMd5s, List<Date> chaTimes, Integer dbSlice) {
		StringBuilder sql = new StringBuilder();
		
		if (CollectionUtils.isEmpty(ids)
				|| CollectionUtils.isEmpty(fileSrcMd5s)
				|| CollectionUtils.isEmpty(chaTimes)) {
			return;
		}
		int size = ids.size();
		if (size != fileSrcMd5s.size()
				|| size != chaTimes.size()) {
			log.error("updateUnits failed[ids.size() != mcIds.size() || ids.size() != mcVersionIds.size()]");
		}
		
		Object[] params = new Object[size * 3];
		int[] paramTypes = new int[size * 3];
		for (int i = 0; i < ids.size(); i++) {
			sql.append("update beidou.cprounitstate" + dbIndex
					+ " s join beidou.cprounitmater" + dbIndex
					+ " m on s.id=m.id set ubmcsyncflag=1, file_src_md5=? where s.id=? and chaTime=?;");
			params[i * 3 + 0] = fileSrcMd5s.get(i);
			params[i * 3 + 1] = ids.get(i);
			params[i * 3 + 2] = chaTimes.get(i);
			
			paramTypes[i * 3 + 0] = Types.VARCHAR;
			paramTypes[i * 3 + 1] = Types.BIGINT;
			paramTypes[i * 3 + 2] = Types.TIMESTAMP;
		}
		
		int userId = MultiDataSourceSupport.DB_INDEX[dbSlice];
		super.updateBySql(userId, sql.toString(), params, paramTypes);
	}
	
	/**
	 * updateUnitSyncFlag: 更新物料的同步标记字段，仅当与chaTime相同时进行
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateUnitSyncFlag(int index, Long id, String fileSrcMd5, Date chaTime, Integer userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("update beidou.cprounitstate" + index
				+ " s join beidou.cprounitmater" + index
				+ " m on s.id=m.id set ubmcsyncflag=1, file_src_md5=? where s.id=? and chaTime=?");

		Object[] params = new Object[] { fileSrcMd5, id, chaTime };
		int[] paramTypes = new int[]{ Types.VARCHAR, Types.BIGINT, Types.TIMESTAMP };

		super.updateBySql(userId, sql.toString(), params, paramTypes);
	}
	
	/**
	 * updateUnitMd5: 更新图片的Md5
	 * @version cpweb-567
	 * @author genglei01
	 * @date Aug 12, 2013
	 */
	public int updateUnitMd5(int index, Long id, String fileSrcMd5, Date chaTime, Integer userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("update beidou.cprounitstate" + index
				+ " s join beidou.cprounitmater" + index
				+ " m on s.id=m.id set file_src_md5=? where s.id=? and chaTime=?");

		Object[] params = new Object[] { fileSrcMd5, id, chaTime };
		int[] paramTypes = new int[]{ Types.VARCHAR, Types.BIGINT, Types.TIMESTAMP };

		return super.updateBySql(userId, sql.toString(), params, paramTypes);
	}
	
	/**
	 * findToFilterUnit: 获取待过滤的物料
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<UnitMaterView> findToFilterUnit(int dbIndex, int dbSlice) {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from beidou.cprounitstate" + dbIndex
				+ " s join beidou.cprounitmater" + dbIndex
				+ " m on s.id=m.id where state!=2 and wuliaoType=9 and chaTime>=?");
		
		Date date = DateUtils.getDate(20140404);
		
		int userId = MultiDataSourceSupport.DB_INDEX[dbSlice];
		
		return super.findBySql(userId, new UnitMaterViewRowMapping(), sql.toString(), 
				new Object[] { date }, new int[] { Types.TIMESTAMP });
	}
	
	/**
	 * findNotSyncUnit: 获取包含特殊字符的创意信息
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<UnitMaterView> findUnitWithSpecialChar(int index, Long unitId, Integer userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from beidou.cprounitstate" + index
				+ " s join beidou.cprounitmater" + index
				+ " m on s.id=m.id where state!=2 and s.id=?");
		
		return super.findBySql(userId, new UnitMaterViewRowMapping(), sql.toString(), 
				new Object[] { unitId }, new int[] { Types.BIGINT });
	}
	
	public List<Long> findIconIdByUnitId(Long unitId, Integer userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select iconId from beidou.uniticon where unitId=?");
		
		return super.findBySql(userId, new IconIdRowMapping(), sql.toString(), 
				new Object[] { unitId }, new int[] { Types.BIGINT });
	}
	
	private class IconIdRowMapping implements GenericRowMapping<Long> {
		public Long mapRow(ResultSet rs, int rowNum) throws SQLException, DataAccessException {
			return rs.getLong("iconId");
		}
	}
	
	/**
	 * Function: 更新图片的尺寸和Md5
	 *
	 * @author genglei01
	 * @date Jun 28, 2014
	 */
	public int updateUnitSizeAndMd5(int index, Long id, int width, int height, 
			String fileSrcMd5, Date chaTime, Integer userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("update beidou.cprounitstate" + index
				+ " s join beidou.cprounitmater" + index
				+ " m on s.id=m.id set width=?, height=?, file_src_md5=? where s.id=? and chaTime=?");

		Object[] params = new Object[] { width, height, fileSrcMd5, id, chaTime };
		int[] paramTypes = new int[]{ Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.BIGINT, Types.TIMESTAMP };

		return super.updateBySql(userId, sql.toString(), params, paramTypes);
	}
	
	/**
	 * updateUnitFilterSpecialChar: 更新db数据，过滤特殊字符
	 * @version cpweb-567
	 * @author genglei01
	 * @date Aug 12, 2013
	 */
	public void updateUnitFilterSpecialChar(int index, UnitMaterView info) {
		StringBuilder sql = new StringBuilder();
		Object[] params = null;
		int[] paramTypes = null;
		
		int wuliaoType = info.getWuliaoType();
		Integer userId = info.getUserId();
		if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL) {
			sql.append("update beidou.cprounitstate" + index
					+ " s join beidou.cprounitmater" + index
					+ " m on s.id=m.id set title=?, description1=?, description2=?,"
					+ " showUrl=?, targetUrl=?, wireless_show_url=?, wireless_target_url=?"
					+ " where s.id=? and chaTime=?");

			params = new Object[] { info.getTitle(), info.getDescription1(), info.getDescription2(),
					info.getShowUrl(), info.getTargetUrl(), info.getWirelessShowUrl(),
					info.getWirelessTargetUrl(), info.getId(), info.getChaTime() };
			paramTypes = new int[]{ Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
					Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BIGINT, Types.TIMESTAMP };
		} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_PICTURE
				|| wuliaoType == CproUnitConstant.MATERIAL_TYPE_FLASH) {
			sql.append("update beidou.cprounitstate" + index
					+ " s join beidou.cprounitmater" + index
					+ " m on s.id=m.id set title=?, showUrl=?, targetUrl=?, wireless_show_url=?, wireless_target_url=?"
					+ " where s.id=? and chaTime=?");

			params = new Object[] { info.getTitle(), info.getShowUrl(), info.getTargetUrl(), info.getWirelessShowUrl(),
					info.getWirelessTargetUrl(), info.getId(), info.getChaTime() };
			paramTypes = new int[]{ Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
					Types.VARCHAR, Types.VARCHAR, Types.BIGINT, Types.TIMESTAMP };
		} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON) {
			sql.append("update beidou.cprounitstate" + index
					+ " s join beidou.cprounitmater" + index
					+ " m on s.id=m.id set title=?, description1=?, description2=?,"
					+ " showUrl=?, targetUrl=?, wireless_show_url=?, wireless_target_url=?"
					+ " where s.id=? and chaTime=?");

			params = new Object[] { info.getTitle(), info.getDescription1(), info.getDescription2(),
					info.getShowUrl(), info.getTargetUrl(), info.getWirelessShowUrl(),
					info.getWirelessTargetUrl(), info.getId(), info.getChaTime() };
			paramTypes = new int[]{ Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
					Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BIGINT, Types.TIMESTAMP };
		} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_SMART_IDEA) {
			sql.append("update beidou.cprounitstate" + index
					+ " s join beidou.cprounitmater" + index
					+ " m on s.id=m.id set showUrl=?, targetUrl=?, wireless_show_url=?, wireless_target_url=?"
					+ " where s.id=? and chaTime=?");

			params = new Object[] { info.getShowUrl(), info.getTargetUrl(), info.getWirelessShowUrl(),
					info.getWirelessTargetUrl(), info.getId(), info.getChaTime() };
			paramTypes = new int[]{ Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
					Types.VARCHAR, Types.BIGINT, Types.TIMESTAMP };
		}

		super.updateBySql(userId, sql.toString(), params, paramTypes);
	}
	

	@Override
	public List<UnitMaterView> findUnitWithSpecifiedWuliaoType(int userId, List<Long> ids, List<Integer> wuliaoType) {
		if (CollectionUtils.isEmpty(ids)) {
			return null;
		}
		
		StringBuilder sqlBuilder = new StringBuilder();

		sqlBuilder.append("select m.id as id, s.chatime as chatime, s.pid as pid, s.gid as gid, title, wuliaoType, height, width, userid, new_adtradeid, targeturl, mcId, mcVersionId, confidence_level, beauty_level, cheat_level, vulgar_level from beidou.")
				  .append(materStrategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename())
				  .append(" m join ")
				  .append(strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename())
				  .append(" s ")
				  .append(" on s.id=m.id ")
				  .append(" where ");
		
		sqlBuilder.append("m.id in (");
		for (int i = 0; i < ids.size(); i++) {
			sqlBuilder.append(ids.get(i));
			if (i < ids.size() - 1) {
				sqlBuilder.append(",");
			}
		}
		sqlBuilder.append(")");
		
		if (wuliaoType != null) {
			sqlBuilder.append(" and wuliaoType in (");
			for (int i = 0; i < wuliaoType.size(); i++) {
				sqlBuilder.append(wuliaoType.get(i));
				if (i < wuliaoType.size() - 1) {
					sqlBuilder.append(",");
				}
			}
			sqlBuilder.append(")");
		}
		
		return super.findBySql(userId, new GenericRowMapping<UnitMaterView>() {

			@Override
			public UnitMaterView mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				UnitMaterView unit = new UnitMaterView();
				unit.setId(rs.getLong("id"));
				unit.setWuliaoType(rs.getInt("wuliaoType"));
				unit.setHeight(rs.getInt("height"));
				unit.setWidth(rs.getInt("width"));				
				unit.setMcId(rs.getLong("mcId"));
				unit.setMcVersionId(rs.getInt("mcVersionId"));
				unit.setUserId(rs.getInt("userid"));
				unit.setVulgar_level(rs.getInt("vulgar_level"));
				unit.setBeauty_level(rs.getInt("beauty_level"));
				unit.setConfidence_level(rs.getInt("confidence_level"));
				unit.setCheat_level(rs.getInt("cheat_level"));
				unit.setNewAdTradeId(rs.getInt("new_adtradeid"));
				unit.setChaTime(rs.getDate("chatime"));
				unit.setTargetUrl(rs.getString("targeturl"));
				unit.setTitle(rs.getString("title"));
				unit.setGid(rs.getInt("gid"));
				unit.setPid(rs.getInt("pid"));
				
				return unit;
			}
		}, sqlBuilder.toString(), null, null);
	}
	

	@Override
	public List<Long> findUnitIdsByPlanIds(int userId, List<Integer> planIds) {
		if (CollectionUtils.isEmpty(planIds)) {
			return null;
		}
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select id from beidou.").append(strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename()).append(" where ");
		sqlBuilder.append("pid in (");
		for (int i = 0; i < planIds.size(); i++) {
			sqlBuilder.append(planIds.get(i));
			if (i < planIds.size() - 1) {
				sqlBuilder.append(",");
			}
		}
		sqlBuilder.append(")");
		sqlBuilder.append(" and state=0");
		
		return super.findBySql(userId, new GenericRowMapping<Long>(){

			@Override
			public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong(1);
			}
		
		}, sqlBuilder.toString(), null, null);
	}
	
	@Override
	public DataPage<Long> findUnitIdsByPlanIds(int userId, List<Integer> planIds,
			int pageSize, int pageNo) {
		if (CollectionUtils.isEmpty(planIds)) {
			return null;
		}
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select id from beidou.").append(strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename()).append(" where ");
		sqlBuilder.append("pid in (");
		for (int i = 0; i < planIds.size(); i++) {
			sqlBuilder.append(planIds.get(i));
			if (i < planIds.size() - 1) {
				sqlBuilder.append(",");
			}
		}
		sqlBuilder.append(")");
		sqlBuilder.append(" and state=0 order by id");
		
		return super.queryPage(new GenericRowMapping<Long>(){

			@Override
			public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getLong(1);
			}
		
		}, userId, sqlBuilder.toString(), pageNo, pageSize);
	}
	
	
	
	private class UnitMaterViewRowMapping implements GenericRowMapping<UnitMaterView> {
		public UnitMaterView mapRow(ResultSet rs, int rowNum) throws SQLException, DataAccessException {
			UnitMaterView unit = new UnitMaterView();
			unit.setId(rs.getLong("s.id"));
			unit.setUserId(rs.getInt("s.uid"));
			unit.setChaTime(rs.getTimestamp("chaTime"));
			
			unit.setWid(rs.getLong("wid"));
			unit.setWuliaoType(rs.getInt("wuliaoType"));
			
			unit.setTitle(rs.getString("title"));
			unit.setDescription1(rs.getString("description1"));
			unit.setDescription2(rs.getString("description2"));
			unit.setShowUrl(rs.getString("showUrl"));
			unit.setTargetUrl(rs.getString("targetUrl"));
			unit.setWirelessShowUrl(rs.getString("wireless_show_url"));
			unit.setWirelessTargetUrl(rs.getString("wireless_target_url"));
			
			unit.setFileSrc(rs.getString("fileSrc"));
			unit.setFileSrcMd5(rs.getString("file_src_md5"));
			unit.setHeight(rs.getInt("height"));
			unit.setWidth(rs.getInt("width"));
			
			unit.setUbmcsyncflag(rs.getInt("ubmcsyncflag"));
			unit.setMcId(rs.getLong("mcId"));
			unit.setMcVersionId(rs.getInt("mcVersionId"));
			
			return unit;
		}
	}

	public PartitionStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(PartitionStrategy strategy) {
		this.strategy = strategy;
	}

	public PartitionStrategy getMaterStrategy() {
		return materStrategy;
	}

	public void setMaterStrategy(PartitionStrategy materStrategy) {
		this.materStrategy = materStrategy;
	}

    @Override
    public void deleteMater(DelMaterial delMaterial) {
        Integer userId = delMaterial.getUserId();
        StringBuilder sql = new StringBuilder();
        sql.append("delete from beidou.delmater where mcId = ").append(delMaterial.getMcId())
                .append(" and mcVersionId = ").append(delMaterial.getMcVersionId());

        try {
            super.updateBySql(userId, sql.toString(), null, null);
        } catch (Exception e) {
            log.warn("delmater failed: ", e);

            // 如果删除失败，则遍历八库删除
            this.appendUserIdRouting(sql, "userid");
            super.updateBySql(sql.toString(), new Object[0], new int[0]);
        }
    }
}
