package com.baidu.beidou.cprounit.service.syncubmc.vo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.auditmanager.vo.AuditHistoryView;

public class AuditHistoryUnitSet {
	
	private static final Log log = LogFactory.getLog(AuditHistoryUnitSet.class);
	
	private int total;
	private BufferedReader reader = null;
	
	public AuditHistoryUnitSet(String fileName) {
		total = 0;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized List<AuditHistoryView> getNextList(int maxNum) {
		List<AuditHistoryView> result = new ArrayList<AuditHistoryView>();
		if (maxNum <= 0) {
			return result;
		}
		
		int num = 0;
		String line = null;
		try {
			int column = 0;
			while ((line = reader.readLine()) != null && (!StringUtils.isEmpty(line))) {
				String[] items = line.split("\t");
				
				column = 0;
				AuditHistoryView mater = new AuditHistoryView();
				mater.setId(Integer.valueOf(items[column++]));
				mater.setUserId(Integer.valueOf(items[column++]));
				
				mater.setWuliaoType(Integer.valueOf(items[column++]));
				
				mater.setTitle(dealEscapeChar(dealEmptyStr(items[column++])));
				mater.setDescription1(dealEscapeChar(dealEmptyStr(items[column++])));
				mater.setDescription2(dealEscapeChar(dealEmptyStr(items[column++])));
				mater.setShowUrl(dealEmptyStr(items[column++]));
				mater.setTargetUrl(dealEscapeChar(dealEmptyStr(items[column++])));
				mater.setWirelessShowUrl(dealEmptyStr(items[column++]));
				mater.setWirelessTargetUrl(dealEscapeChar(dealEmptyStr(items[column++])));
				
				mater.setFileSrc(dealEmptyStr(items[column++]));
				
				mater.setHeight(dealEmptyInt(items[column++]));
				mater.setWidth(dealEmptyInt(items[column++]));
				
				mater.setUbmcsyncflag(Integer.valueOf(items[column++]));
				mater.setMcId(Long.valueOf(items[column++]));
				mater.setMcVersionId(Integer.valueOf(items[column++]));
				result.add(mater);
				
				num++;
				total++;
				if (num >= maxNum) {
					break;
				}
			}
		} catch (FileNotFoundException e) {
			log.error("file not found...", e);
		} catch (IOException e) {
			log.error("read data from file failed...", e);
		} catch (Exception e) {
			log.error("read data from file failed for line=" + line, e);
		}
		
		return result;
	}
	
	private synchronized String dealEscapeChar(String str) {
		try {
			str = str.replaceAll("\\\\\\\\", "\\\\");
		} catch (Exception e) {
			return str;
		}
		return str;
	}
	
	private synchronized String dealEmptyStr(String str) {
		if (StringUtils.isEmpty(str)
				|| str.equalsIgnoreCase("null")) {
			return null;
		}
		return str;
	}
	
	private synchronized Integer dealEmptyInt(String str) {
		if (StringUtils.isEmpty(str)
				|| str.equalsIgnoreCase("null")) {
			return 0;
		}
		return Integer.valueOf(str);
	}
	
	public void closeFile() {
		try {
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
}
