package com.baidu.beidou.accountmove.table.useruploadicons;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.accountmove.dao.DataAccessService;
import com.baidu.beidou.accountmove.dao.IDService;
import com.baidu.beidou.accountmove.dao.UbmcMaterial;
import com.baidu.beidou.accountmove.keymap.KeyMapper;
import com.baidu.beidou.accountmove.table.TableImporter;
import com.baidu.beidou.accountmove.table.TableSchemaInfo;
import com.baidu.beidou.accountmove.util.FileExporter;
import com.baidu.beidou.accountmove.util.StringUtil;
import com.baidu.beidou.accountmove.util.TableNameUtil;

public class UserUploadIconsImporter implements TableImporter{

//	private static final Log logger = LogFactory.getLog(UnitIconImporter.class);
	
	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	
	private IDService idService;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> userIconList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(userIconList)) {
			return;
		}
		
		for (List<String> icon : userIconList) {
			String oldUserIconId = icon.get(StringUtil.getIndex("id", getColumnNameArray()));
			String oldUserid = icon.get(StringUtil.getIndex("userid", getColumnNameArray()));
			String oldMcId = icon.get(StringUtil.getIndex("mcId", getColumnNameArray()));
			
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.USERUPLOADICONS_ID, oldUserIconId, userId) == null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			icon.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
			
			// ubmc id get from sequence
			UbmcMaterial ubmc = idService.generateUbmcMaterial(userId, Long.valueOf(oldMcId), 1);
			if (ubmc != null){
				icon.set(StringUtil.getIndex("mcId", getColumnNameArray()), String.valueOf(ubmc.getMcId()));
			}
			
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
			String newUserIconId = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newUserIconId != null) {
				keyMapper.addKeyMap(KeyMapper.USERUPLOADICONS_ID, oldUserIconId, newUserIconId, Integer.valueOf(oldUserid));
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.USERUPLOADICONS;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.useruploadicons;
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
