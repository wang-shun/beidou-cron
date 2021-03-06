package com.baidu.beidou.accountmove.table.smartideakeyword;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.accountmove.dao.DataAccessService;
import com.baidu.beidou.accountmove.table.TableExporter;
import com.baidu.beidou.accountmove.table.TableSchemaInfo;
import com.baidu.beidou.accountmove.util.FileExporter;
import com.baidu.beidou.accountmove.util.StringUtil;
import com.baidu.beidou.accountmove.util.TableNameUtil;

public class SmartIdeaKeywordExporter implements TableExporter {

	private FileExporter fexporter;
	private DataAccessService dataAccessService;

	@Override
	public void executeExport(int userId) {
		// query info from db
		String exportSql = "select "
				+ StringUtil.getStringFromArray(getColumnNameArray(),
						",") + " from "
				+ TableNameUtil.getActualTableName(getTableName(), userId)
				+ " where userid = " + userId + ";";
		List<List<String>> result = dataAccessService.queryInfo(exportSql,
				null, userId);

		if (CollectionUtils.isEmpty(result)) {
			return;
		}
		// save info into file
		for (List<String> message : result) {
			fexporter.export(message, getTableName(), userId);
		}

	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.SMARTIDEA_KEYWORD;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.smartidea_keyword;
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

}
