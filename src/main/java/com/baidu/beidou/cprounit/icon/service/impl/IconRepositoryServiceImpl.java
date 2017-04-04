package com.baidu.beidou.cprounit.icon.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.icon.AdCategCache;
import com.baidu.beidou.cprounit.icon.bo.AdTradeInfo;
import com.baidu.beidou.cprounit.icon.bo.IconPurpose;
import com.baidu.beidou.cprounit.icon.bo.Purpose;
import com.baidu.beidou.cprounit.icon.bo.SystemIcon;
import com.baidu.beidou.cprounit.icon.bo.TempSystemIcon;
import com.baidu.beidou.cprounit.icon.dao.AdConfigDao;
import com.baidu.beidou.cprounit.icon.dao.IconPurposeDao;
import com.baidu.beidou.cprounit.icon.dao.PurposeDao;
import com.baidu.beidou.cprounit.icon.dao.SystemIconDao;
import com.baidu.beidou.cprounit.icon.service.IconRepositoryService;

public class IconRepositoryServiceImpl implements IconRepositoryService {

	private static final Log log = LogFactory.getLog(IconRepositoryServiceImpl.class);

	private PurposeDao purposeDao;
	private SystemIconDao systemIconDao;
	private IconPurposeDao iconPurposeDao;
	private AdConfigDao adConfigDao;

	/** 推广目的映射表 key:推广目的名称，value：推广目的ID */
	Map<String, Integer> purposeMap = new HashMap<String, Integer>();

	/**
	 * @function 获取图标的所有推广目的映射表
	 */
	private void initFirst() {
		List<IconPurpose> iconPurposeList = iconPurposeDao.findAll();
		for (IconPurpose iconPurpose : iconPurposeList)
			purposeMap.put(iconPurpose.getPurposeName(), iconPurpose.getPurposeId());
	}

	/**
	 * @function 将drmc插入的系统图标保存到数据库中
	 * @param iconList
	 * @throws UnsupportedEncodingException
	 */
	public void setSystemIconRepository(List<TempSystemIcon> iconList) throws Exception {
		// 在这里做入库工作，这个方案受到事务的保护
		// 导入已有的推广目的入内存
		initFirst();

		AdCategCache adCategCache = new AdCategCache();
		// 导入所有行业信息入内存
		adCategCache.setAdTradeInfo(adConfigDao.findAdTrade());

		// 一级行业列表
		List<AdTradeInfo> allFirstTrades = adCategCache.getFirstTradeList();

		for (TempSystemIcon tempSystemIcon : iconList) {
			Integer firstTradeId = -1;
			Integer secondTradeId = -1;
			Integer purposeId = -1;

			// System.out.println("一级行业:"+tempSystemIcon.getFirstTrade()+",二级行业："+tempSystemIcon.getSecondTrade()+
			// ",推广目的:"+tempSystemIcon.getPurpose()+",业务词："+tempSystemIcon.getTags());

			// 如果一级行业信息不为空
			if (null != tempSystemIcon.getFirstTrade()) {
				for (AdTradeInfo trade : allFirstTrades) {
					if (trade.getTradename().equals(tempSystemIcon.getFirstTrade())) {
						firstTradeId = trade.getTradeid();
						// 一级行业和二级行业都不为空
						if (null != tempSystemIcon.getSecondTrade()) {
							// 获取一级行业下的二级行业
							List<AdTradeInfo> secondTrades = adCategCache.getSencondTradeList(firstTradeId);
							for (AdTradeInfo secTrade : secondTrades) {
								if (secTrade.getTradename().equals(tempSystemIcon.getSecondTrade())) {
									secondTradeId = secTrade.getTradeid();
									break;
								}
							}
						}
						break;
					}
				}
			}

			// 获取推广目的Id
			if (null != tempSystemIcon.getPurpose() && !"".equals(tempSystemIcon.getPurpose())) {
				if (purposeMap.containsKey(tempSystemIcon.getPurpose().trim())) {
					purposeId = purposeMap.get(tempSystemIcon.getPurpose().trim());
				} else {
					// 新读取的推广目的 ，添加新的推广目的到IconPurpose 数据库中
					IconPurpose ip = new IconPurpose();
					ip.setPurposeName(tempSystemIcon.getPurpose().trim());
					iconPurposeDao.insert(ip);
					ip = iconPurposeDao.findByName(tempSystemIcon.getPurpose().trim());
					purposeId = ip.getPurposeId();
					purposeMap.put(tempSystemIcon.getPurpose(), purposeId);
				}
			}

			// 构建SystemIcon对象
			SystemIcon icon = new SystemIcon();
			icon.setMcId(tempSystemIcon.getMcId());
			icon.setUbmcsyncflag(CproUnitConstant.UBMC_SYNC_FLAG_YES);
			icon.setFirstTradeId(firstTradeId == -1 ? null : firstTradeId);
			icon.setHight(tempSystemIcon.getHight());
			icon.setWidth(tempSystemIcon.getWidth());
			icon.setSecondTradeId(secondTradeId == -1 ? null : secondTradeId);
			icon.setPurposeId(purposeId == -1 ? null : purposeId);
			icon.setAddTime(new Date());
			// 对 tags进行一些处理
			if ("".equals(tempSystemIcon.getTags())) {
				icon.setTags(null);
			} else {
				String tags[] = tempSystemIcon.getTags().split("\\|");
				String t_tags = "";
				for (int i = 0; i < tags.length; i++) {
					if (!"".equals(tags[i]))
						t_tags += tags[i].trim() + "|";
				}
				// System.out.println(t_tags);
				icon.setTags(t_tags);
			}

			icon.setUsedSum(0);

			// 图标入库之前先判断是有已存在

			log.info("init system icons into database:[mcId=" + icon.getMcId() + ",width:" + icon.getHight() 
					+ ",width:" + icon.getWidth() + ",firstTrade:" + tempSystemIcon.getFirstTrade() 
					+ ",secondTrade:" + tempSystemIcon.getSecondTrade() + ",purpose:"
					+ tempSystemIcon.getPurpose() + ",tags:" + tempSystemIcon.getTags());
			// System.out.println("一级行业ID:"+icon.getFirstTradeId()+",二级行业ID："+icon.getSecondTradeId()+
			// ",推广目的ID:"+icon.getPurposeId()+",业务词："+icon.getTags());

			// 插入系统图标信息
			systemIconDao.insertSystemIcon(icon);

			// 将一级行业信息及对应的推广目的插入推广目的列表
			Purpose purpose = new Purpose();
			purpose.setFirstTradeId(firstTradeId == -1 ? null : firstTradeId);
			purpose.setPurposeId(purposeId == -1 ? null : purposeId);
			purpose.setPurposeName(tempSystemIcon.getPurpose());
			purposeDao.insert(purpose);

		}
	}

	public PurposeDao getPurposeDao() {
		return purposeDao;
	}

	public void setPurposeDao(PurposeDao purposeDao) {
		this.purposeDao = purposeDao;
	}

	public SystemIconDao getSystemIconDao() {
		return systemIconDao;
	}

	public void setSystemIconDao(SystemIconDao systemIconDao) {
		this.systemIconDao = systemIconDao;
	}

	public IconPurposeDao getIconPurposeDao() {
		return iconPurposeDao;
	}

	public void setIconPurposeDao(IconPurposeDao iconPurposeDao) {
		this.iconPurposeDao = iconPurposeDao;
	}

	public AdConfigDao getAdConfigDao() {
		return adConfigDao;
	}

	public void setAdConfigDao(AdConfigDao adConfigDao) {
		this.adConfigDao = adConfigDao;
	}

}
