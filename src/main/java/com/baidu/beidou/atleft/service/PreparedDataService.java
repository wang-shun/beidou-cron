package com.baidu.beidou.atleft.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.baidu.beidou.atleft.bo.TradeInfo;
import com.baidu.beidou.atleft.util.AtLeftUtil;

public class PreparedDataService {
	private final static Log LOG = LogFactory.getLog(PreparedDataService.class);
	private final static String SEP = "\t";
	
	private Map<Integer,List<TradeInfo>> shardTradeMap = new HashMap<Integer,List<TradeInfo>>();
	
	/**
	 * 加载文件
	 */
	public Map<Integer,List<TradeInfo>> loadFile(String file) throws IOException {
		File tradeFile = new File(file);
		if (!tradeFile.exists()) {
			LOG.error("trade file not exist file path : " + file);
		}
		FileInputStream inputStream = null;
		BufferedReader reader = null;
		try {
			inputStream = new FileInputStream(tradeFile);
			reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String line = reader.readLine();
			while(!StringUtils.isEmpty(line)) {
				String[] columes = line.split(SEP);
				Integer shard = (Integer.parseInt(columes[0]) >> 6) % 8;
				List<TradeInfo> tradeList = shardTradeMap.get(shard);
				
				if (tradeList == null) {
					tradeList = new ArrayList<TradeInfo>();
					shardTradeMap.put(shard, tradeList);
				}
				Integer tradeId = Integer.parseInt(columes[1]);
				Integer firstTradeId = tradeId / 100;
				tradeList.add(new TradeInfo(Integer.parseInt(columes[0]),tradeId,
						firstTradeId,columes[2],columes[3],columes[4],columes[5]));
				line = reader.readLine();
			}
		} catch(FileNotFoundException e) {
			LOG.error("file not found ", e);
		} catch (UnsupportedEncodingException e) {
			LOG.error("unsupported the encoding" , e);
		} catch (IOException e) {
			LOG.error("read file occur io exception" , e);
		} catch (NumberFormatException e) {
			LOG.error("convert string to integer error" , e);
		} catch(Exception e) {
			LOG.error("error" , e);
		}
		finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (reader != null) {
				reader.close();
			}
		}
		return shardTradeMap;
	}
	
	/**
	 * 分8个线程操作8个数据库
	 */
	public void submit(ApplicationContext context,CountDownLatch doneSignal) {
		for (Map.Entry<Integer, List<TradeInfo>> entry : shardTradeMap.entrySet()) {
			AtLeftTradeExcutor.submitTask(entry.getKey(), entry.getValue(), context,doneSignal);
		}
		
		AtLeftTradeExcutor.close();
	}

}
