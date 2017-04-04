/**
 * AdBlacklistFilter.java 
 */
package com.baidu.beidou.bes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.util.string.StringUtil;
import com.google.common.collect.Maps;

/**
 * 对pm提供的物料黑名单进行过滤，黑名单量级会很小，直接load进内存处理
 * 
 * @author lixukun
 * @date 2014-01-14
 */
public class AdBlacklistFilter {
	private static final Log log = LogFactory.getLog(AdBlacklistFilter.class);
	private String inputFile;
	private String blacklistFile;
	private String outputFile;
	
	public AdBlacklistFilter() {
		
	}
	
	public AdBlacklistFilter(String inputFile, String blacklistFile, String outputFile) {
		this.inputFile = inputFile;
		this.blacklistFile = blacklistFile;
		this.outputFile = outputFile;
	}
	
	public void doFilter() {
		File input = new File(inputFile);
		File blacklist = new File(blacklistFile);
		
		if (!input.exists() || !input.isFile() || 
			!blacklist.exists() || !blacklist.isFile()) {
			return;
		}
		
		Map<Long, Long> dict = buildBlacklistDict(blacklist);
		if (dict != null) {
			output(input, new File(outputFile), dict);
		}
	}
	
	private void output(File input, File output, Map<Long, Long> dict) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output, true)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (StringUtil.isEmpty(line)) {
					continue;
				}
				
				String[] elements = StringUtil.split(line, "\t");
				if (elements == null || elements.length < 2) {
					continue;
				}
				
				long adid = StringUtil.convertLong(elements[0], 0);
				long tag = StringUtil.convertLong(elements[1], 0);
				
				StringBuilder restString = new StringBuilder();
				for (int i = 2; i < elements.length; i++) {
					restString.append("\t").append(elements[i]);
				}
				
				if (dict.containsKey(adid)) {
					tag = tag & (~dict.get(adid));
				}
				
				StringBuilder sb = new StringBuilder();
				sb.append(adid).append("\t").append(tag).append(restString.toString());
				
				writer.write(sb.toString());
				writer.newLine();
			}
			writer.flush();
		} catch (Exception ex) {
			log.error("AdBlacklistFilter|", ex);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (Exception e) {
				log.error("AdBlacklistFilter|", e);
			}
			
			try {
				if (writer != null)
					writer.close();
			} catch (Exception e) {
				log.error("AdBlacklistFilter|", e);
			}
		}
	}
	
	private Map<Long, Long> buildBlacklistDict(File blacklist) {
		BufferedReader reader = null;
		Map<Long, Long> dict = Maps.newHashMapWithExpectedSize(1000);
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(blacklist)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (StringUtil.isEmpty(line)) {
					continue;
				}
				
				String[] elements = StringUtil.split(line, "\t");
				if (elements == null || elements.length < 2) {
					continue;
				}
				
				long adid = StringUtil.convertLong(elements[1], 0);
				long tag = StringUtil.convertLong(elements[0], 0);
				
				if (dict.containsKey(adid)) {
					dict.put(adid, dict.get(adid) | tag);
				} else {
					dict.put(adid, tag);
				}
			}
			
		} catch (Exception ex) {
			log.error("AdBlacklistFilter|", ex);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (Exception e) {
				log.error("AdBlacklistFilter|", e);
			}
		}
		return dict;
	}
	
	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getBlackListFile() {
		return blacklistFile;
	}

	public void setBlackListFile(String blacklistFile) {
		this.blacklistFile = blacklistFile;
	}

	/**
	 * @return the outputFile
	 */
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * @param outputFile the outputFile to set
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 3) {
			log.error("AdBlacklistFilter|Usage: AdBlacklistFilter sourceFile blackListFile outputFile;");
			System.exit(1);
		}
		
		try {
			new AdBlacklistFilter(args[0], args[1], args[2]).doFilter();
		} catch (Exception ex) {
			log.error("AdBlacklistFilter|" + args[0] + "|" + args[1] + "|" + args[2], ex);
		}
	}

}
