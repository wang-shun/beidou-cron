package com.baidu.beidou.accountmove.ubmc.conf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class FileConfig {
	
	public FileConfig(String path){
		this(new File(path));
	}

	Map<String, String> properties = new HashMap<String, String>();
	
	public FileConfig(File f) {
		if(!f.isFile()){
			throw new RuntimeException("配置文件不存在");
		}
		
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			String line = null;
			while((line = br.readLine()) != null){
				extractProperties(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			closeReader(br);
			closeReader(fr);
		}
	}

	private void extractProperties(String line) {
		line = line.trim();
		if(line.startsWith("#")){
			return;
		}
		
		int index = line.indexOf('=');
		if(index < 0){
			return;
		}
		
		String key = line.substring(0, index).trim();
		String value = line.substring(index + 1).trim();
		if(key.length() > 0 && value.length() > 0){
			properties.put(key, value);
		}else{
			System.out.println("异常的配置:" + line);
		}
	}

	private void closeReader(Reader r) {
		if(r != null){
			try{
				r.close();
			}catch(Exception e){
			}
		}
		
	}
	
	public String getProperty(String key){
		return properties.get(key);
	}
	
	public int getPropertyAsInt(String key){
		String value = properties.get(key);
		return Integer.parseInt(value);
	}
	
	public long getPropertyAsLong(String key){
		String value = properties.get(key);
		return Long.parseLong(value);
	}

	public String[] getPropertyAsArray(String key) {
		String value = properties.get(key);
		return value.split(",");
	}
}
