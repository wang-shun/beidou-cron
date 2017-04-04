package com.baidu.beidou.accountmove.exporter.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.accountmove.exporter.Exporter;
import com.baidu.beidou.accountmove.table.TableExporter;

public class ExporterImpl implements Exporter {

	private static final Log logger = LogFactory.getLog(ExporterImpl.class);
	private List<TableExporter> exporterList;
	
	
	@Override
	public void exportAccount(int userId) {

		for (TableExporter exportor : exporterList ) {
			logger.info("start export table "+ exportor.getTableName());
			exportor.executeExport(userId);
			logger.info("done export table "+ exportor.getTableName());
		}
		
	}


	public List<TableExporter> getExporterList() {
		return exporterList;
	}


	public void setExporterList(List<TableExporter> exporterList) {
		this.exporterList = exporterList;
	}

}
