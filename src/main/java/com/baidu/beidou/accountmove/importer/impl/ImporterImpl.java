package com.baidu.beidou.accountmove.importer.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.accountmove.importer.Importer;
import com.baidu.beidou.accountmove.table.TableImporter;

public class ImporterImpl implements Importer {

	private static final Log logger = LogFactory.getLog(ImporterImpl.class);
	
	private List<TableImporter> importerList;
	
	@Override
	public void importAccount(int userId) {
		for (TableImporter importer : importerList ) {
			logger.info("start import table "+ importer.getTableName());
			importer.executeImport(userId);
			logger.info("done import table "+ importer.getTableName());
		}
		
	}

	public List<TableImporter> getImporterList() {
		return importerList;
	}

	public void setImporterList(List<TableImporter> importerList) {
		this.importerList = importerList;
	}

	
}
