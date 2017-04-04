package com.baidu.beidou.auditmanager.vo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.baidu.beidou.auditmanager.vo.AkaAuditUnit;

/**
 * ClassName: ValidUnitSet
 * Function: 符合aka轮询有效性的广告全库
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version beidou-cron 1.1.2
 * @since TODO
 * @date Aug 3, 2011
 * @see 
 */
public class ValidUnitSet {
	private int index;
	private int count;
	private static int numPerReq = 1000;
	private List<AkaAuditUnit> validUnitList = null;
	
	public ValidUnitSet() {
		index = 0;
		count = 0;
		validUnitList = new ArrayList<AkaAuditUnit>();
	}
	
	public void init(List<AkaAuditUnit> akaUnitList, int cntPerReq) {
		index = 0;
		validUnitList = akaUnitList;
		count = validUnitList.size();
		numPerReq = cntPerReq;
	}
	
	
	public void init(String fileName, int cntPerReq) {
		numPerReq = cntPerReq;
		File inFile = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(inFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] items = line.split("\t");
				
				AkaAuditUnit akaUnit = new AkaAuditUnit();
				akaUnit.setId(Long.valueOf(items[0]));
				akaUnit.setUserId(Integer.valueOf(items[1]));
				akaUnit.setTitle(new String(items[2].getBytes(), "gbk"));
				akaUnit.setDesc1(new String(items[3].getBytes(), "gbk"));
				akaUnit.setDesc2(new String(items[4].getBytes(), "gbk"));
				akaUnit.setTargetUrl(items[5]);
				akaUnit.setShowUrl(new String(items[6].getBytes(), "gbk"));
				akaUnit.setWirelessTargetUrl(items[7]);
				akaUnit.setWirelessShowUrl(new String(items[8].getBytes(), "gbk"));
				validUnitList.add(akaUnit);
			}
			
			count = validUnitList.size();
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
	}
	
	public synchronized boolean hasNext() {
		return count > index;
	}
	
	public synchronized List<AkaAuditUnit> getNextValidUnitList() {
		if (count > index) {
			int toIndex = index + numPerReq;
			if (toIndex > count) {
				toIndex = count;
			}
			
			List<AkaAuditUnit> result = new ArrayList<AkaAuditUnit>();
			for (int i = index; i < toIndex; i++) {
				result.add(validUnitList.get(i));
			}
			
			index = toIndex;
			
			return result;
		}
		
		return null;
	}
}
