package com.baidu.beidou.util.akadriver.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.util.BeidouConfig;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.akadriver.bo.AkaBeidouResult;
import com.baidu.beidou.util.akadriver.bo.AkaCheckInfo;
import com.baidu.beidou.util.akadriver.bo.AkaKwCheckInfo;
import com.baidu.beidou.util.akadriver.bo.AkaUnitCheckInfo;
import com.baidu.beidou.util.akadriver.constant.Constant;
import com.baidu.beidou.util.akadriver.exception.AkaException;
import com.baidu.beidou.util.akadriver.protocol.AdvRequestPacket;
import com.baidu.beidou.util.akadriver.protocol.AkaNsHead;
import com.baidu.beidou.util.akadriver.protocol.AkaRequest;
import com.baidu.beidou.util.akadriver.protocol.AkaResponse;
import com.baidu.beidou.util.akadriver.service.AkaDriver;
import com.baidu.beidou.util.akadriver.transform.AkaKwCheckInfoTransformer;
import com.baidu.beidou.util.akadriver.transform.AkaKwResultInfoTransformer;
import com.baidu.beidou.util.akadriver.transform.AkaPatrolUnitCheckInfoTransformer;
import com.baidu.beidou.util.akadriver.transform.AkaPatrolUnitResultInfoTransformer;
import com.baidu.beidou.util.akadriver.transform.AkaPicCheckInfoTransformer;
import com.baidu.beidou.util.akadriver.transform.AkaPicResultInfoTransformer;
import com.baidu.beidou.util.akadriver.transform.AkaUnitCheckInfoTransformer;
import com.baidu.beidou.util.akadriver.transform.AkaUnitResultInfoTransformer;
import com.baidu.beidou.util.rpc.McPacker;
import com.baidu.beidou.util.socket.InstantSocketDriver;
import com.baidu.beidou.util.socket.NsHead;
import com.baidu.beidou.util.socket.exception.SocketConnectException;
import com.baidu.mcpack.McpackException;

