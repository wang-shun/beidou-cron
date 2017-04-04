package com.baidu.beidou.accountmove.ubmc.subFrame.mcCopy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CopyLog {

	private static BufferedWriter succWriter = null;
	private static BufferedWriter failWriter = null;
	private static String splitTag = " ";

	private static Map<MaterialKey, MaterialKey> succKeyMap = null; 
	public static void initCopyLog(String succLogPath, String failLogPath) {
		CopyLog.succWriter = initWriter(succLogPath);
		CopyLog.failWriter = initWriter(failLogPath);
		
		loadSuccKeyMap(succLogPath);
	}
	private static void loadSuccKeyMap(String succLogPath) {
		BufferedReader br = null;
		String line = null;
		try{
			succKeyMap = new HashMap<MaterialKey, MaterialKey>(1000000);
			br = new BufferedReader(new FileReader(succLogPath));
			while((line = br.readLine()) != null){
				String[] array = line.split(splitTag);
				long mcId = Long.parseLong(array[2]);
				long newMcId = Long.parseLong(array[8]);
				int mcVersion = Integer.parseInt(array[3]);
				int newMcVersion = Integer.parseInt(array[9]);
				
				succKeyMap.put(new MaterialKey(mcId, mcVersion), new MaterialKey(newMcId, newMcVersion));
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		
	}
	public static void releaseMem() {
		try {
			succWriter.close();
			failWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static BufferedWriter initWriter(String path) {
		try {
			File f = new File(path);
			if(!f.isFile()){
				File parentDir = f.getParentFile();
				if(!parentDir.isDirectory()){
					parentDir.mkdirs();
				}
				f.createNewFile();
			}
			
			FileWriter fw = new FileWriter(path, true);
			return new BufferedWriter(fw);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void failCopy(long mcId, int mcVersion, int userId,
			int planId, int groupId, int unitId) {
		try {
			failWriter.write(buildLine(mcId, mcVersion, userId, planId,
					groupId, unitId));
			failWriter.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void succCopy(long mcId, int mcVersion, int userId,
			int planId, int groupId, int unitId, long newMcId, int newMcVersion) {
		try {
		    // TODO 成功后是否需要将成功的mcId和versionId放置succKeyMap，后面需要考虑下
			succWriter.write(buildLine(mcId, mcVersion, userId, planId,
					groupId, unitId, newMcId, newMcVersion));
			succWriter.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String buildLine(Object... os) {
		StringBuilder sb = new StringBuilder(100);
		String date = fmt.format(new Date());
		sb.append(date);
		for (Object o : os) {
			sb.append(splitTag).append(o);
		}
		return sb.append(EOF).toString();
	}

	private static DateFormat fmt = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");
	private static String EOF = System.getProperty("line.separator");
	
	/**
	 * 看这个物料是否已经进行了拷贝，如果已经拷贝则返回新的key，没拷贝则返回null
	 * @param mcId
	 * @param mcVersion
	 * @return
	 */
	public static MaterialKey getExistedKey(long mcId, int mcVersion){
		MaterialKey key = new MaterialKey(mcId, mcVersion);
		return succKeyMap.get(key);
	}

}
