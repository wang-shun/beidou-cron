package com.baidu.beidou.unionsite.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import com.baidu.beidou.test.common.AbstractTestCase;
import com.baidu.beidou.unionsite.bo.IntegerEntry;
import com.baidu.beidou.unionsite.bo.WMSiteBo;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.service.WMSiteService;
import com.baidu.beidou.unionsite.service.impl.WMSiteServiceImpl.Stat;
import com.baidu.beidou.unionsite.vo.WMSiteIndexVo;

public class WMSiteServiceImplTest extends AbstractTestCase {
	@Resource
	WMSiteService wMSiteService;
	@Ignore
	@Test
	public void testCaculateSiteHeat() {
		List<WMSiteBo> list = new ArrayList<WMSiteBo>();

		WMSiteBo bo = new WMSiteBo();
		bo.setSiteId(1);
		bo.setRateCmp(0.95);
		bo.setScoreCmp(3000);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_5);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(2);
		bo.setRateCmp(0.96);
		bo.setScoreCmp(3020);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_5);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(3);
		bo.setRateCmp(0.97);
		bo.setScoreCmp(3120);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_5);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(4);
		bo.setRateCmp(0.87);
		bo.setScoreCmp(2720);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_4);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(5);
		bo.setRateCmp(0.77);
		bo.setScoreCmp(2620);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_4);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(6);
		bo.setRateCmp(0.76);
		bo.setScoreCmp(2520);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_4);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(7);
		bo.setRateCmp(0.67);
		bo.setScoreCmp(2220);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_3);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(8);
		bo.setRateCmp(0.57);
		bo.setScoreCmp(2120);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_3);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(9);
		bo.setRateCmp(0.56);
		bo.setScoreCmp(2020);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_3);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(10);
		bo.setRateCmp(0.47);
		bo.setScoreCmp(1220);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_2);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(11);
		bo.setRateCmp(0.46);
		bo.setScoreCmp(1120);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_2);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(12);
		bo.setRateCmp(0.45);
		bo.setScoreCmp(1020);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_2);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(13);
		bo.setRateCmp(0.27);
		bo.setScoreCmp(220);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_1);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(14);
		bo.setRateCmp(0.24);
		bo.setScoreCmp(120);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_1);
		list.add(bo);

		bo = new WMSiteBo();
		bo.setSiteId(15);
		bo.setRateCmp(0.21);
		bo.setScoreCmp(20);
		bo.setCmpLevel(SiteConstant.CONSTANT_CMP_LEVEL_1);
		list.add(bo);
		((WMSiteServiceImpl)wMSiteService).caculate(list);

		Map<Integer, Integer> expected = new HashMap<Integer, Integer>();
		expected.put(15, 21);
		expected.put(14, 21);
		expected.put(13, 21);
		expected.put(12, 32);
		expected.put(11, 44);
		expected.put(10, 55);
		expected.put(9, 67);
		expected.put(8, 67);
		expected.put(7, 67);
		expected.put(6, 72);
		expected.put(5, 78);
		expected.put(4, 84);
		expected.put(3, 97);
		expected.put(2, 96);
		expected.put(1, 95);

		for (int i = 0; i < list.size(); i++) {
			WMSiteBo b = list.get(i);
			Assert.assertEquals(b.getSiteHeat(), expected.get(b.getSiteId()).intValue());
		}
	}
	@Test
	public void testMerge() {
		List<WMSiteIndexVo> sub = new ArrayList<WMSiteIndexVo>();
		List<WMSiteIndexVo> main = new ArrayList<WMSiteIndexVo>();

		WMSiteIndexVo vo = new WMSiteIndexVo();
		vo.setSiteId(1);
		vo.setRegion("1,2");
		sub.add(vo);

		vo = new WMSiteIndexVo();
		vo.setSiteId(2);
		vo.setRegion("1,2");
		sub.add(vo);

		vo = new WMSiteIndexVo();
		vo.setSiteId(3);
		vo.setRegion("1,2");
		sub.add(vo);

		vo = new WMSiteIndexVo();
		vo.setSiteId(2);
		vo.setGender("1,2");
		vo.setDegree("1,2");
		vo.setAge("1,2");
		main.add(vo);

		main = ((WMSiteServiceImpl)wMSiteService).merge(main, sub);
		Assert.assertNotNull(main.get(1).getGender());
	}
	@Ignore
	@Test
	public void testDigestString() {
		String temp = "0,1000|1,3000|2,4000";
		Map<Integer, Stat> totalstatMap = new HashMap<Integer, Stat>();
		List<IntegerEntry> boStatEntry = new ArrayList<IntegerEntry>();
		Set<Integer> candidates = new HashSet<Integer>();
		candidates.add(1);
		candidates.add(2);
		((WMSiteServiceImpl)wMSiteService).digestString(temp, totalstatMap, boStatEntry, candidates);
//		wMSiteServiceImpl.digestString(temp, totalstatMap, boStatEntry, candidates);
		for (IntegerEntry ie : boStatEntry) {
			Assert.assertNotSame(0, ie.getKey());
		}
		Assert.assertEquals(6000.0, totalstatMap.get(1).amount);
	}
	@Test
	public void testCalIncludesAndPercent() {
		String temp = "0,1000|1,3000|2,4000";
		Map<Integer, Stat> totalstatMap = new HashMap<Integer, Stat>();
		List<IntegerEntry> boStatEntry = new ArrayList<IntegerEntry>();
		Set<Integer> candidates = new HashSet<Integer>();
		Set<Integer> excludes = new HashSet<Integer>();
		excludes.add(1);
		candidates.add(1);
		candidates.add(2);
		((WMSiteServiceImpl)wMSiteService).digestString(temp, totalstatMap, boStatEntry, candidates);
		List<Integer> result = ((WMSiteServiceImpl)wMSiteService).calIncludesAndPercent(boStatEntry, totalstatMap, excludes);
		System.out.println(boStatEntry);
		System.out.println(result);
		Assert.assertEquals(2, result.get(0).intValue());
	}
	@Override
	public int getShard() {
		// TODO Auto-generated method stub
		return 0;
	}
}
