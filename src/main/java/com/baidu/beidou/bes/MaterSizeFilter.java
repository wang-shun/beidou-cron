/**
 * SizeFilter.java 
 */
package com.baidu.beidou.bes;

import static com.baidu.beidou.bes.util.BesUtil.getMaterSizeHashCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.bes.util.BesUtil;
import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.util.string.StringUtil;

/**
 * 物料大小及尺寸过滤器(批量)
 * 
 * @author lixukun
 * @date 2013-12-24
 */
public class MaterSizeFilter implements MaterFilter {
	private static final Log log = LogFactory.getLog(MaterFilter.class);
	public static final String SIZES_SEP = ",";
	public static final String SIZE_INTERNAL_SEP = ":";
	
	private List<MaterSize> sizeList;
	private UbmcServiceExtension ubmcService;
	private boolean checkSize;
	private String sourceFile;
	
	public MaterSizeFilter() {
	}
	
	public MaterSizeFilter(String sourceFile) {
		this.sourceFile = sourceFile;
		initSizeFromSourceFile();
	}
	
	@Override
	public void doFilter(MaterContext context) {
		if (context == null || context.getUnitMaters() == null) {
			return;
		}
		
		Map<Integer, Integer> dict = buildDictionary();
		List<UnitMaterView> maters = context.getUnitMaters();
		List<UnitMaterView> filtedMaters = new ArrayList<UnitMaterView>(maters.size());
		for (UnitMaterView mater : maters) {
			if (fit(mater, dict)) {
				filtedMaters.add(mater);
			}
		}
		
		context.setUnitMaters(filtedMaters);
	}
	
	/**
	 * 构建尺寸匹配字典
	 * 
	 * @return
	 */
	protected Map<Integer, Integer> buildDictionary() {
		if (CollectionUtils.isEmpty(sizeList)) {
			return new HashMap<Integer, Integer>(0);
		}
		Map<Integer, Integer> dictionary = new HashMap<Integer, Integer>();
		for (MaterSize size : sizeList) {
			int key = getMaterSizeHashCode(size.getHeight(), size.getWidth());
			dictionary.put(key, size.getSize());
		}
		
		return dictionary;
	}
	
	private void initSizeFromSourceFile() {
		if (StringUtil.isEmpty(sourceFile)) {
			return;
		}
		File f = new File(BesUtil.getWorkPath() + "/" + sourceFile);
		if (!f.exists()) {
			return;
		}
		
		if (CollectionUtils.isEmpty(sizeList)) {
			sizeList = new ArrayList<MaterSize>();
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
					log.error("MaterFilter|line invalid|" + line);
					continue;
				}
				int width = StringUtil.convertInt(elements[1], -1);
				int height = StringUtil.convertInt(elements[2], -1);
				if (width <= 0 || height <= 0) {
					continue;
				}
				MaterSize size = new MaterSize();
				size.setHeight(height);
				size.setWidth(width);
				
				sizeList.add(size);
			}
		} catch (Exception ex) {
			log.error("MaterFilter|initSizeFromSourceFile|", ex);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	protected boolean fit(UnitMaterView mater, Map<Integer, Integer> dict) {
		if (mater == null || dict == null) {
			return false;
		}
		int key = getMaterSizeHashCode(mater.getHeight(), mater.getWidth());
		if (!dict.containsKey(key)) {
			return false;
		}
		
		// 不检查物料大小
		if (!checkSize) {
			return true;
		}
		
		Integer size = dict.get(key);
		if (size == null || size <= 0) {
			return true;
		}
		
		Integer matersize = ubmcService.getMaterSize(mater.getMcId(), mater.getMcVersionId());
		if (matersize == null) {
			return false;
		}
		
		return matersize <= size;
	}

	public List<MaterSize> getSizeList() {
		return sizeList;
	}

	public void setSizeList(List<MaterSize> sizeList) {
		this.sizeList = sizeList;
	}

	public void setUbmcService(UbmcServiceExtension ubmcService) {
		this.ubmcService = ubmcService;
	}

	public boolean isCheckSize() {
		return checkSize;
	}

	public void setCheckSize(boolean checkSize) {
		this.checkSize = checkSize;
	}


	public String getSourceFile() {
		return sourceFile;
	}


	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}
}
