package com.baidu.beidou.accountmove.table.cprogroup;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.accountmove.dao.DataAccessService;
import com.baidu.beidou.accountmove.dao.IDService;
import com.baidu.beidou.accountmove.keymap.KeyMapper;
import com.baidu.beidou.accountmove.table.TableImporter;
import com.baidu.beidou.accountmove.table.TableSchemaInfo;
import com.baidu.beidou.accountmove.util.FileExporter;
import com.baidu.beidou.accountmove.util.StringUtil;
import com.baidu.beidou.accountmove.util.TableNameUtil;

public class CprogroupImporter implements TableImporter{

	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	
	private IDService idService;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> groupList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(groupList)) {
			return;
		}
		
		for (List<String> group : groupList) {
			String oldGroupid = group.get(StringUtil.getIndex("groupid", TableSchemaInfo.cprogroup));
			String oldPlanid = group.get(StringUtil.getIndex("planid", TableSchemaInfo.cprogroup)); 
			String oldUserid = group.get(StringUtil.getIndex("userid", TableSchemaInfo.cprogroup));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.GROUPID, oldGroupid, userId) != null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			// planid auto increment
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			group.set(StringUtil.getIndex("userid", TableSchemaInfo.cprogroup), newUserId);
			
			String newPlanId = keyMapper.getMappedKey(KeyMapper.PLANID,
					oldPlanid, Integer.valueOf(oldUserid));
			group.set(StringUtil.getIndex("planid", TableSchemaInfo.cprogroup), newPlanId);
			
			// generate new groupid, and replace old groupid
			long newGenerateGroupId = idService.generateKeys(Integer.valueOf(newUserId), "cprogroup", 1);
			group.set(StringUtil.getIndex("groupid", TableSchemaInfo.cprogroup), String.valueOf(newGenerateGroupId));
			
			String[] colNames = Arrays.copyOfRange(TableSchemaInfo.cprogroup, 0,
					(TableSchemaInfo.cprogroup.length));
			// insert into beidou.cproplan (planname,...,plan_type)values(?,...,?);
			String sql = "insert into "
					+ TableNameUtil.getActualTableName(getTableName(), Integer.parseInt(newUserId)) + " ("
					+ StringUtil.getStringFromArray(colNames, ",")
					+ ") values("
					+ StringUtil.getRepetitionString("?", ",", colNames.length)
					+ ");";
			Object[] params = new Object[colNames.length];
			for (int i = 0; i < colNames.length; i++) {
				params[i] = group.get(i);
			}
			String newGroupId = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newGroupId != null) {
				keyMapper.addKeyMap(KeyMapper.GROUPID, oldGroupid, newGroupId, Integer.valueOf(oldUserid));
			}else {
				keyMapper.addKeyMap(KeyMapper.GROUPID, oldGroupid, String.valueOf(newGenerateGroupId), Integer.valueOf(oldUserid));
			}
			
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.GROUP;
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
