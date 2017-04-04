package com.baidu.beidou.util.bmqdriver.service;

import java.util.List;

import com.baidu.beidou.util.bmqdriver.bo.BmqUrlCheck;
import com.baidu.beidou.util.bmqdriver.bo.BmqUrlResult;
import com.baidu.bmq.BmqException;

/**
 * ClassName: BmqDriver
 * Function: TODO ADD FUNCTION
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 1.0.0
 * @since cpweb-325
 * @date 2011-10-10
 * @see 
 */
public interface BmqDriver {
	
	/**
	 * sendConnect: 发送消息时创建BmqClient
	 * @version BmqDriver
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public boolean sendConnect() throws BmqException;
	
	/**
	 * recvConnect: 接受消息时创建BmqClient
	 * @version BmqDriver
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public boolean recvConnect() throws BmqException;
	
	/**
	 * sendUrlRequest: 发送请求，审核targeturl的页面跳转情况
	 * @version BmqDriver
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public void sendUrlRequest(List<BmqUrlCheck> checkList) throws BmqException;
	
	/**
	 * subscribe: 接收消息前，先在队列的startPoint处开始订阅消息
	 * @version BmqDriver
	 * @author genglei01
	 * @date 2011-10-20
	 */
	public boolean subscribe(long startPoint) throws BmqException;
	
	/**
	 * hasMsgToRead: 判断bmq是否已经将数据发送过来，如果有则调用recvUrlResponse获取消息
	 * @version BmqDriver
	 * @author genglei01
	 * @date 2011-10-20
	 */
	public boolean hasMsgToRead() throws BmqException;
	
	/**
	 * recvUrlResponse: 接受消息，对消息进行后续处理
	 * @version BmqDriver
	 * @author genglei01
	 * @date 2011-10-20
	 */
	public BmqUrlResult recvUrlResponse() throws BmqException;
	
	/**
	 * unsubscribe: 取消订阅
	 * @version BmqDriver
	 * @author genglei01
	 * @date 2011-10-20
	 */
	public boolean unsubscribe() throws BmqException;
	
	/**
	 * disconnect: 断开BmqClient
	 * @version BmqDriver
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public boolean disconnect() throws BmqException;
}
