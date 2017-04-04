package com.baidu.beidou.message.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.bigpipe.client.NoneBlockingBigpipePubClient;
import com.baidu.beidou.bigpipe.transport.SendFutrue;
import com.baidu.beidou.message.constant.MessageConstant;
import com.baidu.beidou.message.request.Content;
import com.baidu.beidou.message.request.MessageRequest;
import com.baidu.beidou.util.page.DataPage;
import com.baidu.beidou.util.string.StringUtil;

/**
 * 推送消费突变消息
 * caichao
 */
public class MessageSend {
	private final static Log LOG = LogFactory.getLog(MessageSend.class);
	private final static String SEP = "\t";
	private final static FastDateFormat FORMAT = FastDateFormat.getInstance("MM月dd日");
	private final static FastDateFormat OFFSET_DATE = FastDateFormat.getInstance("yyyy-MM-dd");
	private final static FastDateFormat EVENT_TIME = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	private NoneBlockingBigpipePubClient client;
	private Integer RETRY_TIME = 10;//重复调用次数的次数
	private Integer PAGE_SIZE = 500;
	private final static int TIME_OUT = 1;//设置超时时间
	
	public NoneBlockingBigpipePubClient getClient() {
		return client;
	}

	public void setClient(NoneBlockingBigpipePubClient client) {
		this.client = client;
	}

	public Integer getRETRY_TIME() {
		return RETRY_TIME;
	}

	public void setRETRY_TIME(Integer rETRY_TIME) {
		RETRY_TIME = rETRY_TIME;
	}

	public Integer getPAGE_SIZE() {
		return PAGE_SIZE;
	}

	public void setPAGE_SIZE(Integer pAGE_SIZE) {
		PAGE_SIZE = pAGE_SIZE;
	}

	public void send(String filePath,String offSetPath) {
		List<MessageRequest> messageList = readFile(filePath,offSetPath);
		int pageNo = 1;
		boolean next = false;
		do {
			DataPage<MessageRequest> page = DataPage.getByList(messageList, PAGE_SIZE, pageNo);
			//发bigpipe
			List<Object> mlist = new LinkedList<Object>();
			List<MessageRequest> plist = page.getRecord();
			for(MessageRequest mr:plist){
				mlist.add(mr);
			}
			if (CollectionUtils.isEmpty(mlist)) {
				break;
			}
			SendFutrue futrue = client.publish(mlist);
			List<Object> failed = null;
			long begin = 0;
			try {
				begin = System.currentTimeMillis();
				failed = futrue.get(TIME_OUT,TimeUnit.MINUTES);
				long end = System.currentTimeMillis();
				
				Integer currentSize = calculateOffSet(offSetPath);
				if(failed != null && failed.size() == 0){
					LOG.info("send " + mlist.size() + " messages , user time " + (end - begin) + "ms");
					writeOffSet(offSetPath, currentSize + page.getRecord().size());
					pageNo++;
					next = page.hasNextPage();
				} else {
					for (int i=1 ; i < RETRY_TIME + 1;i++) {
						failed = retry(failed);
						if (CollectionUtils.isEmpty(failed)) {
							writeOffSet(offSetPath, currentSize + page.getRecord().size());
							pageNo++;
							next = page.hasNextPage();
							break;
						}
						LOG.error("retry " + i + " times , fail count is " + failed.size());
					}
					
					LOG.error("it is already retry " + RETRY_TIME + " times , so i will exit now");
					System.exit(1);
				}
				
			} catch (InterruptedException e) {
				LOG.error("InterruptedException", e);
			} catch (TimeoutException e) {
				e.printStackTrace();
				LOG.error("TimeOutException", e);
				long end = System.currentTimeMillis();
				LOG.error("time out use time " + (end-begin));
				System.exit(1);
			}

		} while (next);
		
	}
	
	private  List<Object> retry(List<Object> failed) throws InterruptedException, TimeoutException{
		SendFutrue futrue = client.publish(failed);
		List<Object> stillFailed = futrue.get(TIME_OUT,TimeUnit.MINUTES);
		if (stillFailed != null && stillFailed.size() == 0) {
			return new ArrayList<Object>(0);
		} else {
			return stillFailed;
		}
	}

	public List<MessageRequest> readFile(String filePath,String offSetPath) {
		File messageFile = new File(filePath);
		
		if (!messageFile.exists()) {
			LOG.error("message file is not exist path : " + filePath);
		}
		List<MessageRequest> messageList = new ArrayList<MessageRequest>();
		InputStreamReader stream = null;
		FileInputStream fis = null;
		BufferedReader reader = null;
		Integer offset = calculateOffSet(offSetPath);
		
		try {
			fis = new FileInputStream(messageFile);
			
			stream = new InputStreamReader(fis,"UTF-8");
			reader = new BufferedReader(stream);
			
			int count = 1;
			
			String line = reader.readLine();
			while (StringUtil.isNotEmpty(line)) {
				if (count > offset) {
					MessageRequest message = createMessage(line);
					if (message == null) {
						count ++ ;
						line = reader.readLine();
						continue;
					}
					messageList.add(message);
				}
				count ++ ;
				line = reader.readLine();
			}
			
		} catch(FileNotFoundException e){
			LOG.error("file not fount ", e);
		} catch (UnsupportedEncodingException e) {
			LOG.error("UnsupportedEncodingException", e);
		} catch (IOException e) {
			LOG.error("IOException", e);
		} finally {
			try{
				if (fis != null) {
					fis.close();
				}
				if (stream != null) {
					stream.close();
				}
				if (reader != null){
					reader.close();
				}
			}catch(Exception e){
				LOG.error("close resource fail", e);
			}
		}
		
		return messageList;
	}
	
