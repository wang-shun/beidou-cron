/**
 * 2009-4-28 下午04:02:28
 */
package com.baidu.beidou.util.file;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class IteratorObjectWriter<T> {

	private ObjectOutputStream obj = null;
	public IteratorObjectWriter(String fileName) throws FileNotFoundException, IOException{
		obj = new ObjectOutputStream(new FileOutputStream(fileName));
	}
	/**
	 * 存储记录
	 * @author zengyunfeng
	 * @param list
	 * @param cacheFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void persistentCache(final List<T> list)
			throws IOException {
		if (list == null) {
			return;
		}

		for (T bo : list) {
			obj.writeObject(bo);
		}

	}
	
	public void close() throws IOException{
		obj.close();
	}
}
