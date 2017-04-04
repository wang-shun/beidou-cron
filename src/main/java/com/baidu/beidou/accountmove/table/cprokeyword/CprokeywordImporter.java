package com.baidu.beidou.accountmove.table.cprokeyword;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

public class CprokeywordImporter implements TableImporter{

	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	private IDService idService;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> keywordList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(keywordList)) {
			return;
		}
		
		List<String> keywordRow = keywordList.get(0);
		String oldUserid = keywordRow.get(StringUtil.getIndex("userid", TableSchemaInfo.cprokeyword));
		String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,	oldUserid, Integer.valueOf(oldUserid));
		
		List<List<List<String>>> pageList = doPage(keywordList, 10000);
		
		for(List<List<String>> keywordListPage : pageList){
			Long[] newGenerateKeywordIds = idService.getNextKeywordIdBatch(Integer.valueOf(newUserId), "cprokeyword", keywordListPage.size());
			
			String[] colNames = Arrays.copyOfRange(TableSchemaInfo.cprokeyword, 0,
					TableSchemaInfo.cprokeyword.length);
			String sql = "insert into "
					+ TableNameUtil.getActualTableName(getTableName(), Integer.parseInt(newUserId)) + " ("
					+ StringUtil.getStringFromArray(colNames, ",")
					+ ") values("
					+ StringUtil.getRepetitionString("?", ",", colNames.length)
					+ ");";
			
			List<List<Object>> paramsList = new ArrayList<List<Object>>(keywordListPage.size());
			
			for(int i=0; i < keywordListPage.size(); i++){
				List<String> keyword = keywordListPage.get(i);
				String oldKeywordid = keyword.get(StringUtil.getIndex("keywordid", TableSchemaInfo.cprokeyword));
				String oldGroupid = keyword.get(StringUtil.getIndex("groupid", TableSchemaInfo.cprokeyword));
				String oldPlanid = keyword.get(StringUtil.getIndex("planid", TableSchemaInfo.cprokeyword));
				if (keyMapper.getMappedKey(KeyMapper.KEYWORDID, oldKeywordid, userId) != null) {
					continue;
				}
				
				keyword.set(StringUtil.getIndex("userid", TableSchemaInfo.cprokeyword), newUserId);
				
				String newPlanId = keyMapper.getMappedKey(KeyMapper.PLANID,
						oldPlanid, Integer.valueOf(oldUserid));
				keyword.set(StringUtil.getIndex("planid", TableSchemaInfo.cprokeyword), newPlanId);
				
				String newGroupId = keyMapper.getMappedKey(KeyMapper.GROUPID,
						oldGroupid, Integer.valueOf(oldUserid));
				keyword.set(StringUtil.getIndex("groupid", TableSchemaInfo.cprokeyword), newGroupId);
				
				// keywordid get from sequence
				long newGenerateKeywordId = newGenerateKeywordIds[i];
				keyword.set(StringUtil.getIndex("keywordid", TableSchemaInfo.cprokeyword), String.valueOf(newGenerateKeywordId));
				
				keyMapper.addKeyMap(KeyMapper.KEYWORDID, oldKeywordid, String.valueOf(newGenerateKeywordId), Integer.valueOf(oldUserid));
				
				List<Object> params = new ArrayList<Object>(colNames.length);
				for (int j = 0; j < colNames.length; j++) {
					params.add(keyword.get(j));
				}
				
				if (StringUtils.isEmpty(keyword.get(StringUtil.getIndex("audittime", TableSchemaInfo.cprokeyword)))) {
	                params.set(StringUtil.getIndex("audittime", TableSchemaInfo.cprokeyword), null);
	            }
				
				paramsList.add(params);
			}
			dataAccessService.updateInfoBatch(sql, paramsList, Integer.parseInt(newUserId));
			
		}
		
