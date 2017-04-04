package com.baidu.beidou.accountmove.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * simple file service
 * used to manage file write and read
 * @author work
 *
 */
public class FileExporter {
	
	private static final Log logger = LogFactory.getLog(FileExporter.class);
	
	private String baseFilePath;
	
	/**
	 * read file line by line,use \t as a seperator to splite it into a list
	 * @param fileName just fileName,not contain path
	 * @param userId old userId
	 * @return result
	 */
	public List<List<String>> importIn(String fileName, int userId) {
		String filePath =  baseFilePath + "/" + userId + "/info/" + fileName;
		File file = new File(filePath);
		BufferedReader br = null;
		
		List<List<String>> result = new ArrayList<List<String>>();
		// if file not exist, just return
		if (!file.exists()) {
			return result;
		}
		
		try {
						
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			String lineStr = null;
			while ((lineStr = br.readLine()) != null) {
				String line = lineStr + "\t#";
				List<String> lineList = StringUtil.getListFromString(line, "\t");
				if (CollectionUtils.isNotEmpty(lineList)) {
				    for (int i = 0; i < lineList.size(); i++) {
				        String col = lineList.get(i);
				        if ("###***###".equals(col)) { // decode string ###***### to null
				            lineList.set(i, null);
				        }
				    }
					result.add(lineList.subList(0, lineList.size()-1));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				logger.error("close import in file failed.");
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	/**
	 * write message into file,use \t as seperator
	 * @param message message
	 * @param fileName fileName
	 * @param userId old userid
	 */
	public void export(List<String> message, String fileName, int userId) {
        StringBuffer strLine = new StringBuffer();
        for (int i = 0; i<message.size(); i++) {
        	if (i > 0) {
        		strLine.append("\t");
        	}
        	
        	if (message.get(i) == null) {
        		strLine.append("###***###"); // incode null to string ###***###
        	} else {
        		strLine.append(message.get(i));
        	}
        }
        write(strLine.toString()+System.getProperty("line.separator", "\n"), baseFilePath + "/" + userId + "/info/" + fileName);
        
	}

	/**
	 * actual file operator
	 * @param message message
	 * @param filePath filePath
	 */
	private void write(String message, String filePath) {

		File outputFile = new File(filePath);
		FileOutputStream fout = null;
		FileChannel fch = null;
		// if cache not exist, create new file
		File fileParent = outputFile.getParentFile();
		if(!fileParent.exists()) {
			fileParent.mkdirs();
		}

		try {
			
			
			fout = new FileOutputStream(outputFile, true);

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

	public String getBaseFilePath() {
		return baseFilePath;
	}

	public void setBaseFilePath(String baseFilePath) {
		this.baseFilePath = baseFilePath;
	}
	
}