public class AkaDriverBeidouImpl extends InstantSocketDriver implements
		AkaDriver {

	private static final Log log = LogFactory.getLog(AkaDriverBeidouImpl.class);

	private int MAX_TASK;

	protected AkaDriverBeidouImpl() {
		super();
		properties = Constant.CONFIG_MEM_POP;
		String maxTaskStr = properties.getProperty("MAX_TASK");
		try {
			MAX_TASK = Integer.parseInt(maxTaskStr);
			if (MAX_TASK < 1) {
				LogUtils.fatal(log, "wrong value of aka MAX_TASK=" + MAX_TASK);
				MAX_TASK = 1;
			}
		} catch (NumberFormatException e) {
			LogUtils.fatal(log, "wrong value of aka MAX_TASK");
			MAX_TASK = 1;
		}

	}

	public List<AkaBeidouResult> getAkaUnitResultInfoListForUnit(
			final List<AkaUnitCheckInfo> akaCheckInfoList) throws AkaException {
		return askAka(BeidouConfig.AKA_LITERAL_CLIENT,
				BeidouConfig.AKA_LIST_TYPE, akaCheckInfoList,
				new AkaUnitCheckInfoTransformer(),
				new AkaUnitResultInfoTransformer());
	}
	
	public List<AkaBeidouResult> getAkaPatrolUnitResultInfoListForUnit(
			final List<AkaUnitCheckInfo> akaCheckInfoList) throws AkaException {
		return askAka(BeidouConfig.AKA_LITERAL_CLIENT,
				BeidouConfig.AKA_LIST_TYPE, akaCheckInfoList,
				new AkaPatrolUnitCheckInfoTransformer(),
				new AkaPatrolUnitResultInfoTransformer());
	}

	public List<AkaBeidouResult> getAkaPicResultInfoListForUnit(
			final List<AkaCheckInfo> akaCheckInfoList) throws AkaException {
		return askAka(BeidouConfig.AKA_PICTURE_CLIENT,
				BeidouConfig.AKA_LIST_TYPE, akaCheckInfoList,
				new AkaPicCheckInfoTransformer(),
				new AkaPicResultInfoTransformer());
	}

	public List<AkaBeidouResult> getAkaResultInfoListForKw(
			final List<AkaKwCheckInfo> akaCheckInfoList) throws AkaException {
		return askAka(BeidouConfig.AKA_KW_CLIENT, BeidouConfig.AKA_LIST_TYPE,
				akaCheckInfoList, new AkaKwCheckInfoTransformer(),
				new AkaKwResultInfoTransformer());
	}

	@SuppressWarnings("unchecked")
	private List<AkaBeidouResult> askAka(int client, int listtype,
			List akaCheckInfoList, Transformer checktransformer,
			Transformer outTransformer) throws AkaException {
		int length = akaCheckInfoList.size();
		int toIndex = 0;
		List<AkaBeidouResult> result = new ArrayList<AkaBeidouResult>(length);
		long start = 0;
		for (int index = 0; index < length; index = toIndex) {
			toIndex = index + MAX_TASK;
			if (toIndex > length) {
				toIndex = length;
			}
			start = System.currentTimeMillis();
			LogUtils.businessInfo(null, "aka audit task_num="
					+ (toIndex - index));
			result.addAll(_askAka(client, listtype, akaCheckInfoList.subList(index, toIndex),
					checktransformer, outTransformer));
			LogUtils.businessInfo(null, "aka audit end time="
					+ (System.currentTimeMillis() - start) + "ms");
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private List<AkaBeidouResult> _askAka(int client, int listtype,
			List akaCheckInfoList, Transformer checktransformer,
			Transformer outTransformer) throws AkaException {
		assert (akaCheckInfoList.size() > 0) : "akaCheckInfoList cannot be zero";

		log.info("begin to ask aka...");
		String err = "";// 错误信息
		List<AkaBeidouResult> result = new ArrayList<AkaBeidouResult>();
		try {
			// 填充NsHead
			AkaNsHead nshead_req = new AkaNsHead(client);
			nshead_req.setReserved(listtype);
			// 填充协议
			List<AdvRequestPacket> requestPackets = (List<AdvRequestPacket>) CollectionUtils
					.collect(akaCheckInfoList, checktransformer);
			AkaRequest request = new AkaRequest();
			request.setReqCon(requestPackets);
			byte[] content = McPacker.pack(request);

			nshead_req.setBodyLen(content.length);
			log.info("[REQUEST] request nshead: " + nshead_req);
//			log.info("[REQUEST] request content: " + request);
			// 发送请求
			connectServer();
			out.write(nshead_req.toBytes());
			out.write(content);
			out.flush();

			// aka 处理中...

			// 处理结果
			// 读取NsHead
			byte[] nshead_bytes = new byte[NsHead.NSHEAD_LEN];
			readBody(nshead_bytes);
			AkaNsHead nshead_res = new AkaNsHead(nshead_bytes);
			log.info("[RESPONSE] response nshead: " + nshead_res);
			// 读取返回体
			long bodyLen = nshead_res.getBodyLen();
			byte[] content_bytes = new byte[(int) bodyLen];
			readBody(content_bytes);
			AkaResponse response = McPacker.unpack(content_bytes,
					AkaResponse.class);
//			log.info("[RESPONSE] response content: " + response);
			if (response.getStatus() != Constant.STATUS_OK) {
				err = "exception in aka and the status code is "
						+ response.getStatus();
				log.error(err);
				throw new AkaException(err);
			}
			// 封装返回体
			Transformer rettransformer = outTransformer;
			result = (List<AkaBeidouResult>) CollectionUtils.collect(response
					.getResCon(), rettransformer);

			if (result.size() != requestPackets.size()) {
				err = "adv num inconsistent: request num is "
						+ requestPackets.size() + "and actual num is "
						+ requestPackets.size();
				log.error(err);
				throw new AkaException(err);
			}
			
		} catch (IOException e) {
			log.error("io exception when communicate with aka", e);
			throw new AkaException("io exception when communicate with aka", e);
		} catch (McpackException e) {
			log.error("mcpack exception when communicate with aka", e);
			throw new AkaException(
					"mcpack exception when communicate with aka", e);
		} catch (SocketConnectException e) {
			log.error("socket connect exception when communicate with aka", e);
			throw new AkaException(
					"socket connect exception when communicate with aka", e);
		} catch (InternalException e) {
			log.error("io exception when communicate with aka", e);
			throw new AkaException("io exception when communicate with aka", e);
		}

		return result;
	}

}
