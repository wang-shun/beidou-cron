package com.baidu.beidou.accountmove.table.cprogroupinfo;

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

public class CprogroupinfoImporter implements TableImporter{

	private static final Log logger = LogFactory.getLog(CprogroupinfoImporter.class);
	
	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	
	private IDService idService;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> groupinfoList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(groupinfoList)) {
			return;
		}
		
		for (List<String> groupinfo : groupinfoList) {
			String oldGroupid = groupinfo.get(StringUtil.getIndex("groupid", getColumnNameArray()));
			String oldUserid = groupinfo.get(StringUtil.getIndex("userid", getColumnNameArray()));
			String oldMcId = groupinfo.get(StringUtil.getIndex("attach_ubmc_id", getColumnNameArray()));
			String oldMcVersion = groupinfo.get(StringUtil.getIndex("attach_ubmc_version_id", getColumnNameArray()));
			
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.GROUPID, oldGroupid, userId) == null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			// planid auto increment
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			groupinfo.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
			//replace old groupid
			String newGenerateGroupId = keyMapper.getMappedKey(KeyMapper.GROUPID,
					oldGroupid, Integer.valueOf(oldUserid));
			groupinfo.set(StringUtil.getIndex("groupid", getColumnNameArray()), String.valueOf(newGenerateGroupId));
			
			// change ubmc id and version id
			if(Long.valueOf(oldMcId) > 0){
				UbmcMaterial ubmc = idService.generateGroupUbmcMaterial(userId, Long.valueOf(oldMcId), Integer.valueOf(oldMcVersion), Integer.valueOf(oldGroupid), Integer.valueOf(newGenerateGroupId));
				if(ubmc != null){//TODO 需要放出来，这是ubmc换的失败了；
					groupinfo.set(StringUtil.getIndex("attach_ubmc_id", getColumnNameArray()), String.valueOf(ubmc.getMcId()));
					groupinfo.set(StringUtil.getIndex("attach_ubmc_version_id", getColumnNameArray()), String.valueOf(ubmc.getVersionid()));
				}
			}
			
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
				params[i] = groupinfo.get(i);
			}
			String newGroupId = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newGroupId != null) {
				logger.info("autoincrement cprogroupinfo primary key is:" + newGroupId);
			}
			
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.GROUPINFO;
	}
	
	public String[] getColumnNameArray() {
		return TableSchemaInfo.cprogroupinfo;
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
