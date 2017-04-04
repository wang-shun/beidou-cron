/**
 * FileRowMapper.java 
 */
package com.baidu.beidou.bes;

import java.util.Map;

/**
 * 标记了输入文件及改文件相关的字段匹配字典
 * 
 * @author lixukun
 * @date 2013-12-30
 */
public class FileRowMapper {
	private String inputFile;		// 输入文件
	private Map<String, String> fieldMapper;		//字段匹配字典
	
	public FileRowMapper() {
		
	}

	/**
	 * @return the inputFile
	 */
	public String getInputFile() {
		return inputFile;
	}

	/**
	 * @param inputFile the inputFile to set
	 */
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * @return the fieldMapper
	 */
	public Map<String, String> getFieldMapper() {
		return fieldMapper;
	}

	/**
	 * @param fieldMapper the fieldMapper to set
	 */
	public void setFieldMapper(Map<String, String> fieldMapper) {
		this.fieldMapper = fieldMapper;
	}
}
