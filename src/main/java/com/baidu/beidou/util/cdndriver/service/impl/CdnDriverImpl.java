package com.baidu.beidou.util.cdndriver.service.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.util.cdndriver.bo.AbstractCdnRequestInfo;
import com.baidu.beidou.util.cdndriver.bo.CdnPurgeRequestInfo;
import com.baidu.beidou.util.cdndriver.bo.CdnPurgeResponseInfo;
import com.baidu.beidou.util.cdndriver.bo.CdnQueryRequestInfo;
import com.baidu.beidou.util.cdndriver.bo.CdnQueryResponseInfo;
import com.baidu.beidou.util.cdndriver.bo.CdnQueryResponseItemInfo;
import com.baidu.beidou.util.cdndriver.exception.CdnException;
import com.baidu.beidou.util.cdndriver.protocol.CdnNsHead;
import com.baidu.beidou.util.cdndriver.service.CdnDriver;
import com.baidu.beidou.util.rpc.McPacker;
import com.baidu.beidou.util.socket.InstantSocketDriver;
import com.baidu.beidou.util.socket.NsHead;
import com.baidu.beidou.util.socket.exception.SocketConnectException;
import com.baidu.beidou.util.string.StringUtil;
import com.baidu.mcpack.McpackException;

public class CdnDriverImpl extends InstantSocketDriver implements CdnDriver {

	private static final Log log = LogFactory.getLog(CdnDriverImpl.class);
	
	private static final int PURGE_RES_OK = 200;
	
	private static final String CONFIG_FILE_NAME = "cdn";
	
	private static final String PURGE_FLAG = "purge";
	
	private static final String QUERY_FLAG = "query";
	
	protected CdnDriverImpl(){
		super();
		
		// 加载配置文件
		if(properties == null){
			properties = new Properties();
		}
		
		final ResourceBundle res = ResourceBundle.getBundle(CONFIG_FILE_NAME);
		Enumeration<String> en = res.getKeys();
		String key = null;
		String value = null;
		while (en.hasMoreElements()) {
			key = en.nextElement().trim();
			value = res.getString(key);
			properties.setProperty(key, value.trim());
		}
	}
	
	public static void printHelp(){
		String str = "\n   Usage: CdnDriverImpl purge /asset/reginfo.js /asset/siteinfo.js ...";
		str = str + "\nor Usage: CdnDriverImpl purge -f delFilePath ";
		str = str + "\nor Usage: CdnDriverImpl query accessId ";
		log.info(str);
		System.exit(0);
	}
	
	public Object purge(List<String> uriList) throws CdnException, McpackException{
		
		if(CollectionUtils.isEmpty(uriList)){
			String err = "purge uriList is empty ";
			log.error(err);
			throw new CdnException(err);
		}
		
		String cncServer = properties.getProperty("server_cnc_purge");
		String ctServer = properties.getProperty("server_ct_purge");
		if(cncServer == null || ctServer == null){
			String err = "purge cdn server is empty ";
			log.error(err);
			throw new CdnException(err);
		}
		
		List<String> accessIdList = new ArrayList<String>();
		
		// 循环网通和电信
		String[] serverArr = new String[]{cncServer, ctServer};
		for(String server : serverArr){
			
			properties.put("server", server);
			log.info("### server is " + server);
			
			AbstractCdnRequestInfo reqInfo = CdnPurgeRequestInfo.buildCdnReqInfo(uriList.toArray(new String[]{}));
			byte[] content_bytes = dispose(reqInfo);
			
			CdnPurgeResponseInfo resInfo = McPacker.unpack(content_bytes, CdnPurgeResponseInfo.class);
			if (resInfo == null || resInfo.getRes_code() != PURGE_RES_OK) {
				String err = "exception in cdn communication and the status code is " + resInfo.getRes_code();
				log.error(err);
				throw new CdnException(err);
			}
				
			String accessId = resInfo.getId();	
			log.info("### cdn purge access id: " + accessId);
			
			accessIdList.add(accessId);
		}
		
		return accessIdList;
	}
	
