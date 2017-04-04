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

import com.baidu.beidou.cprounit.icon.bo.UserUploadIcon;

public class UserIconSet {
	
	private static final Log log = LogFactory.getLog(UserIconSet.class);
	
	private int total;
	private BufferedReader reader = null;
	
	public UserIconSet(String fileName) {
		total = 0;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized List<UserUploadIcon> getNextList(int maxNum) {
		List<UserUploadIcon> result = new ArrayList<UserUploadIcon>();
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
				UserUploadIcon mater = new UserUploadIcon();
				mater.setId(Integer.valueOf(items[column++]));
				mater.setUserId(Integer.valueOf(items[column++]));
				
				mater.setFileSrc(dealEmptyStr(items[column++]));
				
				mater.setHight(dealEmptyInt(items[column++]));
				mater.setWidth(dealEmptyInt(items[column++]));
				
				mater.setUbmcsyncflag(Integer.valueOf(items[column++]));
				mater.setMcId(Long.valueOf(items[column++]));
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
