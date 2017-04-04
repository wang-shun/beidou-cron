package com.baidu.beidou.accountmove.keymap.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.accountmove.keymap.KeyMapper;

public class KeyMapperImpl implements KeyMapper {

	private static final Log logger = LogFactory.getLog(KeyMapperImpl.class);
	
	private Map<String, KeyMap> keyholder = new HashMap<String, KeyMapperImpl.KeyMap>();
	private int userId;
	
	private String baseFilePath = null;
	
	public KeyMapperImpl () {
	}
	
	public KeyMapperImpl (int userId) {
		this.userId = userId;
	}
	
	@Override
	public boolean addKeyMap(String keyName, String oldKey, String newKey, int userId) {
		
		if (this.userId == 0) {
			this.userId = userId;
		}
		
		KeyMap keymapper = keyholder.get(keyName);
		
		if (keymapper == null) {
			keymapper = new KeyMap(keyName);
			keyholder.put(keyName, keymapper);
		}
		
		keymapper.addKeyMap(oldKey, newKey);
		
		return true;
	}

	@Override
	public String getMappedKey(String keyName, String oldKey, int userId) {
		
		if (this.userId == 0) {
			this.userId = userId;
		}
		
		KeyMap keymapper = keyholder.get(keyName);
		if (keymapper == null) {
			keymapper = new KeyMap(keyName);
		}
		return keymapper.getMappedKey(oldKey);
	}

	
	class KeyMap {
		
		String keyName;
		Map<String, String> keyMap;
		/**
		 * construct, will initial map when new a object
		 * @param keyName
		 */
		public KeyMap(String keyName) {
			this.keyName = keyName;
			initKeyMap();
		}
		
		/**
		 * add a key map into map, and write into map file
		 * @param oldKey oldKey
		 * @param newKey newKey
		 * @return result
		 */
		public boolean addKeyMap(String oldKey, String newKey) {
			if (keyMap == null) {
				initKeyMap();
			}
			keyMap.put(oldKey, newKey);
			// persist into file
			String filePath = baseFilePath + "/" + userId + "/keyMap" + "/" + keyName;
			this.write(oldKey + "\t" + newKey + "\n", filePath);
			return true;
		}

		/**
		 * get map key from map
		 * @param oldKey oldKey
		 * @return result
		 */
		public String getMappedKey(String oldKey) {
			if (keyMap == null) {
				initKeyMap();
			}
			return keyMap.get(oldKey);
		}
		
		/**
		 * if exist file with name keyName, load this file into map;
		 * else create a empty map
		 */
		private void initKeyMap() {
			keyMap = new HashMap<String, String>();
			
			String filePath = baseFilePath + "/" + userId + "/keyMap" + "/" + keyName;
			
			BufferedReader br = null;
			try {
				File file = new File(filePath);
				// if cache not exist, create new file
				File fileParent = file.getParentFile();
				if (!fileParent.exists()) {
					fileParent.mkdirs();
				}
				if (!file.exists()) {
					file.createNewFile();
					return;
				}
				br = new BufferedReader(new FileReader(filePath));
				String line = null;
				do{
					line = br.readLine();
					
					if (line==null || !line.contains("\t") || line.trim().equals("")) {
						continue;
					}
					String oldKey = line.split("\t")[0].trim();
					String newKey = line.split("\t")[1].trim();
					
					keyMap.put(oldKey, newKey);
					
				}while(line != null);
			} catch (FileNotFoundException e) {
				logger.error("load valid group failed, not found file:" + filePath);
				e.printStackTrace();
			} catch (IOException e) {
				logger.error("load valid group failed, read io exception");
				e.printStackTrace();
			}finally{
				try {
					if (br!=null) {
						br.close();
					}
				} catch (IOException e) {
					logger.error("close export file failed.");
					e.printStackTrace();
				}
			}
		}
		
		private void write(String message, String filePath) {

			String outputFilePath = filePath;
			FileOutputStream fout = null;
			FileChannel fch = null;

			try {
				fout = new FileOutputStream(new File(outputFilePath), true);

				fch = fout.getChannel();
				fch.write(ByteBuffer.wrap(message.getBytes("utf-8")));
			} catch (IOException e) {
				logger.error("message export to file failed");
				logger.error("LOST_MESSAGE:" + message);
				e.printStackTrace();
			} finally {
				try {
					if (fch != null) {
						fch.close();
					}
					if (fout != null) {
						fout.close();
					}
				} catch (IOException e) {
					logger.error("close export file failed.");
					e.printStackTrace();
				}
			}
		}
	}


	public String getBaseFilePath() {
		return baseFilePath;
	}

	public void setBaseFilePath(String baseFilePath) {
		this.baseFilePath = baseFilePath;
	}
	
}
