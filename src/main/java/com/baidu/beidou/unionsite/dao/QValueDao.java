/**
 * 2009-4-28 下午03:58:21
 */
package com.baidu.beidou.unionsite.dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.baidu.beidou.unionsite.bo.QValue;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public interface QValueDao {

	void persistentCache(final List<QValue> list, final String cacheFile)
			throws FileNotFoundException, IOException ;

}
