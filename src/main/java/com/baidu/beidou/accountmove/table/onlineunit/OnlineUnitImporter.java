package com.baidu.beidou.accountmove.table.onlineunit;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.accountmove.dao.DataAccessService;
import com.baidu.beidou.accountmove.dao.IDService;
import com.baidu.beidou.accountmove.dao.UbmcMaterial;
import com.baidu.beidou.accountmove.keymap.KeyMapper;
import com.baidu.beidou.accountmove.table.TableImporter;
import com.baidu.beidou.accountmove.table.TableSchemaInfo;
import com.baidu.beidou.accountmove.util.FileExporter;
import com.baidu.beidou.accountmove.util.StringUtil;
import com.baidu.beidou.accountmove.util.TableNameUtil;

public class OnlineUnitImporter implements TableImporter{

	private static final Log logger = LogFactory.getLog(OnlineUnitImporter.class);
	
	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	private IDService idService;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> groupitList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(groupitList)) {
			return;
		}
		
		for (List<String> groupit : groupitList) {
			String oldUnitid = groupit.get(StringUtil.getIndex("id", getColumnNameArray()));
			String oldGroupid = groupit.get(StringUtil.getIndex("groupid", getColumnNameArray()));
			String oldPlanid = groupit.get(StringUtil.getIndex("planid", getColumnNameArray())); 
			String oldUserid = groupit.get(StringUtil.getIndex("userid", getColumnNameArray()));
			
			String oldMcId = groupit.get(StringUtil.getIndex("mc_id", getColumnNameArray()));
			String oldMcVersion = groupit.get(StringUtil.getIndex("mc_version_id", getColumnNameArray()));
			
			// query unitid, if this unit is a shadow, will not in online_unit map, will pass through; 
			if (keyMapper.getMappedKey(KeyMapper.ONLINE_UNITID, oldUnitid, userId) == null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			groupit.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
			String newPlanId = keyMapper.getMappedKey(KeyMapper.PLANID,
					oldPlanid, Integer.valueOf(oldUserid));
			groupit.set(StringUtil.getIndex("planid", getColumnNameArray()), newPlanId);
			
			String newGroupId = keyMapper.getMappedKey(KeyMapper.GROUPID,
					oldGroupid, Integer.valueOf(oldUserid));
			groupit.set(StringUtil.getIndex("groupid", getColumnNameArray()), newGroupId);
			
			UbmcMaterial ubmc = idService.generateUbmcMaterial(userId, Long.valueOf(oldMcId), Integer.valueOf(oldMcVersion));
			groupit.set(StringUtil.getIndex("mc_id", getColumnNameArray()), String.valueOf(ubmc.getMcId()));
			groupit.set(StringUtil.getIndex("mc_version_id", getColumnNameArray()), String.valueOf(ubmc.getVersionid()));
			
			
			// itid get from sequence
			String newGenerateUnitId = keyMapper.getMappedKey(KeyMapper.UNITID, oldUnitid, userId);
			groupit.set(StringUtil.getIndex("id", getColumnNameArray()), newGenerateUnitId);
			
			String[] colNames = Arrays.copyOfRange(getColumnNameArray(), 0,
					getColumnNameArray().length);
			// insert into beidou.cproplan (planname,...,plan_type)values(?,...,?);
			String sql = "insert into "
					+ TableNameUtil.getActualTableName(getTableName(), Integer.parseInt(newUserId)) + " ("
					+ StringUtil.getStringFromArray(colNames, ",")
					+ ") values("
					+ StringUtil.getRepetitionString("?", ",", colNames.length)
					+ ");";
			Object[] params = new Object[colNames.length];
			for (int i = 0; i < colNames.length; i++) {
				params[i] = groupit.get(i);
			}
			String newUnitId = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newUnitId == null) {
				logger.error("add unit mater failed, old unitid is: " + oldUnitid);
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.ONLINE_UNIT;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.online_unit;
	}
	
	public FileExporter getFexporter() {
		return fexporter;
	}

	public void setFexporter(FileExporter fexporter) {
		this.fexporter = fexporter;
	}

	public DataAccessService getDataAccessService() {
		return dataAccessService;
	}

	public void setDataAccessService(DataAccessService dataAccessService) {
		this.dataAccessService = dataAccessService;
	}

	public KeyMapper getKeyMapper() {
		return keyMapper;
	}

	public void setKeyMapper(KeyMapper keyMapper) {
		this.keyMapper = keyMapper;
	}

	public IDService getIdService() {
		return idService;
	}

	public void setIdService(IDService idService) {
		this.idService = idService;
	}

}
