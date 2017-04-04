package com.baidu.beidou.auditmanager.vo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.util.bmqdriver.bo.BmqUrlCheck;

public class ValidUrlSet {
	
	private static final Log log = LogFactory.getLog(ValidUrlSet.class);
	
	private int index;
	private int count;
	private static int numPerReq = 1000;
	private long firstTaskId = 0;
	
	private List<BmqUrlCheck> validUrlCheckList = null;
	
	// key是targeturl，value是taskid
	private Map<KeyForUrl, Long> validUrlMap = null;
	private List<KeyForUrl> keySet = null;
	
	public ValidUrlSet(long firstTaskId) {
		index = 0;
		count = 0;
		this.firstTaskId = firstTaskId;
		validUrlCheckList = new ArrayList<BmqUrlCheck>();
		validUrlMap = new HashMap<KeyForUrl, Long>();
		keySet = new ArrayList<KeyForUrl>();
	}
	
	public boolean init(List<UrlCheckUnit> urlCheckUnitList,
			int cntPerReq, int type, String dateStr) {

		long count = firstTaskId;
		int dateSuf = Integer.valueOf(dateStr.substring(6));
			
		for (UrlCheckUnit urlCheckUnit : urlCheckUnitList) {

			List<KeyForUrl> keyForUrlList = new ArrayList<KeyForUrl>(2);
			keyForUrlList.add(new KeyForUrl(urlCheckUnit.getUserId(), urlCheckUnit.getTargetUrl()));
			if (StringUtils.isNotEmpty(urlCheckUnit.getWirelessTargetUrl()) && !urlCheckUnit.getWirelessTargetUrl().equals(urlCheckUnit.getTargetUrl())) {
				keyForUrlList.add(new KeyForUrl(urlCheckUnit.getUserId(), urlCheckUnit.getWirelessTargetUrl()));
			}
			
			for (KeyForUrl urlKey : keyForUrlList) {
				if (keySet.contains(urlKey)) {
					for (KeyForUrl key : keySet) {
						if (key.equals(urlKey)) {
							urlKey = key;
							break;
						}
					}
					
					Long taskId = validUrlMap.get(urlKey);
					urlCheckUnit.setTaskId(taskId);
				} else {
					count++;
					Long taskId = Long.valueOf(100 * count + dateSuf);
					
					urlCheckUnit.setTaskId(taskId);
					
					validUrlMap.put(urlKey, taskId);
					keySet.add(urlKey);
					
					BmqUrlCheck urlCheck = new BmqUrlCheck();
					urlCheck.setTaskid(taskId);
					urlCheck.setType(type);
					urlCheck.setUrl(urlKey.getUrl());
					urlCheck.setUserid(urlCheckUnit.getUserId());
					validUrlCheckList.add(urlCheck);
				}
			}
			
		}
		
		numPerReq = cntPerReq;
		this.count = validUrlCheckList.size();
		
		return true;
	}
	
	public List<UrlCheckUnit> init(String fileName, int cntPerReq, int type, String dateStr) {
		numPerReq = cntPerReq;
		
		List<UrlCheckUnit> result = new ArrayList<UrlCheckUnit>();
		
		int count = 0;
		int dateSuf = Integer.valueOf(dateStr.substring(6));
		
		File inFile = new File(fileName);
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new FileReader(inFile));
			
			while ((line = reader.readLine()) != null) {
				String[] items = line.split("\t");
				
				UrlCheckUnit urlCheckUnit = new UrlCheckUnit();
				urlCheckUnit.setId(Long.valueOf(items[0]));
				urlCheckUnit.setUserId(Integer.valueOf(items[1]));
				urlCheckUnit.setBeidouId(Integer.valueOf(items[2]));
				urlCheckUnit.setTargetUrl(new String(items[3].getBytes(), "gbk"));
				if (items.length>4 && StringUtils.isNotEmpty(items[4])){
					urlCheckUnit.setWirelessTargetUrl(new String(items[4].getBytes(), "gbk"));
				} 
				
				List<KeyForUrl> keyForUrlList = new ArrayList<KeyForUrl>(2);
				keyForUrlList.add(new KeyForUrl(urlCheckUnit.getUserId(), urlCheckUnit.getTargetUrl()));
				if (StringUtils.isNotEmpty(urlCheckUnit.getWirelessTargetUrl()) && !urlCheckUnit.getWirelessTargetUrl().equals(urlCheckUnit.getTargetUrl())) {
					keyForUrlList.add(new KeyForUrl(urlCheckUnit.getUserId(), urlCheckUnit.getWirelessTargetUrl()));
				}
				
				for (KeyForUrl urlKey : keyForUrlList) {
					if (keySet.contains(urlKey)) {
						for (KeyForUrl key : keySet) {
							if (key.equals(urlKey)) {
								urlKey = key;
								break;
							}
						}
						
						Long taskId = validUrlMap.get(urlKey);
						urlCheckUnit.setTaskId(taskId);
					} else {
						count++;
						Long taskId = Long.valueOf(100 * count + dateSuf);
						
						urlCheckUnit.setTaskId(taskId);
						
						validUrlMap.put(urlKey, taskId);
						keySet.add(urlKey);
						
						BmqUrlCheck urlCheck = new BmqUrlCheck();
						urlCheck.setTaskid(taskId);
						urlCheck.setType(type);
						urlCheck.setUrl(urlKey.getUrl());
						urlCheck.setUserid(urlCheckUnit.getUserId());
						validUrlCheckList.add(urlCheck);
					}
				}
				
				result.add(urlCheckUnit);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		this.count = validUrlCheckList.size();
		
		return result;	
	}
	
	public boolean hasNext() {
		return count > index;
	}
	
	public List<BmqUrlCheck> getNextValidUrlList() {
		if (count > index) {
			int toIndex = index + numPerReq;
			if (toIndex > count) {
				toIndex = count;
			}
			
			List<BmqUrlCheck> result = new ArrayList<BmqUrlCheck>();
			for (int i = index; i < toIndex; i++) {
				result.add(validUrlCheckList.get(i));
			}
			
			index = toIndex;
			
			return result;
		}
		
		return null;
	}
	
	public List<BmqUrlCheck> getAllValidUrlList() {
		return validUrlCheckList;
	}
}
