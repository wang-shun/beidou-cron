/**
 * SimpleObjectWriter.java 
 */
package com.baidu.beidou.bes;

/**
 * 简单的对象输出格式化器<br/>
 * 继承后将对象的输出格式化成字符串<br/>
 * 
 * @author lixukun
 * @date 2014-03-10
 */
public interface SimpleObjectFormatter<T> {
	/**
	 * 格式化对象
	 * 
	 * @param obj
	 * @return
	 */
	String formatObject(T obj);
}
