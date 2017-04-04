package com.baidu.beidou.accountmove.table.cprogroupatright;

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

public class CprogroupAtrightImporter implements TableImporter{

//	private static final Log logger = LogFactory.getLog(CprogroupAtrightImporter.class);
	
	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> groupatList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(groupatList)) {
			return;
		}
		
		for (List<String> groupat : groupatList) {
			String oldAtid = groupat.get(StringUtil.getIndex("atid", getColumnNameArray()));
			String oldGroupid = groupat.get(StringUtil.getIndex("groupid", getColumnNameArray()));
			String oldPlanid = groupat.get(StringUtil.getIndex("planid", getColumnNameArray())); 
			String oldUserid = groupat.get(StringUtil.getIndex("userid", getColumnNameArray()));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.ATID, oldAtid, userId) != null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			groupat.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
			String newPlanId = keyMapper.getMappedKey(KeyMapper.PLANID,
					oldPlanid, Integer.valueOf(oldUserid));
			groupat.set(StringUtil.getIndex("planid", getColumnNameArray()), newPlanId);
			
			String newGroupId = keyMapper.getMappedKey(KeyMapper.GROUPID,
					oldGroupid, Integer.valueOf(oldUserid));
			groupat.set(StringUtil.getIndex("groupid", getColumnNameArray()), newGroupId);
			
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
				params[i] = groupat.get(i + 1);
			}
			String newAtId = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newAtId == null) {
				keyMapper.addKeyMap(KeyMapper.ATID, oldAtid, newAtId, Integer.valueOf(oldUserid));
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.CPROGROUP_ATRIGHT;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.cprogroup_atright;
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
