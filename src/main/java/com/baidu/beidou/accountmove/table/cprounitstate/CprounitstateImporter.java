package com.baidu.beidou.accountmove.table.cprounitstate;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.baidu.beidou.accountmove.dao.DataAccessService;
import com.baidu.beidou.accountmove.dao.IDService;
import com.baidu.beidou.accountmove.keymap.KeyMapper;
import com.baidu.beidou.accountmove.table.TableImporter;
import com.baidu.beidou.accountmove.table.TableSchemaInfo;
import com.baidu.beidou.accountmove.util.FileExporter;
import com.baidu.beidou.accountmove.util.StringUtil;
import com.baidu.beidou.accountmove.util.TableNameUtil;

public class CprounitstateImporter implements TableImporter{

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
			String oldGroupid = groupit.get(StringUtil.getIndex("gid", getColumnNameArray()));
			String oldPlanid = groupit.get(StringUtil.getIndex("pid", getColumnNameArray())); 
			String oldUserid = groupit.get(StringUtil.getIndex("uid", getColumnNameArray()));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.UNITID, oldUnitid, userId) != null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			groupit.set(StringUtil.getIndex("uid", getColumnNameArray()), newUserId);
			
			String newPlanId = keyMapper.getMappedKey(KeyMapper.PLANID,
					oldPlanid, Integer.valueOf(oldUserid));
			groupit.set(StringUtil.getIndex("pid", getColumnNameArray()), newPlanId);
			
			String newGroupId = keyMapper.getMappedKey(KeyMapper.GROUPID,
					oldGroupid, Integer.valueOf(oldUserid));
			groupit.set(StringUtil.getIndex("gid", getColumnNameArray()), newGroupId);
			
			// unitid get from sequence
			long newGenerateUnitId = idService.generateKeys(Integer.valueOf(newUserId), "cprounit", 1);
			groupit.set(StringUtil.getIndex("id", getColumnNameArray()), String.valueOf(newGenerateUnitId));
			
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
			
			if(StringUtils.isEmpty(groupit.get(StringUtil.getIndex("audittime", TableSchemaInfo.cprounitstate)))){
				params[StringUtil.getIndex("audittime", TableSchemaInfo.cprounitstate)] = null;
			}
			
			String newUnitId = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newUnitId != null) {
				keyMapper.addKeyMap(KeyMapper.UNITID, oldUnitid, newUnitId, Integer.valueOf(oldUserid));
			} else {
				keyMapper.addKeyMap(KeyMapper.UNITID, oldUnitid, String.valueOf(newGenerateUnitId), Integer.valueOf(oldUserid));
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.CPROUNIT_STATE;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.cprounitstate;
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
