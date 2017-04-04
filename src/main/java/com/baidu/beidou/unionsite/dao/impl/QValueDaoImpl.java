/**
 * 2009-4-28 下午03:58:03
 */
package com.baidu.beidou.unionsite.dao.impl;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.bo.QValue;
import com.baidu.beidou.unionsite.bo.SiteStatBo;
import com.baidu.beidou.unionsite.dao.QValueDao;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class QValueDaoImpl implements QValueDao {
	private static final Log LOG = LogFactory.getLog(QValueDaoImpl.class);

	public void persistentCache(final List<QValue> list, final String cacheFile)
			throws FileNotFoundException, IOException {
		if (list == null || cacheFile == null) {
			return;
		}
		
		ObjectOutputStream output = new ObjectOutputStream(
				new FileOutputStream(cacheFile));
		
		for (QValue bo : list) {
			output.writeObject(bo);
		}

	}
	
	/**
	 * 读取一条记录
	 * 
	 * @author zengyunfeng
	 * @param input
	 * @return 返回null表示读到文件结尾
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public SiteStatBo next(ObjectInputStream input) throws IOException,
			ClassNotFoundException {
		if (input == null) {
			return null;
		}
		try {
			return (SiteStatBo) input.readObject();
		} catch (EOFException e) {
			return null;
		}
	}
}
