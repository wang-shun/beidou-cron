/**
 * MaterSize.java 
 */
package com.baidu.beidou.bes;

import com.baidu.beidou.util.string.StringUtil;

/**
 * 物料尺寸\大小
 * 
 * @author lixukun
 * @date 2013-12-24
 */
public class MaterSize {
	private int height;		// 高度
	private int width;		// 宽度
	private int size;		// 物料大小
	
	public MaterSize() {
		
	}
	
	public MaterSize(int height, int width, int size) {
		this.height = height;
		this.width = width;
		this.size = size;
	}

	/**
	 * 物料高度
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * 物料宽度
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * 物料大小
	 * @return
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}
	
	/**
	 * 支持数字+单位的格式设置size, 如10k<br/>
	 * 目前支持后缀:k
	 * @param size
	 */
	public void setSizeByStr(String size) {
		if (size.endsWith("k") || size.endsWith("K")) {
			String num = size.substring(0, size.length() - 1);
			this.size = StringUtil.convertInt(num, 0) * 1024;
		}
	}
	
}
