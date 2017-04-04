package com.baidu.beidou.unionsite.strategy.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.strategy.ISiteLinkPrefixGenerator;

/**
 * ClassName:GenerateSiteLinkPrefixByClickHistory
 * Function: 根据点击日志基础库匹配实现
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-7-8
 * @version  $Id: Exp $
 */
public class GenerateSiteLinkPrefixByClickHistory implements ISiteLinkPrefixGenerator{
	
	private Log log = LogFactory.getLog(GenerateSiteLinkPrefixByClickHistory.class);
	/** 基础库大小 */
	public static final int BASE_SIZE = 200000;
	/** 是否已经初始化 */
	private boolean initialized = false;
	
	/** 基础库文件名 */
	private String file = "";
	
	private Set<String> domainLib = new HashSet<String>();
	
	/** 辅助生成器。此处用“三点策略” */
	private ISiteLinkPrefixGenerator assitantGenerator;
	
	public String generatePrefix(String domain) {
		
		if (!initialized) {
			//需要初始化
			try {
				init();
			} catch (IOException e) {
				log.error(e);
				throw new RuntimeException(e.getMessage());
			}
		}
		
		if (domainLib.contains(domain)) {
			return PROTOCOL_PREFIX ;
		} else if (domainLib.contains(WWW_PREFIX + domain)) {
			return DEFAULT_PREFIX ;
		} 
		if (assitantGenerator != null) {
			return assitantGenerator.generatePrefix(domain);
		}
		return DEFAULT_PREFIX ;
	}
	
	public synchronized void init() throws IOException {
		if (initialized) {
			return;
		}
		
		String domain;
		BufferedReader br = new BufferedReader(new FileReader(file));
		domainLib = new HashSet<String>(BASE_SIZE);
		while ( ( domain = br.readLine()) != null ) {
			if (!org.apache.commons.lang.StringUtils.isEmpty(domain)) {
				domainLib.add(domain.trim());
			}
		}
		initialized = true;
	}

	public ISiteLinkPrefixGenerator getAssitantGenerator() {
		return assitantGenerator;
	}

	public void setAssitantGenerator(ISiteLinkPrefixGenerator assitantGenerator) {
		this.assitantGenerator = assitantGenerator;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

}
