package com.baidu.beidou.unionsite.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.unionsite.vo.WM123SiteCprodataVo;

/**
 * @author lvzichan
 * @since 2013-10-10
 */
@TransactionConfiguration(transactionManager = "xdbTransactionManager")
public class WM123SiteStatDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private WM123SiteStatDaoImpl wm123SiteStatDaoImpl;

	public void setDataSource(@Qualifier("xdbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}
	
	@Test
	@NotTransactional
	public void testSaveSiteCprodata() {
		List<WM123SiteCprodataVo> cprodataVos = new ArrayList<WM123SiteCprodataVo>();
		WM123SiteCprodataVo cprodataVo = new WM123SiteCprodataVo();
		cprodataVo.setSiteId(1);
		cprodataVo.setSiteUrl("1.com");
		cprodataVo.setInsertDate("20131011");
		cprodataVo.setClick(100);
		cprodataVo.setUv(100);
		cprodataVo.setCpm((float)0.01);
		cprodataVo.setCtr((float)0.2312);
		cprodataVo.setHourClick("-|10|-|-|50|-|20|-|20|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-");
		cprodataVos.add(cprodataVo);
		
		WM123SiteCprodataVo cprodataVo2 = new WM123SiteCprodataVo();
		cprodataVo2.setSiteId(2);
		cprodataVo2.setSiteUrl("2.com");
		cprodataVo2.setInsertDate("20131011");
		cprodataVo2.setClick(200);
		cprodataVo2.setUv(200);
		cprodataVo2.setCpm((float)0.02);
		cprodataVo2.setCtr((float)0.12);
		cprodataVo2.setHourClick("-|20|-|30|-|50|-|100|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-");
		cprodataVos.add(cprodataVo2);
		
		wm123SiteStatDaoImpl.saveSiteCprodata(cprodataVos);
	}
	
	@Test
	@NotTransactional
	public void testDelSiteCprodataByDate() {
		String date = "20131011";
		wm123SiteStatDaoImpl.delSiteCprodataByDate(date);
	}
}