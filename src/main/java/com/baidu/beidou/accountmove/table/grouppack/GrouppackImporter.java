package com.baidu.beidou.accountmove.table.grouppack;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.accountmove.dao.DataAccessService;
import com.baidu.beidou.accountmove.keymap.KeyMapper;
import com.baidu.beidou.accountmove.table.TableImporter;
import com.baidu.beidou.accountmove.table.TableSchemaInfo;
import com.baidu.beidou.accountmove.util.FileExporter;
import com.baidu.beidou.accountmove.util.StringUtil;
import com.baidu.beidou.accountmove.util.TableNameUtil;

public class GrouppackImporter implements TableImporter{

	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> groupPackList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(groupPackList)) {
			return;
		}
		
		for (List<String> groupPack : groupPackList) {
			String oldGPid = groupPack.get(StringUtil.getIndex("gpid", getColumnNameArray()));
			String oldGroupid = groupPack.get(StringUtil.getIndex("groupid", getColumnNameArray()));
			String oldPlanid = groupPack.get(StringUtil.getIndex("planid", getColumnNameArray())); 
			String oldUserid = groupPack.get(StringUtil.getIndex("userid", getColumnNameArray()));
			String oldpackId = groupPack.get(StringUtil.getIndex("pack_id", getColumnNameArray()));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.GPID, oldGPid, userId) != null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			groupPack.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
			String newPlanId = keyMapper.getMappedKey(KeyMapper.PLANID,
					oldPlanid, Integer.valueOf(oldUserid));
			groupPack.set(StringUtil.getIndex("planid", getColumnNameArray()), newPlanId);
			
			String newGroupId = keyMapper.getMappedKey(KeyMapper.GROUPID,
					oldGroupid, Integer.valueOf(oldUserid));
			groupPack.set(StringUtil.getIndex("groupid", getColumnNameArray()), newGroupId);
			
			String newPackId = keyMapper.getMappedKey(KeyMapper.PACKID,
					oldpackId, Integer.valueOf(oldUserid));
			groupPack.set(StringUtil.getIndex("pack_id", getColumnNameArray()), newPackId);
			
			// vtid get from sequence
			
			String[] colNames = Arrays.copyOfRange(getColumnNameArray(), 1,
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
				params[i] = groupPack.get(i + 1);
			}
			String newVtId = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newVtId != null) {
				keyMapper.addKeyMap(KeyMapper.GPID, oldGPid, newVtId, Integer.valueOf(oldUserid));
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.GROUPPACK;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.group_pack;
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

}