	public Object query(String accessId, boolean isPrintDetail) throws CdnException, McpackException{
		
		if(StringUtil.isEmpty(accessId)){
			String err = "query accessId is empty ";
			log.error(err);
			throw new CdnException(err);
		}
		
		String cncServer = properties.getProperty("server_cnc_query");
		String ctServer = properties.getProperty("server_ct_query");
		if(cncServer == null || ctServer == null){
			String err = "purge cdn server is empty ";
			log.error(err);
			throw new CdnException(err);
		}
		
		List<CdnQueryResponseInfo> queryResponseList = new ArrayList<CdnQueryResponseInfo>();
		
		// 循环网通和电信
		String[] serverArr = new String[]{cncServer, ctServer};
		for(String server : serverArr){
			
			properties.put("server", server);
			log.info("### server is " + server);
			
			AbstractCdnRequestInfo reqInfo = CdnQueryRequestInfo.buildCdnReqInfo(accessId);
			byte[] content_bytes = dispose(reqInfo);
			
			CdnQueryResponseInfo resInfo = McPacker.unpack(content_bytes, CdnQueryResponseInfo.class);
			if (resInfo == null) {
				String err = "exception in cdn communication and response is null";
				log.error(err);
				throw new CdnException(err);
			}
				
			queryResponseList.add(resInfo);
			
			// 打印查询的删除结果
			System.out.println("### CDN total query result: " + resInfo.getResult() + " (" + server + ")");
			
			// 打印无响应节点的名称
			String[] nodeNameArr = resInfo.getResponseless_nodes();
			if(!ArrayUtils.isEmpty(nodeNameArr)){
				for(String nodeName : nodeNameArr){
					System.out.println("### CDN query Responseless node: " + nodeName);
				}
			}
			
			// 打印各节点的应答
			if(isPrintDetail){
				Map<String, CdnQueryResponseItemInfo> itemInfoMap = resInfo.getResponses();
				if(!MapUtils.isEmpty(itemInfoMap)){
					for(Map.Entry<String, CdnQueryResponseItemInfo> entry : itemInfoMap.entrySet()){
						System.out.println("\t### detail node " + entry.getKey());
						System.out.println("\t\t### result " + entry.getValue().getResult());
						
						String[] downArr = entry.getValue().getDown();
						if(!ArrayUtils.isEmpty(downArr)){
							for(String host : downArr){
								System.out.println("\t\t### down " + host);
							}
						}
					}
				}
			}
		}
		
		return queryResponseList;
	}
	
	private byte[] dispose(AbstractCdnRequestInfo requestInfo) throws CdnException{
		
		try {
			// 填充NsHead
			CdnNsHead nshead_req = new CdnNsHead();
			
			// 填充协议
			byte[] content = McPacker.pack(requestInfo);
			nshead_req.setBodyLen(content.length);
			
			log.info("[REQUEST] request nshead: " + nshead_req);

			// 发送请求
			connectServer();
			out.write(nshead_req.toBytes());
			out.write(content);
			out.flush();

			// 处理中...

			// 处理结果
			// 读取NsHead
			byte[] nshead_bytes = new byte[NsHead.NSHEAD_LEN];
			readBody(nshead_bytes);
			CdnNsHead nshead_res = new CdnNsHead(nshead_bytes);
			log.info("[RESPONSE] response nshead: " + nshead_res);
			
			// 读取返回体
			long bodyLen = nshead_res.getBodyLen();
			byte[] content_bytes = new byte[(int) bodyLen];
			readBody(content_bytes);
			
			return content_bytes;
			
		} catch (IOException e) {
			log.error("io exception when communicate with cdn", e);
			throw new CdnException("io exception when communicate with cdn", e);
		} catch (McpackException e) {
			log.error("mcpack exception when communicate with cdn", e);
			throw new CdnException(
					"mcpack exception when communicate with cdn", e);
		} catch (SocketConnectException e) {
			log.error("socket connect exception when communicate with cdn", e);
			throw new CdnException(
					"socket connect exception when communicate with cdn", e);
		} catch (InternalException e) {
			log.error("io exception when communicate with cdn", e);
			throw new CdnException("io exception when communicate with cdn", e);
		}

	}

	public static void main(String[] args) throws CdnException, McpackException{
		if(args != null && args.length <2 ){
			printHelp();
		}
		
		CdnDriverImpl impl = new CdnDriverImpl();
		String flag = args[0];
		
		if(PURGE_FLAG.equalsIgnoreCase(flag)){
			
			List<String> uriList = new ArrayList<String>();
			
			// 如果要删除的资源在文件中
			if("-f".equals(args[1])){
				
				if(args.length < 3){
					printHelp();
				}
				
				log.info("get resource from file");
				
				BufferedReader br = null;
				try{
					br = new BufferedReader(new FileReader(args[2]));
					String uri = null;
					while((uri=br.readLine()) != null){
						uriList.add(uri.trim());
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
				finally{
					try{
						if(br!=null){
							br.close();
						}
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			else{ // 手动输入要删除的资源
				
				log.info("get resource from cmd line");
				
				int len = args.length;
				for(int i = 1; i < len; i++){
					if(args[i] != null){
						uriList.add(args[i]);
					}
				}
			}
			
			impl.purge(uriList);
			
		}
		else if(QUERY_FLAG.equalsIgnoreCase(flag)){
			log.info("accessId is " + args[1]);
			
			if(QUERY_FLAG.toUpperCase().equals(flag)){
				impl.query(args[1], true);
			}
			else{
				impl.query(args[1], false);
			}
		}
		else{
			printHelp();
		}
		
	}
}
