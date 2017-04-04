package com.baidu.beidou.cprounit.dao.impl;

import java.util.Date;

import com.baidu.beidou.cprounit.bo.Unit;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.dao.UnitWriteDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.partition.impl.PartKeyUseridImpl;
import com.baidu.beidou.util.partition.strategy.PartitionStrategy;

/**
 * @author hanxu03
 *
 */
public class UnitWriteDaoImpl extends GenericDaoImpl implements UnitWriteDao {

	private PartitionStrategy strategy = null;
	private PartitionStrategy materStrategy = null;
	
	public void modUnitInfo(Integer userId, final Unit unit){
		StringBuilder sb = new StringBuilder();

		sb.append("update beidou." + strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() 
				+ " s join " + materStrategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() 
				+ " m on s.id=m.id set m.fileSrc=?, m.height=?, m.width=?, m.wid=?, m.wuliaotype=?, "
				+ " m.player=?, s.chaTime=?, m.ubmcsyncflag=?, m.mcId=?, m.mcVersionId=?, m.file_src_md5=? where s.id=?");

		Object[] params = new Object[] { unit.getMaterial().getFileSrc(), 
				unit.getMaterial().getHeight(),	unit.getMaterial().getWidth(), 
				unit.getMaterial().getWid(), unit.getMaterial().getWuliaoType(), 
				unit.getMaterial().getPlayer(), new Date(), 
				CproUnitConstant.UBMC_SYNC_FLAG_YES, unit.getMaterial().getMcId(), 
				unit.getMaterial().getMcVersionId(), unit.getMaterial().getFileSrcMd5(),
				unit.getId()};
	
		super.executeBySql(sb.toString(), params);
	}
	
	public void updateUnitInfo(Integer userId, Long unitId, String fileSrc, Long wid, Date chaTime, Integer state) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("update beidou." + strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() 
				+ " s join " + materStrategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() 
				+ " m on s.id=m.id set m.fileSrc=?, m.wid=?, m.ubmcsyncflag=0 where s.id=? and s.chaTime=? and s.state=?");
		
		Object[] params = new Object[] { fileSrc, wid, unitId, chaTime, state };
		
		super.executeBySql(sb.toString(), params);
	}
	
	public void updateUnitForUbmcToDrmc(Integer userId, Long unitId, String fileSrc, Long wid, Date chaTime, Integer state) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("update beidou." + strategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() 
				+ " s join " + materStrategy.getPartitions(new PartKeyUseridImpl(userId)).getTablename() 
				+ " m on s.id=m.id set m.fileSrc=?, m.wid=?, m.drmcsyncflag=1 where s.id=? and s.chaTime=? and s.state=?");
		
		Object[] params = new Object[] { fileSrc, wid, unitId, chaTime, state };
		
		super.executeBySql(sb.toString(), params);
	}

	public PartitionStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(PartitionStrategy strategy) {
		this.strategy = strategy;
	}

	public PartitionStrategy getMaterStrategy() {
		return materStrategy;
	}

	public void setMaterStrategy(PartitionStrategy materStrategy) {
		this.materStrategy = materStrategy;
	}
	
}
