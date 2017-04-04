/**
 * FileResultOutput.java 
 */
package com.baidu.beidou.bes;

import static com.baidu.beidou.bes.util.BesUtil.getAbsoluteFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.lib.StringUtil;
import org.springframework.beans.factory.DisposableBean;

/**
 * 输出为文件<br/>
 * 一个单线程的文件输出类，多线程处理数据的时候，不应该同时进行文件的写入，避免写入混乱<br/>
 * 多线程处理数据后，提交到这里进行单线程的文件I/O<br/>
 * 设置了一个固定大小的list，避免因为磁盘问题造成的内存泄漏<br/>
 * 
 * @author lixukun
 * @date 2013-12-26
 */
public class FileResultOutput implements DisposableBean {
	private static final Log log = LogFactory.getLog(FileResultOutput.class);
	// 设置一个队列
	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 120, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(30000));
	
	private final BufferedWriter writer;
	
	public FileResultOutput(String outputFile, String company) throws Exception {
		outputFile = getAbsoluteFile(outputFile, company);
		File f = new File(outputFile);
		if (f.exists()) {
			f.delete();
		}
		FileOutputStream out = new FileOutputStream(f, false);
		writer = new BufferedWriter(new OutputStreamWriter(out));
	}

	public void submit(final String output) {
		executor.execute(new Runnable() {
			
			@Override
			public void run() {
				if (writer == null) {
					return;
				}
				
				try {
					if (!StringUtil.isEmpty(output)) {
						writer.write(output);
//						writer.flush();
					}
				} catch (IOException e) {
					log.error("FileResultOutput|", e);
				}
			}
		});
	}
	
	@Override
	public void destroy() throws Exception {
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.HOURS);
		if (writer != null) {
			writer.flush();
			writer.close();
		}
		log.info("FileResultOutput terminated");
	}
}
