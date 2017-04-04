/*******************************************************************************
 * CopyRight (c) 2000-2012 Baidu Online Network Technology (Beijing) Co., Ltd. All rights reserved.
 * Filename:    FlashDecodeTaskImpl.java
 * Creator:     <a href="mailto:xuxiaohu@baidu.com">Xu,Xiaohu</a>
 * Create-Date: 2013-6-17 下午5:50:45
 *******************************************************************************/
package com.baidu.beidou.cprounit.task.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.service.UbmcService;
import com.baidu.beidou.cprounit.task.FlashDecodeTask;
import com.baidu.chuangyi.flash.decode.DecodeResult;
import com.baidu.chuangyi.flash.decode.FlashDecoder;

/**
 * FlashDecodeTaskImpl
 * 
 * @author <a href="mailto:xuxiaohu@baidu.com">Xu,Xiaohu</a>
 * @version 2013-6-17 下午5:50:45
 */
public class FlashDecodeTaskImpl implements FlashDecodeTask {
	private static final Log logger = LogFactory
			.getLog(FlashDecodeTaskImpl.class);

	//有空字段时候，以“-”标识；
	private final static String EMPTY_DATA_POSITION = "-";
	
	/**
	 * 字符编码
	 */
	private final static String CHARSET_NAME="utf8";
	/**
	 * flash 相关信息处理输入文件
	 */
	private String FLASH_INFO_INPUT_FILE;

	/**
	 * flash 相关信息处理输出文件
	 */
	private String FLASH_INFO_OUTPUT_FILE;
	
	private String DRMC_MATPREFIX;
	
	private UbmcService ubmcService;
	
	private FlashDecoder flashDecoder;
	
	
	public boolean dealFlashDecode(boolean isFull){
		String inputFile = FLASH_INFO_INPUT_FILE;
		String outputFile = FLASH_INFO_OUTPUT_FILE;
		if(isFull){
			String suffix = ".full";
			inputFile = inputFile + suffix;
			outputFile = outputFile + suffix;
		}
	
		return dealFlashDec(inputFile, outputFile);
	}

