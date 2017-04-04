package com.baidu.beidou.accountmove.table.cproplan;

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

public class CproplanImporter implements TableImporter{

	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	
	@Override
	public void executeImport(int userId) {
		
		// read cproplan file, for each line
		List<List<String>> planList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(planList)) {
			return;
		}
		
		for (List<String> plan : planList) {
			String oldPlanid = plan.get(StringUtil.getIndex("planid", TableSchemaInfo.cproplan)); 
			String oldUserid = plan.get(StringUtil.getIndex("userid", TableSchemaInfo.cproplan));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.PLANID, oldPlanid, userId) != null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			// planid auto increment
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			plan.set(StringUtil.getIndex("userid", TableSchemaInfo.cproplan), newUserId);
			String[] colNames = Arrays.copyOfRange(TableSchemaInfo.cproplan, 1,
					(TableSchemaInfo.cproplan.length));
			// insert into beidou.cproplan (planname,...,plan_type)values(?,...,?);
			String sql = "insert into "
					+ TableNameUtil.getActualTableName(getTableName(), Integer.parseInt(newUserId)) + " ("
					+ StringUtil.getStringFromArray(colNames, ",")
					+ ") values("
					+ StringUtil.getRepetitionString("?", ",", colNames.length)
					+ ");";
			Object[] params = new Object[colNames.length];
			for (int i = 0; i < colNames.length; i++) {
				params[i] = plan.get(i + 1);
			}
			String newPlanid = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newPlanid != null) {
				keyMapper.addKeyMap(KeyMapper.PLANID, oldPlanid, newPlanid, Integer.valueOf(oldUserid));
			}
			
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.PLAN;
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
