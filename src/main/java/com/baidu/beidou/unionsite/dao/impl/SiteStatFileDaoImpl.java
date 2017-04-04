/**
 * 2009-4-23 下午04:21:55
 */
package com.baidu.beidou.unionsite.dao.impl;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Types;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import com.baidu.beidou.unionsite.bo.IPCookieBo;
import com.baidu.beidou.unionsite.bo.SiteStatBo;
import com.baidu.beidou.unionsite.bo.SiteStatExtBo;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.dao.SiteStatFileDao;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;
import com.baidu.beidou.util.JdbcTypeCast;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.dao.GenericDaoImpl;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class SiteStatFileDaoImpl extends GenericDaoImpl implements
		SiteStatFileDao {

	private static final Log LOG = LogFactory.getLog(SiteStatFileDaoImpl.class);
	private static final String FIELD_SPLITER = "\t";
	private static final String SUB_FIELD_SPLITER = "\\|";
	private static final String THIRD_FIELD_SPLITER = "\\*";
	private static final String SIZE_NAME = "statfilesize";
	private static final String START_NAME = "statfilestart";
	private static final String KEY_VALUE_SPLITER = ",";

	/**
	 * 返回cpro-stat站点统计数据的个数和起始序号
	 * 
	 * @author zengyunfeng
	 * @return 如何不存在返回{0,0}
	 */
	public int[] getFileRange() {
		int[] result = new int[2];
		List<Map<String, Object>> list = super.findBySql("SELECT value FROM beidoucap.sysnvtab WHERE NAME=? LIMIT 1", new Object[] { SIZE_NAME }, new int[] { Types.VARCHAR });
		if (list.isEmpty()) {
			result[0] = 0;
			super.executeBySql("INSERT INTO beidoucap.sysnvtab(`name`,`value`) values(?,?)", new Object[] { SIZE_NAME, String.valueOf(0) });
		} else {
			result[0] = JdbcTypeCast.CastToInt(list.get(0).get("value"));
		}

		list = super.findBySql("SELECT value FROM beidoucap.sysnvtab WHERE NAME=? LIMIT 1", new Object[] { START_NAME }, new int[] { Types.VARCHAR });
		if (list.isEmpty()) {
			result[1] = 0;
			super.executeBySql("INSERT INTO beidoucap.sysnvtab(`name`,`value`) values(?,?)", new Object[] { START_NAME, String.valueOf(0) });
		} else {
			result[1] = JdbcTypeCast.CastToInt(list.get(0).get("value"));
		}
		return result;
	}

	/**
	 * 存储cpro-stat站点统计数据的个数和起始序号
	 * 
	 * @author zengyunfeng
	 * @return
	 */
	public void storeFileRange(int start, int size) {
		super.executeBySql("UPDATE beidoucap.sysnvtab SET value=? WHERE name=?",
				new Object[] { String.valueOf(start), START_NAME });

		super.executeBySql("UPDATE beidoucap.sysnvtab SET value=? WHERE name=?",
				new Object[] { String.valueOf(size), SIZE_NAME });
	}

	/**
	 * 生成老的bo对象
	 * @author zengyunfeng
	 * @param reader
	 * @return
	 * @throws IOException
	 * @throws ErrorFormatException
	 */
	public SiteStatBo readOldRecord(BufferedReader reader)
			throws IOException, ErrorFormatException {

		if (reader == null) {
			return null;
		}
		String line = reader.readLine();
		if (line == null) {
			return null;
		}
		String[] fields = line.split(FIELD_SPLITER);
		if (fields.length < 7) {
			throw new ErrorFormatException("site stat file with record='"
					+ line + "'");
		}
		if (fields.length > 9) {
			LogUtils.warn(LOG, "error format in site stat file with record='"
					+ line + "'");
		}

		SiteStatBo result = new SiteStatBo(); 

		String curField = fields[0].trim();
		validatEmpty("cntn", curField, line);
		result.setCntn(curField);

		curField = fields[1].trim();
		curField = curField.toLowerCase();
		validatEmpty("domain", curField, line);
		result.setDomain(curField);

		curField = fields[2].trim();
		result.setRetrieve(validatLong("retrieve", curField, line));

		curField = fields[3].trim();
		result.setAds(validatLong("ads", curField, line));

		curField = fields[4].trim();
		// wuliao字段改用二进制编码, 与cprostat统计模块同步调整, @since beidou1.2.24
		result.setWuliao(validatInt("wuliao", curField, line));

		// 如果统计到的物料类型在系统支持的范围之外，则保持
		if (result.getWuliao() > SiteConstant.WL_FULL_SUPPORT
				|| result.getWuliao() <= SiteConstant.WL_ZERO_SUPPORT) {
			throw new ErrorFormatException("wuliao is not valid in record='"
					+ line + "'");
		}

		curField = fields[5].trim();
		result.setClicks(validatInt("clicks", curField, line));

		curField = fields[6].trim();
		result.setCost(validatInt("cost", curField, line));

		// 如果站点支持图片物料，则继续处理其
		if (SiteConstant.bitOp_supports(result.getWuliao(),
				SiteConstant.WL_PIC_FLAG)) {
			if (fields.length < 9) {
				throw new ErrorFormatException(
						"error format in site stat file with record='" + line
								+ "'");
			}
			curField = fields[7].trim();
			validatEmpty("size", curField, line);
			String[] sizeList = curField.split(SUB_FIELD_SPLITER);
			curField = fields[8].trim();
			String[] sizeFlowList = curField.split(SUB_FIELD_SPLITER);
			Map<Integer, Integer> sizeFlow = new Hashtable<Integer, Integer>(
					sizeList.length, 1);

			int width = 0;
			int height = 0;
			int intSize = 0;
			if (sizeList.length != sizeFlowList.length) {
				throw new ErrorFormatException(
						"error format in site stat file with record='" + line
								+ "'");
			}

			for (int index = 0; index < sizeList.length; index++) {
				if ("".equals(sizeList[index])
						|| "".equals(sizeFlowList[index])) {
					continue;
				}
				String[] sizeStr = sizeList[index].split(THIRD_FIELD_SPLITER);
				if (sizeStr.length < 2) {
					throw new ErrorFormatException(
							"error format in site stat file with record='"
									+ line + "'");
				}
				width = validatInt("size", sizeStr[0], line);
				height = validatInt("size", sizeStr[1], line);

				// 悬浮广告支持的尺寸集合，与固定图片广告支持的尺寸集合不同，需要分别处理
				// mod by zhuqian @beidou1.2.24
				intSize = SiteConstant.isSupportFixedAdSize(new int[] {
							width, height });

				if (intSize > 0) {
					try {
						sizeFlow.put(intSize, validatInt("size_flow",
								sizeFlowList[index], line));
					} catch (ErrorFormatException e) {
						LogUtils.warn(LOG,
								"size_flow is not a number with record='"
										+ line + "'");
					}
				} else {
					LogUtils.warn(LOG, "unsupport size (" + width + ", "
							+ height + ") in site " + "stat file with record='" + line + "'");
				}
			}

			if (sizeFlow.size() == 0) {
				throw new ErrorFormatException("non of the sizes is supported "
						+ " in line ='" + line
						+ "'");
			}

			result.setSizeFlow(sizeFlow);
		}


		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.baidu.beidou.unionsite.dao.SiteStatFileDao#readRecord(java.io.BufferedReader, int)
	 */
	public SiteStatBo readRecord(BufferedReader reader, int displayType)
			throws IOException, ErrorFormatException {

		if (reader == null) {
			return null;
		}
		String line = reader.readLine();
		if (line == null) {
			return null;
		}
		String[] fields = line.split(FIELD_SPLITER);
		if (fields.length < 7) {
			throw new ErrorFormatException(SiteConstant.FILTER_OUT_REASON_FIELD_NUMBER_TOO_SMALL + "\t" + "site stat file with record='"
					+ line + "'");
		}
		if (fields.length > 9) {
			LogUtils.warn(LOG, "error format in site stat file with record='"
					+ line + "'");
		}

		SiteStatBo result = new SiteStatBo(); 

		String curField = fields[0].trim();
		validatEmpty("cntn", curField, line);
		result.setCntn(curField);

		curField = fields[1].trim();
		curField = curField.toLowerCase();
		validatEmpty("domain", curField, line);
		result.setDomain(curField);

		curField = fields[2].trim();
		result.setRetrieve(validatLong("retrieve", curField, line));

		curField = fields[3].trim();
		result.setAds(validatLong("ads", curField, line));

		curField = fields[4].trim();
		// wuliao字段改用二进制编码, 与cprostat统计模块同步调整, @since beidou1.2.24
		result.setWuliao(validatInt("wuliao", curField, line));

		// 如果统计到的物料类型在系统支持的范围之外，则保持
		if (result.getWuliao() > SiteConstant.WL_FULL_SUPPORT
				|| result.getWuliao() <= SiteConstant.WL_ZERO_SUPPORT) {
			throw new ErrorFormatException(SiteConstant.FILTER_OUT_REASON_WULIAO_TYPE_INVALID + "\t" + "wuliao is not valid in record='"
					+ line + "'");
		}
		
		// 如果是悬浮、贴片流量数据，则需检查它是否支持图片
		if ((displayType == SiteConstant.DISP_FLOW_FLAG || displayType == SiteConstant.DISP_FILM_FLAG)
				&& !SiteConstant.bitOp_supports(result.getWuliao(),
						SiteConstant.WL_PIC_FLAG)) {
			throw new ErrorFormatException(SiteConstant.FILTER_OUT_REASON_DISP_FILM_AND_FLOW_NOT_SUPPORT_PIC_WULIAO_TYPE + "\t" + "[displayType=" + displayType
					+ "] wuliao should support pic in record='" + line + "'");
		}

		curField = fields[5].trim();
		result.setClicks(validatInt("clicks", curField, line));

		curField = fields[6].trim();
		result.setCost(validatInt("cost", curField, line));

		// 如果站点支持图片物料，则继续处理其
		if (SiteConstant.bitOp_supports(result.getWuliao(),
				SiteConstant.WL_PIC_FLAG)) {
			if (fields.length < 9) {
				throw new ErrorFormatException(
						SiteConstant.FILTER_OUT_REASON_SUPPORT_PIC_WULIAP_TYPE_BUT_FILED_LESS_THAN_9 + "\t" + 
						"error format in site stat file with record='" + line
								+ "'");
			}
			curField = fields[7].trim();
			validatEmpty("size", curField, line);
			String[] sizeList = curField.split(SUB_FIELD_SPLITER);
			curField = fields[8].trim();
			String[] sizeFlowList = curField.split(SUB_FIELD_SPLITER);
			Map<Integer, Integer> sizeFlow = new Hashtable<Integer, Integer>(
					sizeList.length, 1);

			int width = 0;
			int height = 0;
			int intSize = 0;
			if (sizeList.length != sizeFlowList.length) {
				throw new ErrorFormatException(
						SiteConstant.FILTER_OUT_REASON_SIZE_AND_SIZEFLOW_LIST_NUMBER_NOT_MATCH + "\t" +
						"error format in site stat file with record='" + line
								+ "'");
			}

			for (int index = 0; index < sizeList.length; index++) {
				if ("".equals(sizeList[index])
						|| "".equals(sizeFlowList[index])) {
					continue;
				}
				String[] sizeStr = sizeList[index].split(THIRD_FIELD_SPLITER);
				if (sizeStr.length < 2) {
					throw new ErrorFormatException(
							SiteConstant.FILTER_OUT_REASON_SIZEFLOW_DELEMETER_BY_STAR_NOT_HAVE_TWO_FIELD + "\t" +
							"error format in site stat file with record='"
									+ line + "'");
				}
				width = validatInt("size", sizeStr[0], line);
				height = validatInt("size", sizeStr[1], line);

				// 悬浮广告支持的尺寸集合，与固定图片广告支持的尺寸集合不同，需要分别处理
				// mod by zhuqian @beidou1.2.24
				// 增加了贴片广告的尺寸集合 @beidou1.2.24
				int[] imageSize = new int[]{width, height};
				switch (displayType) {
				
					case SiteConstant.DISP_FIXED_FLAG:
						intSize = SiteConstant.isSupportFixedAdSize(imageSize);
						break;
					case SiteConstant.DISP_FLOW_FLAG:
						intSize = SiteConstant.isSupportFlowAdSize(imageSize);
						break;
					case SiteConstant.DISP_FILM_FLAG:
						intSize = SiteConstant.isSupportFilmAdSize(imageSize);
						break;
					default:
						throw new ErrorFormatException(SiteConstant.FILTER_OUT_REASON_DISP_INVALID + "\t" + "invalid displayType="
								+ displayType + "]");
				}
				
				if (intSize > 0) {
					try {
						sizeFlow.put(intSize, validatInt("size_flow",
								sizeFlowList[index], line));
					} catch (ErrorFormatException e) {
						LogUtils.warn(LOG,
								"size_flow is not a number with record='"
										+ line + "'");
					}
				} else {
					LogUtils.warn(LOG, "[displayType=" + displayType
							+ "] unsupport size (" + width + ", " + height
							+ ") in site stat file with record='" + line + "'");
				}
			}

			if (sizeFlow.size() == 0) {
				throw new ErrorFormatException(SiteConstant.FILTER_OUT_REASON_NONE_SIZEFLOW_SUPPORT + "\t" +
						"[displayType=" + displayType
						+ "] non of the sizes is supported  in line ='" + line
						+ "'");
			}

			result.setSizeFlow(sizeFlow);
		}

		// 如果是悬浮广告，则需要把统计字段同时存放在悬浮独有的字段中, added by zhuqian @beidou1.2.24
		
		result.setDispType(displayType);
		
		switch (displayType) {
		
			case SiteConstant.DISP_FIXED_FLAG:
				result.setFixedAds(result.getAds());
				result.setFixedClicks(result.getClicks());
				result.setFixedCost(result.getCost());
				result.setFixedRetrieve(result.getRetrieve());
				break;
			case SiteConstant.DISP_FLOW_FLAG:
				result.setFlowAds(result.getAds());
				result.setFlowClicks(result.getClicks());
				result.setFlowCost(result.getCost());
				result.setFlowRetrieve(result.getRetrieve());
				break;
			case SiteConstant.DISP_FILM_FLAG:
				result.setFilmAds(result.getAds());
				result.setFilmClicks(result.getClicks());
				result.setFilmCost(result.getCost());
				result.setFilmRetrieve(result.getRetrieve());
				break;
			default:
				throw new ErrorFormatException("invalid displayType="
						+ displayType + "]");
		}
		
		return result;
	}

	/**
	 * 读取一条IP,cookie记录
	 * 
	 * @author zengyunfeng
	 * @param line
	 * @return 解析的统计数据，null表示读到文件末尾
	 * @throws IOException
	 * @throws ErrorFormatException
	 * 
	 */
	public IPCookieBo readIPCookieRecord(BufferedReader reader)
			throws IOException, ErrorFormatException {
		if (reader == null) {
			return null;
		}
		String line = reader.readLine();
		if (line == null) {
			return null;
		}
		String[] fields = line.split(FIELD_SPLITER);
		if (fields.length < 3) {
			throw new ErrorFormatException(
					"site ipcookie stat file with record='" + line + "'");
		}
		if (fields.length > 3) {
			LogUtils.warn(LOG,
					"error format in site ipcookie stat file with record='"
							+ line + "'");
		}
		IPCookieBo result = new IPCookieBo();
		String curField = null;
		curField = fields[0].trim();
		validatEmpty("ipcookie file's domain", curField, line);
		result.setDomain(curField);

		curField = fields[1].trim();
		result.setUnique_ip(validatInt("ipcookie file's unique_ip", curField,
				line));

		curField = fields[2].trim();
		result.setUnique_cookie(validatInt("ipcookie file's unique_cookie",
				curField, line));

		return result;
	}

	private void validatEmpty(String name, String value, String line)
			throws ErrorFormatException {
		if ("".equals(value)) {
			throw new ErrorFormatException(name + " can't be empty in record='"
					+ line + "'");
		}
	}

	private int validatInt(String name, String value, String line)
			throws ErrorFormatException {
		if ("".equals(value)) {
			throw new ErrorFormatException(name + " can't be empty in record='"
					+ line + "'");
		} else {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new ErrorFormatException(name
						+ " is not a number in record='" + line + "'", e);
			}
		}
	}
	
	private long validatLong(String name, String value, String line)
		throws ErrorFormatException {
		if ("".equals(value)) {
			throw new ErrorFormatException(name + " can't be empty in record='"
					+ line + "'");
		} else {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				throw new ErrorFormatException(name
						+ " is not a number in record='" + line + "'", e);
			}
		}
	}

	/**
	 * 保存所有的文件
	 * 
	 * @author zengyunfeng
	 * @param list
	 * @throws IOException
	 */
	@Deprecated
	public void persistentAll(ObjectOutputStream output,
			List<SiteStatExtBo> list) throws IOException {
		if (list == null || output == null) {
			return;
		}
		for (SiteStatBo bo : list) {
			output.writeObject(bo);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.baidu.beidou.unionsite.dao.SiteStatFileDao#persistentAll(java.io.FileOutputStream, java.util.List)
	 */
	public void persistentAll(FileOutputStream output, List<SiteStatBo> list) throws IOException{
		if (output == null || CollectionUtils.isEmpty(list)) {
			return;
		}
		
		for(SiteStatBo bo : list){
			
			StringBuilder builder = new StringBuilder();
			builder.append(bo.getCntn()).append('\t');
			builder.append(bo.getDomain()).append('\t');
			builder.append(bo.getWuliao()).append('\t');
			builder.append(bo.getDispType()).append('\t');
			builder.append(bo.getUnique_ip()).append('\t');
			builder.append(bo.getUnique_cookie()).append('\t');
			builder.append(bo.getClicks()).append('\t');
			builder.append(bo.getCost()).append('\t');
			builder.append(bo.getRetrieve()).append('\t');
			builder.append(bo.getAds()).append('\t');
			builder.append(bo.getFixedClicks()).append('\t');
			builder.append(bo.getFixedCost()).append('\t');
			builder.append(bo.getFixedRetrieve()).append('\t');
			builder.append(bo.getFixedAds()).append('\t');
			builder.append(bo.getFlowClicks()).append('\t');
			builder.append(bo.getFlowCost()).append('\t');
			builder.append(bo.getFlowRetrieve()).append('\t');
			builder.append(bo.getFlowAds()).append('\t');
			builder.append(bo.getFilmClicks()).append('\t');
			builder.append(bo.getFilmCost()).append('\t');
			builder.append(bo.getFilmRetrieve()).append('\t');
			builder.append(bo.getFilmAds());
			
			Map<Integer, Integer> sizeFlow = bo.getSizeFlow();
			//sizeflow的格式：key1,value1|key2,value2|....
			//如果没有sizeflow信息，则不输出
			if(!CollectionUtils.isEmpty(sizeFlow)){
				
				builder.append('\t');
				
				StringBuilder flow = new StringBuilder();
				
				for(Integer key : sizeFlow.keySet()){
					
					if(flow.length() > 0){
						flow.append('|');
					}
					flow.append(key).append(',').append(sizeFlow.get(key));
					
				}
				
				builder.append(flow);
			}
			
			builder.append('\n');
			output.write(builder.toString().getBytes());
		}
	}
	
	/**
	 * 保存所有的文件
	 * 
	 * @author zengyunfeng
	 * @param list
	 * @throws IOException
	 */
	public void persistentOldAll(ObjectOutputStream output,
			List<SiteStatBo> list) throws IOException {
		if (list == null || output == null) {
			return;
		}
		for (SiteStatBo bo : list) {
			output.writeObject(bo);
		}
	}


	/**
	 * 保存所有的文件
	 * 
	 * @author zengyunfeng
	 * @param list
	 * @throws IOException
	 */
	public void persistentAllIPCookie(ObjectOutputStream output,
			List<IPCookieBo> list) throws IOException {
		if (list == null || output == null) {
			return;
		}
		for (IPCookieBo bo : list) {
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
	@Deprecated
	public SiteStatBo next(ObjectInputStream input) throws IOException,
			ClassNotFoundException {
		if (input == null) {
			return null;
		}
		try {
			SiteStatBo bo = (SiteStatBo) input.readObject();

			// 如果文件里存的结果已扩充，则直接返回
			if (bo instanceof SiteStatExtBo) {
				return bo;
			}

			// 如果是老的BO，则：
			// 1、使用新的BO装置
			// 2、调整wuliao字段的编码含义：把2和3的含义对换
			if (bo instanceof SiteStatBo) {
				SiteStatExtBo ext = new SiteStatExtBo(bo);
				if (bo.getWuliao() == 2) {
					ext.setWuliao(3);
				} else if (bo.getWuliao() == 3) {
					ext.setWuliao(2);
				}
				ext.setDispType(SiteConstant.DISP_FIXED_FLAG);
				return ext;
			}

			return null;

		} catch (EOFException e) {
			return null;
		}
	}

	public SiteStatBo next(BufferedReader reader) throws IOException,
			ErrorFormatException {
		if (reader == null) {
			return null;
		}
		try {
			String line = reader.readLine();

			if(line == null){
				return null;
			}
			
			String[] fields = line.split(FIELD_SPLITER);
			if (fields.length < 22 || fields.length > 23) {
				throw new ErrorFormatException(
						"Invalid format in cache line [field size]: '" + line
								+ "'");
			}

			SiteStatBo bo = new SiteStatBo();

			try {

				int index = 0;
				bo.setCntn(fields[index++]);
				bo.setDomain(fields[index++]);
				bo.setWuliao(Integer.parseInt(fields[index++]));
				bo.setDispType(Integer.parseInt(fields[index++]));
				bo.setUnique_ip(Integer.parseInt(fields[index++]));
				bo.setUnique_cookie(Integer.parseInt(fields[index++]));
				bo.setClicks(Integer.parseInt(fields[index++]));
				bo.setCost(Integer.parseInt(fields[index++]));
				bo.setRetrieve(Long.parseLong(fields[index++]));
				bo.setAds(Long.parseLong(fields[index++]));
				bo.setFixedClicks(Integer.parseInt(fields[index++]));
				bo.setFixedCost(Integer.parseInt(fields[index++]));
				bo.setFixedRetrieve(Long.parseLong(fields[index++]));
				bo.setFixedAds(Long.parseLong(fields[index++]));
				bo.setFlowClicks(Integer.parseInt(fields[index++]));
				bo.setFlowCost(Integer.parseInt(fields[index++]));
				bo.setFlowRetrieve(Long.parseLong(fields[index++]));
				bo.setFlowAds(Long.parseLong(fields[index++]));
				bo.setFilmClicks(Integer.parseInt(fields[index++]));
				bo.setFilmCost(Integer.parseInt(fields[index++]));
				bo.setFilmRetrieve(Long.parseLong(fields[index++]));
				bo.setFilmAds(Long.parseLong(fields[index++]));

				//处理sizeflow字段，格式: key1,value1|key2,value2|...
				if (fields.length > 22) {

					Map<Integer, Integer> sizeFlow = new HashMap<Integer, Integer>();

					String sizeflow = fields[index++];
					String[] flows = sizeflow.split(SUB_FIELD_SPLITER);

					for (int i = 0; i < flows.length; i++) {

						String[] flow = flows[i].split(KEY_VALUE_SPLITER);

						if (flow.length != 2) {
							throw new ErrorFormatException(
									"Invalid format in cache line [sizeflow]: '"
											+ line + "'");
						}

						sizeFlow.put(Integer.parseInt(flow[0]), Integer
								.parseInt(flow[1]));
					}

					bo.setSizeFlow(sizeFlow);
				}

			} catch (NumberFormatException e) {
				throw new ErrorFormatException(
						"Invalid format in cache line [NumberFormat]: '" + line
								+ "'");
			}

			return bo;

		} catch (EOFException e) {
			return null;
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
	public IPCookieBo nextIp(ObjectInputStream input) throws IOException,
			ClassNotFoundException {
		if (input == null) {
			return null;
		}
		try {
			return (IPCookieBo) input.readObject();
		} catch (EOFException e) {
			return null;
		}
	}
}