	private boolean dealFlashDec(String inputFile, String outputFile) {
		if (inputFile == null
				|| inputFile.trim().length() == 0) {
			logger.error("The path "+inputFile+" is empty!");
			return false;
		}

		File file = new File(inputFile);
		if (!file.exists()) {
			logger.error(inputFile + " is not exited!");
			return false;
		} else if (file.isDirectory()) {
			logger.error(inputFile + " is a directory!");
			return false;
		}

		if (!file.canRead() || file.length() == 0) {
			logger.error(inputFile + " is not readable!");
			return false;
		}

		File outfile = new File(outputFile);
		if (outfile.exists()) {
			logger.info("Delete older out put file!");
			try {
				outfile.delete();
			} catch (Exception e) {
				logger.error("Fail to delete "+outputFile+" !", e);
				return false;
			}
		}

		try {
			if(!outfile.exists()){
				outfile.createNewFile();
			}
		} catch (IOException e1) {
			logger.error("Fail to create "+outputFile+" !", e1);
			return false;
		}
		
		List<String> needProccessLines = new ArrayList<String>();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				//s.id,s.wuliaoType, s.mcId, s.mcVersionId, s.fileSrc
				needProccessLines.add(line);
			}
		} catch (Exception e) {
			logger.error("read file lines fail " + file + " " + e.getMessage());
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (fileReader != null) {
					fileReader.close();
				}
				
			} catch (IOException e) {
				logger.error("close file error", e);
			}
		}

		if(needProccessLines.size() == 0){
			return true;
		}
		
		OutputStreamWriter outputStreamWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(outfile);
			outputStreamWriter = new OutputStreamWriter(fileOutputStream, CHARSET_NAME);
			bufferedWriter = new BufferedWriter(outputStreamWriter);
			for(String line : needProccessLines){
				String flashInfo = proccessData(line);
				if(flashInfo != null){
					bufferedWriter.write(flashInfo);
					bufferedWriter.newLine();
				}
			}
		} catch (Exception e) {
			logger.error("Fail to decode flash information ! error msg:" + e.getMessage());
		}finally {
			try {
				if (bufferedWriter != null) {
					bufferedWriter.close();
				}
				if (outputStreamWriter != null) {
					outputStreamWriter.close();
				}
			} catch (IOException e) {
				logger.error("close file error", e);
			}
		} 
		
		return true;
	}
	
	private String proccessData(String line){
		//s.id, s.wuliaoType, s.mcId, s.mcVersionId, s.fileSrc
		if(line == null || line.trim().length() == 0){
			return null;
		}
		
		String [] arr = line.split(",");
		//s.id, s.wuliaoType, s.mcId, s.mcVersionId 是必须的
		if(arr == null || arr.length < 4){
			return null;
		}
		
		//如果wuliaoType为1，不用去获取
		if("1".equals(arr[1])){
			return null;
		}
		Long mcId = 0l;
		Integer versionId = 0;
		try {
			mcId = Long.valueOf(arr[2]);
			versionId = Integer.valueOf(arr[3]);
		} catch (NumberFormatException e) {
			logger.error("Fail to get mcid or versionId", e);
			return null;
		}
		
		String url = null;
		//如果mcId 及mcVersionId为0，通过filesrc去获取
		if(mcId == 0 || versionId == 0){
			if(arr.length < 5){
				//没有fileSrc
				return null;
			}
			String fileSrc = arr[4];
			
			url = DRMC_MATPREFIX+fileSrc;
			//url = "http://drmcmm.baidu.com/media/"+fileSrc;
		}else{
			url = ubmcService.getTmpUrl(mcId, versionId);
		}
		
		DecodeResult result = flashDecoder.decodeByUrl(url);
		if(result.getStatus() == 0){
			String message = result.getMessage();
			StringBuilder sb = new StringBuilder();
			sb.append(arr[0]);
			sb.append("\t");
			
			if(message == null || (message=message.trim()).length() == 0){
				sb.append(EMPTY_DATA_POSITION);
				
			}else{
				sb.append(message);
			}
			return sb.toString();
		}else{
			return null;
		}
	}

	/**
	 * @return the fLASH_INFO_INPUT_FILE
	 */
	public String getFLASH_INFO_INPUT_FILE() {
		return FLASH_INFO_INPUT_FILE;
	}

	/**
	 * @param fLASH_INFO_INPUT_FILE the fLASH_INFO_INPUT_FILE to set
	 */
	public void setFLASH_INFO_INPUT_FILE(String fLASH_INFO_INPUT_FILE) {
		FLASH_INFO_INPUT_FILE = fLASH_INFO_INPUT_FILE;
	}

	/**
	 * @return the fLASH_INFO_OUTPUT_FILE
	 */
	public String getFLASH_INFO_OUTPUT_FILE() {
		return FLASH_INFO_OUTPUT_FILE;
	}

	/**
	 * @param fLASH_INFO_OUTPUT_FILE the fLASH_INFO_OUTPUT_FILE to set
	 */
	public void setFLASH_INFO_OUTPUT_FILE(String fLASH_INFO_OUTPUT_FILE) {
		FLASH_INFO_OUTPUT_FILE = fLASH_INFO_OUTPUT_FILE;
	}

	/**
	 * @return the dRMC_MATPREFIX
	 */
	public String getDRMC_MATPREFIX() {
		return DRMC_MATPREFIX;
	}

	/**
	 * @param dRMC_MATPREFIX the dRMC_MATPREFIX to set
	 */
	public void setDRMC_MATPREFIX(String dRMC_MATPREFIX) {
		DRMC_MATPREFIX = dRMC_MATPREFIX;
	}

	/**
	 * @return the ubmcService
	 */
	public UbmcService getUbmcService() {
		return ubmcService;
	}

	/**
	 * @param ubmcService the ubmcService to set
	 */
	public void setUbmcService(UbmcService ubmcService) {
		this.ubmcService = ubmcService;
	}

	/**
	 * @return the flashDecoder
	 */
	public FlashDecoder getFlashDecoder() {
		return flashDecoder;
	}

	/**
	 * @param flashDecoder the flashDecoder to set
	 */
	public void setFlashDecoder(FlashDecoder flashDecoder) {
		this.flashDecoder = flashDecoder;
	}
	
}