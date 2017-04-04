package com.baidu.beidou.accountmove.table.uniticon;

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

public class UnitIconImporter implements TableImporter{

//	private static final Log logger = LogFactory.getLog(UnitIconImporter.class);
	
	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	
	
	@Override
	public void executeImport(int userId) {
		// TODO order after useruploadicons（xdb）
		
		// read cprogroup file, for each line
		List<List<String>> unitIconList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(unitIconList)) {
			return;
		}
		
		for (List<String> icon : unitIconList) {
			String oldunitIconId = icon.get(StringUtil.getIndex("id", getColumnNameArray()));
			String oldUserid = icon.get(StringUtil.getIndex("userid", getColumnNameArray()));
			String oldUnitid = icon.get(StringUtil.getIndex("unitId", getColumnNameArray())); 
			String oldIconid = icon.get(StringUtil.getIndex("iconId", getColumnNameArray()));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.UNIT_ICONID, oldunitIconId, userId) == null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			icon.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
			String newUnitId = keyMapper.getMappedKey(KeyMapper.UNITID,
					oldUnitid, Integer.valueOf(oldUserid));
			icon.set(StringUtil.getIndex("unitId", getColumnNameArray()), newUnitId);
			
			String newIconId = keyMapper.getMappedKey(KeyMapper.USERUPLOADICONS_ID,
					oldIconid, Integer.valueOf(oldUserid));
			icon.set(StringUtil.getIndex("iconId", getColumnNameArray()), newIconId);
			
			// itid get from sequence
//			String newGenerateUnitId = keyMapper.getMappedKey(KeyMapper.UNITID, oldunitIconId, userId);
//			icon.set(StringUtil.getIndex("id", getColumnNameArray()), newGenerateUnitId);
			
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
				params[i] = icon.get(i + 1);
			}
			String newGroupIconId = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newGroupIconId != null) {
				keyMapper.addKeyMap(KeyMapper.UNIT_ICONID, oldunitIconId, newGroupIconId, Integer.valueOf(oldUserid));
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.UNIT_ICON;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.uniticon;
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