	private MessageRequest createMessage(String line) {
		String[] lineArray = line.split(SEP);
		if (lineArray == null || lineArray.length != 4) {
			return null;
		}
		MessageRequest request = new MessageRequest();
		Integer userid = StringUtil.convertInt(lineArray[0], 0);
		String username = lineArray[1];
		BigDecimal beforeConsume = new BigDecimal(lineArray[2]);
		BigDecimal yestdayConsume = new BigDecimal(lineArray[3]);
		BigDecimal befMul100 = beforeConsume.multiply(new BigDecimal("100"));
		BigDecimal yestMul100 = yestdayConsume.multiply(new BigDecimal("100"));
		BigDecimal zero = new BigDecimal("0");
		
		if (befMul100.compareTo(zero) == 0) {
			beforeConsume = beforeConsume.setScale(0);
		}
		
		if (yestMul100.compareTo(zero) == 0 ) {
			yestdayConsume = yestdayConsume.setScale(0);
		}
		String fromDay = FORMAT.format(getDay(-2));
		String today = FORMAT.format(getDay(-1));
		
		//有可能分母为0 与一站式确定分母为0时比例没法算，直接传100
		BigDecimal ratio = null;
		if (befMul100.compareTo(zero) == 0) {
			ratio = new BigDecimal("100");
		}else {
			BigDecimal tmpRatio = beforeConsume.subtract(yestdayConsume).abs().divide(beforeConsume,2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
			ratio = tmpRatio.setScale(0, BigDecimal.ROUND_HALF_UP);
		}
		Content content = new Content(username, fromDay, today, ratio.toString(), beforeConsume.toString(), yestdayConsume.toString());
		request.setUuid(UUID.randomUUID().toString());
		request.setAppid(MessageConstant.APPID);
		if (beforeConsume.compareTo(yestdayConsume) > 0) {
			request.setTypeid(MessageConstant.CONSUME_DECREASE);
		} else {
			request.setTypeid(MessageConstant.CONSUME_INCREASE);
		}
		
		request.setUserid(userid.toString());
		request.setTime(EVENT_TIME.format(new Date()));
		request.setContent(content);
		return request;
	}
	
	/**
	 * 计算昨天，前天日期
	 * @param before
	 * @return
	 */
	private static Date getDay(int before) {
		Date date=new Date();//取时间
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DATE,before);//把日期往后增加一天.整数往后推,负数往前移动
		return calendar.getTime();
	}

	/**
	 * 计算上次处理的行数
	 * @param offSetPath
	 * @return
	 */
	private Integer calculateOffSet(String offSetPath) {
		File file = new File(offSetPath);
		FileInputStream fis = null;
		InputStreamReader stream = null;
		BufferedReader reader = null;
		
		if (!file.exists()) {
			return 0;
		}
		
		try {
			fis = new FileInputStream(file);
			stream = new InputStreamReader(fis,"UTF-8");
			reader = new BufferedReader(stream);
			String line = reader.readLine();
			String[] offSets = line.split(SEP);
			if (offSets.length == 2) {
				Date day = new Date();
				if (OFFSET_DATE.format(day).equals(offSets[0])) {
					return Integer.valueOf(offSets[1]);
				}
			} else {
				return 0;
			}
			
		} catch (FileNotFoundException e) {
			LOG.error("file is not fount [path] : " + offSetPath, e);
		} catch (UnsupportedEncodingException e) {
			LOG.error("UnsupportedEncodingException : " + offSetPath, e);
		} catch (IOException e) {
			LOG.error("IOException : " + offSetPath, e);
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
				if (stream != null) {
					stream.close();
				}
				if (reader != null) {
					reader.close();
				}
			}catch(Exception e) {
				LOG.error("close resource fail", e);
			}
		}
		return 0;
	}
	
	/**
	 * 记录每次处理行数
	 * @param offSetPath
	 * @param offset
	 */
	private void writeOffSet(String offSetPath,Integer offset) {
		File file = new File(offSetPath);
		FileOutputStream out = null;
		OutputStreamWriter stream = null;
		BufferedWriter writer = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			} 
			out = new FileOutputStream(file);
			stream = new OutputStreamWriter(out,"UTF-8");
			writer = new BufferedWriter(stream);
			String time = OFFSET_DATE.format(new Date());
			writer.write(time + SEP + offset);
			writer.flush();
		} catch (IOException e) {
			LOG.error("IOException", e);
		} finally {
			try {
				if (out != null) {
					out.close();
				} 
				if (stream != null) {
					stream.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch(Exception e) {
				LOG.error("close resource fail ", e);
			}
		} 
	}
	
	public static void main(String[] args) {
//		Float beforeConsume = 95.90f;
//		Float yestdayConsume = 172.79f;
//		Float ratio = -1.00f;
//		System.out.println((Math.abs(beforeConsume - yestdayConsume) / beforeConsume) * 100);
//		
//		System.out.println(beforeConsume > yestdayConsume);
//		System.out.println(ratio==-1);
		MessageSend send = new MessageSend();
		float a = 98.11f;
//		System.out.println(((Double)(Math.ceil(a))).intValue());
		BigDecimal beforeConsume = new BigDecimal("12.34");
		BigDecimal yestdayConsume = new BigDecimal("10.89");
		BigDecimal abs = beforeConsume.subtract(yestdayConsume).abs();
		System.out.println(abs.multiply(new BigDecimal("100")).divide(beforeConsume));
	}
}
