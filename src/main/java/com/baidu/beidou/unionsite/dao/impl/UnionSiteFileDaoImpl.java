/**
 * 2009-4-21 上午10:59:33
 */
package com.baidu.beidou.unionsite.dao.impl;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.UnionSiteImporter;
import com.baidu.beidou.unionsite.bo.UnionSiteBo;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.dao.UnionSiteFileDao;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.string.StringUtil;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class UnionSiteFileDaoImpl implements UnionSiteFileDao {

	private static final String FIELD_SPLITER = "\t";
	private static final Log LOG = LogFactory
			.getLog(UnionSiteFileDaoImpl.class);
	private static final int SIZE_CNAME = 64;
	private static final int SIZE_SITEURL = 127;
	private static final int SIZE_SITENAME = 128;
	private static final int SIZE_SITEDESC = 1024;
	private static final int SIZE_FILTER = 256;

	/**
	 * 读取一行联盟记录，并进行校验，对空字符串和长度的检验，长度使用gbk的字节进行校验
	 * 
	 * @author zengyunfeng
	 * @param reader
	 *            联盟接口文件reader
	 * @return 校验，处理后的文件，如何文件格式不符合要求，记error日志，返回null.
	 * @throws IOException
	 *             文件读取失败
	 * @throws ErrorFormatException
	 *             输入格式错误
	 */
	public UnionSiteBo readRecord(BufferedReader reader) throws IOException,
			ErrorFormatException {
		// 过滤掉空行
		String line = null;
		for (line = reader.readLine(); line != null
				&& StringUtils.isBlank(line);) {
			line = reader.readLine();
		}
		if (line == null) {
			return null;
		}

		UnionSiteBo result = new UnionSiteBo();
		String[] fields = line.split(FIELD_SPLITER);

		// 用户名 字符串，最大48
		// 计费名 字符串，最大64
		// 网站域名 字符串，最大256
		// 网站名称 字符串，最大128, 可为空
		// 网站描述 字符串，最大1024, 可为空
		// 北斗二级行业一级类别 整数，最大2位, 可为空
		// 北斗二级行业二级类别 整数，最大4位, 可为空
		// 认证等级 整数，最大1位
		// 财务对象 0：个人； 1：企业
		// 信誉指数 整数，最大10位, 可为空
		// 直营/二级 0：直营； 1：二级
		// 通路 整数，最大10位
		// 作弊次数 整数，最大10位, 可为空
		// 广告过滤行业 字符串，id之间以半角逗号分隔，最大256, 可为空
		// 标识 整数 0：真实网站URL；1：虚拟网站URL，不可为空
		// 站点来源：整数，按位表示：1代表百度联盟站点，2代表来源于google流量
		if (fields == null || fields.length < 13) {
			throw new ErrorFormatException(
					"error format of union site file with record='" + line
							+ "'");
		}
		// 有多余的给出警告
		if (fields.length > 16) {
			LogUtils.error(LOG, "error format of union site file with record='"
					+ line + "'");
		}

		// 计费名 字符串，最大64
		String curFiled = fields[1].trim();
		if (StringUtils.isEmpty(curFiled)) {
			throw new ErrorFormatException("cname can't be empty in record='"
					+ line + "'");
		}
		if (StringUtil.byteLength(curFiled) > SIZE_CNAME) {
			throw new ErrorFormatException("cname is larger than " + SIZE_CNAME
					+ " in record='" + line + "'");
		}
		result.setCname(curFiled);

		// 网站域名 字符串，最大256,
		curFiled = fields[2].trim();
		if (StringUtils.isEmpty(curFiled)) {
			throw new ErrorFormatException("siteurl can't be empty in record='"
					+ line + "'");
		}
		if (StringUtil.byteLength(curFiled) > SIZE_SITEURL) {
			throw new ErrorFormatException("siteurl is larger than "
					+ SIZE_SITEURL + " in record='" + line + "'");
		}

		// 过滤黑名单的域名
		curFiled = curFiled.toLowerCase();
		if (SiteConstant.isBlackSite(curFiled)) {
			throw new ErrorFormatException("siteurl=" + curFiled
					+ " is in black site");
		}
		// 进行www.前缀检查
		if (curFiled.startsWith("www.")) {
			curFiled = curFiled.substring(4);
		}
		result.setSiteUrl(curFiled);

		// 网站名称 字符串，最大128, 可为空
		curFiled = fields[3].trim();
		if (StringUtil.byteLength(curFiled) > SIZE_SITENAME) {
			throw new ErrorFormatException("sitename is larger than "
					+ SIZE_SITENAME + " in record='" + line + "'");
		}
		result.setSiteName(curFiled);

		// 网站描述 字符串，最大1024, 可为空
		curFiled = fields[4].trim();
		if (StringUtil.byteLength(curFiled) > SIZE_SITEDESC) {
			throw new ErrorFormatException("sietdesc is larger than "
					+ SIZE_SITEDESC + " in record='" + line + "'");
		}
		result.setSiteDesc(curFiled);

		// 北斗二级行业一级类别 整数，最大2位, 可为空
		curFiled = fields[5].trim();
		int intFiled = 0;
		if (!StringUtils.isEmpty(curFiled)) {
			try {
				intFiled = Integer.parseInt(curFiled);
			} catch (NumberFormatException e) {
				throw new ErrorFormatException(
						"firsttradeid is not a number in record='" + line + "'");
			}
		}
		result.setFirstTradeId(intFiled);

		// 北斗二级行业二级类别 整数，最大4位, 可为空
		curFiled = fields[6].trim();
		intFiled = 0;
		if (!StringUtils.isEmpty(curFiled)) {
			try {
				intFiled = Integer.parseInt(curFiled);
			} catch (NumberFormatException e) {
				throw new ErrorFormatException(
						"secondtradeid is not a number in record='" + line
								+ "'", e);
			}
		}
		result.setSencondTradeId(intFiled);

		// 认证等级 整数，最大1位
		curFiled = fields[7].trim();
		Byte byteFiled = 0;
		if (!StringUtils.isEmpty(curFiled)) {
			try {
				byteFiled = Byte.valueOf(curFiled);
			} catch (NumberFormatException e) {
				throw new ErrorFormatException(
						"certification is not a number in record='" + line
								+ "'", e);
			}
		} else {
			throw new ErrorFormatException(
					"certification can't be empty in record='" + line + "'");
		}
		result.setCertification(byteFiled);

		// 财务对象 0：个人； 1：企业
		curFiled = fields[8].trim();
		byteFiled = 0;
		if (!StringUtils.isEmpty(curFiled)) {
			try {
				byteFiled = Byte.valueOf(curFiled);
			} catch (NumberFormatException e) {
				throw new ErrorFormatException(
						"finanobj is not a number in record='" + line + "'", e);
			}
		} else {
			throw new ErrorFormatException(
					"finanobj can't be empty in record='" + line + "'");
		}
		result.setFinanobj(byteFiled);

		// 信誉指数 整数，最大10位, 可为空
		curFiled = fields[9].trim();
		Integer integerFiled = null;
		if (!StringUtils.isEmpty(curFiled)) {
			try {
				integerFiled = Integer.valueOf(curFiled);
			} catch (NumberFormatException e) {
				throw new ErrorFormatException(
						"credit is not a number in record='" + line + "'", e);
			}
		}
		result.setCredit(integerFiled);

		// 直营/二级 0：直营； 1：二级
		curFiled = fields[10].trim();
		byteFiled = 0;
		if (!StringUtils.isEmpty(curFiled)) {
			try {
				byteFiled = Byte.valueOf(curFiled);
			} catch (NumberFormatException e) {
				throw new ErrorFormatException(
						"direct is not a number in record='" + line + "'", e);
			}
		} else {
			throw new ErrorFormatException("direct can't be empty in record='"
					+ line + "'");
		}
		result.setDirect(byteFiled);

		// 通路 整数，最大10位
		curFiled = fields[11].trim();
		integerFiled = null;
		if (!StringUtils.isEmpty(curFiled)) {
			try {
				integerFiled = Integer.valueOf(curFiled);
			} catch (NumberFormatException e) {
				throw new ErrorFormatException(
						"channel is not a number in record='" + line + "'", e);
			}
		} else {
			throw new ErrorFormatException("channel can't be empty in record='"
					+ line + "'");
		}
		result.setChannel(integerFiled);

		// 作弊次数 整数，最大10位, 可为空
		curFiled = fields[12].trim();
		integerFiled = null;
		if (!StringUtils.isEmpty(curFiled)) {
			try {
				integerFiled = Integer.valueOf(curFiled);
			} catch (NumberFormatException e) {
				throw new ErrorFormatException(
						"cheats is not a number in record='" + line + "'", e);
			}
		}
		result.setCheats(integerFiled);

		// 广告过滤行业 字符串，id之间以半角逗号分隔，最大256, 可为空
		if (fields.length > 13) {
			curFiled = fields[13].trim();
			if (StringUtil.byteLength(curFiled) > SIZE_FILTER) {
				throw new ErrorFormatException("site ad filter is larger than "
						+ SIZE_FILTER + " in record='" + line + "'");
			}
			result.setFilter(curFiled);
		}

		/*// 标识 整数 0：真实网站URL；1：虚拟网站URL，不可为空
		if (fields.length > 14){
			curFiled = fields[14].trim();
			integerFiled = null;
			if (!StringUtils.isEmpty(curFiled)) {
				try {
					integerFiled = Integer.valueOf(curFiled);
					result.setVirtual(integerFiled == 1 ? true : false);
				} catch (NumberFormatException e) {
					throw new ErrorFormatException(
							"virtual flag is not a number in record='" + line + "'", e);
				}
			}else{
				throw new ErrorFormatException("virtual flag is empty in record='" + line +"'");
			}
		}*/
		//modify by liangshimu @cpweb-250,取消原来第15列表示“是否真实网站URL”的逻辑
		//改为“是否优先以该条数据为准”（有可能出现多个相同的域名）

		if (fields.length > 14){
			curFiled = fields[14].trim();
			integerFiled = null;
			if (!StringUtils.isEmpty(curFiled)) {
				try {
					integerFiled = Integer.valueOf(curFiled);
					result.setShowFlag(integerFiled );
				} catch (NumberFormatException e) {
					throw new ErrorFormatException(
							"show flag is not a number in record='" + line + "'", e);
				}
			}else{
				throw new ErrorFormatException("show flag is empty in record='" + line +"'");
			}
		}
		
		// modified by genglei @cpweb640, 增加站点来源一列
		if (fields.length > 15){
			curFiled = fields[15].trim();
			integerFiled = null;
			if (!StringUtils.isEmpty(curFiled)) {
				try {
					integerFiled = Integer.valueOf(curFiled);
					result.setSiteSource(integerFiled);
				} catch (NumberFormatException e) {
					throw new ErrorFormatException(
							"site source is not a number in record='" + line + "'", e);
				}
			}else{
				throw new ErrorFormatException("site source is empty in record='" + line +"'");
			}
		} else {
			// 兼容版本：因union未上线，兼容联盟站点的老数据格式
			result.setSiteSource(1);
		}
		
		if (StringUtils.isEmpty(result.getSiteName())) {
			UnionSiteImporter.empty1++;
		}
		if (StringUtils.isEmpty(result.getSiteDesc())) {
			UnionSiteImporter.empty2++;
		}
		if (result.getFirstTradeId() == 0) {
			UnionSiteImporter.empty3++;
		}

		return result;
	}

}
