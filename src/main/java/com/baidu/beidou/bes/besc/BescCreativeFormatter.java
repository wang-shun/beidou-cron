/**
 * BescUnitMaterFormatter.java 
 */
package com.baidu.beidou.bes.besc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.bes.SimpleObjectFormatter;
import com.baidu.beidou.bes.util.BesUtil;
import com.baidu.beidou.util.BeidouConstant;
import com.baidu.beidou.util.string.StringUtil;

/**
 * Besc输出格式器
 * 
 *  creativeId		//创意id【新加入】
 *  dsp原id			//同创意id
	mcId
	mcVersionId
	wuliaoType		//物料type
	adtradeId		//使用新体系二级行业ID，北斗创意需要做行业ID转换处理
	adsizeId		//创意尺寸ID
	width
	height
	dspId			//北斗作为一家独立的DSP,id为1
	targetUrl		//点击URL，文字创意使用showURL
	advertiserUrl	//广告主URL
	createTime		//创意创建时间
	beautyLevel		//美观度分档：0代表未评定，1代表低，2代表中，3代表高
	vulgarLevel		//低俗度分档：0代表未评定，1代表低，2代表中，3代表高

 * @author lixukun
 * @date 2014-03-10
 */
public class BescCreativeFormatter implements SimpleObjectFormatter<BescCreativeInfo> {
	private static final Log log = LogFactory.getLog(BescCreativeFormatter.class);
	private String adSizeSourceFile;
	private Map<String, Integer> adSizeMap;

	public BescCreativeFormatter(String adSizeSourceFile) {
		this.adSizeSourceFile = adSizeSourceFile;
		initFromFile();
	}
	
	private void initFromFile() {
		if (StringUtil.isEmpty(adSizeSourceFile)) {
			return;
		}
		File f = new File(BesUtil.getWorkPath() + "/" + adSizeSourceFile);
		if (!f.exists()) {
			return;
		}
		
		if (adSizeMap == null) {
			adSizeMap = new HashMap<String, Integer>();
		}
		
		BufferedReader reader = null;
		try {
			FileInputStream in = new FileInputStream(f);
			reader = new BufferedReader(new InputStreamReader(in));
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (StringUtil.isEmpty(line)) {
					continue;
				}
				String[] elements = StringUtil.split(line, "\t");
				if (elements == null || elements.length < 3) {
					log.error("BescCreativeFormatter|line invalid|" + line);
					continue;
				}
				int width = StringUtil.convertInt(elements[1], -1);
				int height = StringUtil.convertInt(elements[2], -1);
				int id = StringUtil.convertInt(elements[0], -1);
				if (width <= 0 || height <= 0 || id < 0) {
					continue;
				}
				
				String key = buildSizeKey(width, height);
				adSizeMap.put(key, id);
			}
		} catch (Exception ex) {
			log.error("BescCreativeFormatter|initSizeFromSourceFile|", ex);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	private int getAdSizeId(int width, int height) {
		String key = buildSizeKey(width, height);
		
		Integer sizeId = adSizeMap.get(key);
		if (sizeId == null) {
			return -1;
		}
		
		return sizeId.intValue();
	}
	
	private String buildSizeKey(int width, int height) {
		return width + "-" + height;
	}
	
	@Override
	public String formatObject(BescCreativeInfo obj) {
		if (obj == null) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(obj.getId()).append("\t")
		  .append(obj.getId()).append("\t")
		  .append(obj.getMcId()).append("\t")
		  .append(obj.getMcVersionId()).append("\t")
		  .append(obj.getWuliaoType()).append("\t")
		  .append(obj.getNewAdTradeId() / 100).append("\t")
		  .append(getAdSizeId(obj.getWidth(), obj.getHeight())).append("\t")
		  .append(obj.getWidth()).append("\t")
		  .append(obj.getHeight()).append("\t")
		  .append(BeidouConstant.BEIDOU_DSP_ID).append("\t")
		  .append(obj.getTargetUrl()).append("\t")
		  .append(obj.getWebsite()).append("\t")
		  .append(StringUtil.formatDate(obj.getChaTime(), "yyyy-MM-dd")).append("\t")
		  .append(obj.getBeauty_level()).append("\t")
		  .append(obj.getVulgar_level());
		
		return sb.toString();
	}


	public String getAdSizeSourceFile() {
		return adSizeSourceFile;
	}


	public void setAdSizeSourceFile(String adSizeSourceFile) {
		this.adSizeSourceFile = adSizeSourceFile;
	}

}