//		
//		for (List<String> keyword : keywordList) {
//			String oldKeywordid = keyword.get(StringUtil.getIndex("keywordid", TableSchemaInfo.cprokeyword));
//			String oldGroupid = keyword.get(StringUtil.getIndex("groupid", TableSchemaInfo.cprokeyword));
//			String oldPlanid = keyword.get(StringUtil.getIndex("planid", TableSchemaInfo.cprokeyword)); 
//			String oldUserid = keyword.get(StringUtil.getIndex("userid", TableSchemaInfo.cprokeyword));
//			
//			// query primary key to make sure key not processed; 
//			if (keyMapper.getMappedKey(KeyMapper.KEYWORDID, oldKeywordid, userId) != null) {
//				continue;
//			}
//			
//			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
//			
//			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
//					oldUserid, Integer.valueOf(oldUserid));
//			keyword.set(StringUtil.getIndex("userid", TableSchemaInfo.cprokeyword), newUserId);
//			
//			String newPlanId = keyMapper.getMappedKey(KeyMapper.PLANID,
//					oldPlanid, Integer.valueOf(oldUserid));
//			keyword.set(StringUtil.getIndex("planid", TableSchemaInfo.cprokeyword), newPlanId);
//			
//			String newGroupId = keyMapper.getMappedKey(KeyMapper.GROUPID,
//					oldGroupid, Integer.valueOf(oldUserid));
//			keyword.set(StringUtil.getIndex("groupid", TableSchemaInfo.cprokeyword), newGroupId);
//			
//			// keywordid get from sequence
//			long newGenerateKeywordId = idService.generateKeys(Integer.valueOf(newUserId), "cprokeyword", 1);
//			keyword.set(StringUtil.getIndex("keywordid", TableSchemaInfo.cprokeyword), String.valueOf(newGenerateKeywordId));
//			
//			String[] colNames = Arrays.copyOfRange(TableSchemaInfo.cprokeyword, 0,
//					TableSchemaInfo.cprokeyword.length);
//			// insert into beidou.cproplan (planname,...,plan_type)values(?,...,?);
//			String sql = "insert into "
//					+ TableNameUtil.getActualTableName(getTableName(), Integer.parseInt(newUserId)) + " ("
//					+ StringUtil.getStringFromArray(colNames, ",")
//					+ ") values("
//					+ StringUtil.getRepetitionString("?", ",", colNames.length)
//					+ ");";
//			Object[] params = new Object[colNames.length];
//			for (int i = 0; i < colNames.length; i++) {
//				params[i] = keyword.get(i);
//			}
//			
//			if(StringUtils.isEmpty(keyword.get(StringUtil.getIndex("audittime", TableSchemaInfo.cprokeyword)))){
//				params[StringUtil.getIndex("audittime", TableSchemaInfo.cprokeyword)] = null;
//			}
//			
//			String newKeywordId = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
//			if (newGroupId != null) {
//				keyMapper.addKeyMap(KeyMapper.KEYWORDID, oldKeywordid, newKeywordId, Integer.valueOf(oldUserid));
//			} else {
//				keyMapper.addKeyMap(KeyMapper.KEYWORDID, oldKeywordid, String.valueOf(newGenerateKeywordId), Integer.valueOf(oldUserid));
//			}
//		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.KEYWORD;
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

	public static <T extends Object> List<List<T>> doPage(List<T> list, int pageSize) {
		if (CollectionUtils.isEmpty(list)) {
			return Collections.emptyList();
		}

		int pageNum = (list.size() / pageSize) + 1;
		if ((list.size() % pageSize) == 0) {
			pageNum -= 1;
		}
		List<List<T>> result = new ArrayList<List<T>>(pageNum);
		for (int i = 0; i < pageNum; i++) {
			int from = i * pageSize;
			int to = (i + 1) * pageSize;
			if (to > list.size()) {
				to = list.size();
			}
			List<T> item = list.subList(from, to);
			result.add(item);
		}

		return result;
	}
	
	
}
