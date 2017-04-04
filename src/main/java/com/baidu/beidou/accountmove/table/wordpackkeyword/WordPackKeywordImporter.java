package com.baidu.beidou.accountmove.table.wordpackkeyword;

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

public class WordPackKeywordImporter implements TableImporter {

//	private static final Log logger = LogFactory.getLog(AttachInfoImporter.class);
	
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
			String oldsmartid = groupit.get(StringUtil.getIndex("wpk_id", getColumnNameArray()));
			String oldUserid = groupit.get(StringUtil.getIndex("userid", getColumnNameArray()));
			String oldPackid = groupit.get(StringUtil.getIndex("pack_id", getColumnNameArray()));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.WORD_PACK_KEYWORD_ID, oldsmartid, userId) != null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			groupit.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
//			String newPlanId = keyMapper.getMappedKey(KeyMapper.PLANID,
//					oldPlanid, Integer.valueOf(oldUserid));
//			groupit.set(StringUtil.getIndex("planid", getColumnNameArray()), newPlanId);
//			
//			String newGroupId = keyMapper.getMappedKey(KeyMapper.GROUPID,
//					oldGroupid, Integer.valueOf(oldUserid));
//			groupit.set(StringUtil.getIndex("groupid", getColumnNameArray()), newGroupId);
			
			String newPackid = keyMapper.getMappedKey(KeyMapper.PACKID,
					oldPackid, Integer.valueOf(oldUserid));
			if(newPackid != null){
				groupit.set(StringUtil.getIndex("pack_id", getColumnNameArray()), newPackid);
			}
			
//			// itid get from sequence
			long newGenerateId = idService.generateKeys(Integer.valueOf(newUserId), "packkeyword", 1);;
			groupit.set(StringUtil.getIndex("wpk_id", getColumnNameArray()), String.valueOf(newGenerateId));
			
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
			String newSmartid = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newSmartid != null) {
				keyMapper.addKeyMap(KeyMapper.WORD_PACK_KEYWORD_ID, oldsmartid, newSmartid, Integer.valueOf(oldUserid));
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.WORD_PACK_KEYWORD;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.word_pack_keyword;
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
