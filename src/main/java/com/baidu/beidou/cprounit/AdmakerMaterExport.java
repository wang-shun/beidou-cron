package com.baidu.beidou.cprounit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprounit.service.UbmcService;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;

/**
 * 导出admaker物料
 * @version cpweb-567
 * @author genglei01
 * @date May 25, 2013
 */
public class AdmakerMaterExport {

	private static final Log log = LogFactory.getLog(AdmakerMaterExport.class);

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			log.error("Number of args must be 3");
			return;
		}
		
		// 1. spring配置导入，以便获取相关Service的bean
		String[] paths = new String[] { "applicationContext.xml","classpath:/com/baidu/beidou/user/applicationContext.xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		UbmcService ubmcService = (UbmcService) ctx.getBean("ubmcService");

		String materStatPath = args[0];
		String materUrlPath = args[1];
		String resultPath = args[2];

		String line = null;
		Map<Long, AdmakerMater> materMap = new HashMap<Long, AdmakerMater>();
		
		long totalTime1 = System.currentTimeMillis();
		long time1 = System.currentTimeMillis();
		try {
			// 2. 读取统计文件内容
			File materStatFile = new File(materStatPath);
			BufferedReader br = new BufferedReader(new FileReader(materStatFile));
			while (br.ready()) {
				try {
					line = br.readLine();
					
					String[] statLine = line.split("\t");
					Long id = Long.parseLong(statLine[0]);
					Long srchs = Long.parseLong(statLine[1]);
					Long clks = Long.parseLong(statLine[2]);
					AdmakerMater mater = new AdmakerMater();
					mater.setId(id);
					mater.setSrchs(srchs);
					mater.setClks(clks);
					
					materMap.put(id, mater);
				} catch (Exception ex) {
					log.error("parse the line failed from mater stat file in wuliao_type_export.sh");
					continue;
				}
			}
			br.close();
		} catch (Exception ex) {
			log.error("get data failed from mater stat file in wuliao_type_export.sh");
		}
		long time2 = System.currentTimeMillis();
		log.info("read from stat data finished in " + (time2 - time1) + " ms");
		
		
		try {
			// 3. 读取URL文件内容，并写入文件
			time1 = System.currentTimeMillis();
			
			File materUrlFile = new File(materUrlPath);
			BufferedReader br = new BufferedReader(new FileReader(materUrlFile));
			
			File resultFile = new File(resultPath);
			BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile, true));
			
			List<RequestBaseMaterial> requests = new ArrayList<RequestBaseMaterial>();
			while (br.ready()) {
				try {
					line = br.readLine();
					
					String[] statLine = line.split("\t");
					Long id = Long.parseLong(statLine[0]);
					Long mcId = Long.parseLong(statLine[1]);
					Integer versionId = Integer.parseInt(statLine[2]);
					
					AdmakerMater mater = materMap.get(id);
					if (mater == null) {
						continue;
					}
					mater.setMcId(mcId);
					mater.setVersionId(versionId);
					
					RequestLite request = new RequestLite(mcId, versionId);
					requests.add(request);
				} catch (Exception ex) {
					log.error("parse the line failed from mater stat file in wuliao_type_export.sh");
					continue;
				}
			}
			time2 = System.currentTimeMillis();
			log.info("read from unit data finished in " + (time2 - time1) + " ms");
			
			// 4. 获取临时URL
			time1 = System.currentTimeMillis();
			Map<RequestBaseMaterial, String> urlMap = ubmcService.generateMaterUrl(requests);
			time2 = System.currentTimeMillis();
			log.info("generate unit url finished in " + (time2 - time1) + " ms");
			
			// 5. 填充临时URL信息
			time1 = System.currentTimeMillis();
			for (AdmakerMater mater : materMap.values()) {
				RequestLite request = new RequestLite(mater.getMcId(), mater.getVersionId());
				
				String url = urlMap.get(request);
				if (StringUtils.isEmpty(url)) {
					mater.setUrl("");
				} else {
					mater.setUrl(url);
				}
				
				printResult(bw, mater);
			}
			time2 = System.currentTimeMillis();
			log.info("print unit result with url finished in " + (time2 - time1) + " ms");
			
			br.close();
			bw.close();
		} catch (Exception ex) {
			log.error("get data failed from mater stat file in wuliao_type_export.sh");
		}
		
		long totalTime2 = System.currentTimeMillis();
		log.info("export finished in " + (totalTime2 - totalTime1) + " ms");
	}
	
	public static void printResult(BufferedWriter bw, AdmakerMater mater) {
		try {
			bw.write(mater.getId() + "\t" + mater.getUrl() + "\t" + mater.getSrchs() + "\t" + mater.getClks());
			bw.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class AdmakerMater {
	private Long id;
	private Long srchs;
	private Long clks;
	private Long mcId;
	private Integer versionId;
	private String url;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getSrchs() {
		return srchs;
	}
	public void setSrchs(Long srchs) {
		this.srchs = srchs;
	}
	public Long getClks() {
		return clks;
	}
	public void setClks(Long clks) {
		this.clks = clks;
	}
	public Long getMcId() {
		return mcId;
	}
	public void setMcId(Long mcId) {
		this.mcId = mcId;
	}
	public Integer getVersionId() {
		return versionId;
	}
	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
