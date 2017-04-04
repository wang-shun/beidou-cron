/**
 * 
 */
package com.baidu.beidou.cprounit.dao;

import java.util.Date;
import java.util.List;

import com.baidu.beidou.auditmanager.vo.DelMaterial;
import com.baidu.beidou.cprounit.bo.CproUnit;
import com.baidu.beidou.cprounit.bo.Unit;
import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.stat.vo.AdLevelInfo;
import com.baidu.beidou.util.page.DataPage;

/**
 * @author zengyunfeng
 * @version 1.0.0
 */
public interface UnitDao {

	/**
	 * 根据拆表id和推广单元的id查找推广单元
	 * @param userId 拆表id
	 * @param id 推广单元id
	 * @return 
	 */
	public AdLevelInfo findById(int userId, Long id) ;
	
	/**
	 * 获得一个推广组下非删除状态的推广单元
	 * @param groupId
	 * @param userId beidou ID 
	 * @return
	 * 下午03:16:24
	 */
	public List<CproUnit> findUnDeletedUnitbyGroupId(final Integer groupId, final int userId);
	/**
	 * 根据一批推广组ID，获取下属的所有创意IDs
	 * @param groupIds
	 * @return下午02:47:01
	 */
	public List<Long> getAllUnitIdsByGroupId(List<Integer> groupIds);
	
	/**
	 * 根据beidouid和创意ID获取推广单元
	 * @param userId
	 * @param unitid
	 * @return
	 */
	public Unit findUnitById(Integer userId, Long unitid);
	
	/**
	 * 根据beidouid和创意ID获取推广单元
	 * @param userId
	 * @param unitid
	 * @return
	 */
	public Date findUnitChaTimeById(Integer userId, Long unitid);
	
	/**
	 * findNotSyncUnit: 获取未同步到ubmc的物料
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<UnitMaterView> findNotSyncUnit(int index, int maxMaterNum);
	
	/**
	 * findToFilterUnit: 获取待过滤的物料
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<UnitMaterView> findToFilterUnit(int dbIndex, int dbSlice);
	
	public void updateUnitBatch(int dbIndex, List<Long> ids, List<Long> mcIds, List<Integer> mcVersionIds, Integer dbSlice);
	
	/**
	 * updateUnit: 更新物料mcId、mcVersionId
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateUnit(int index, Long id, Long mcId, Integer mcVersionId, Integer userId);
	
	public void updateUnitSyncFlagBath(int dbIndex, List<Long> ids, List<String> fileSrcMd5s, List<Date> chaTimes, Integer dbSlice);
	/**
	 * updateUnitSyncFlag: 更新物料的同步标记字段，仅当与chaTime相同时进行
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateUnitSyncFlag(int index, Long id, String fileSrcMd5, Date chaTime, Integer userId);
	
	/**
	 * updateUnitMd5: 更新图片的Md5
	 * @version cpweb-567
	 * @author genglei01
	 * @date Aug 12, 2013
	 */
	public int updateUnitMd5(int index, Long id, String fileSrcMd5, Date chaTime, Integer userId);
	
	/**
	 * findNotSyncUnit: 获取包含特殊字符的创意信息
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<UnitMaterView> findUnitWithSpecialChar(int index, Long unitId, Integer userId);
	
	/**
	 * Function: 获取图文创意所关联的iconId
	 *
	 * @author genglei01
	 * @date Jun 27, 2014
	 */
	public List<Long> findIconIdByUnitId(Long unitId, Integer userId);
	
	/**
	 * Function: 更新图片的尺寸和Md5
	 *
	 * @author genglei01
	 * @date Jun 28, 2014
	 */
	public int updateUnitSizeAndMd5(int index, Long id, int width, int height, 
			String fileSrcMd5, Date chaTime, Integer userId);
	
	/**
	 * updateUnitFilterSpecialChar: 更新db数据，过滤特殊字符
	 * @version cpweb-567
	 * @author genglei01
	 * @date Aug 12, 2013
	 */
	public void updateUnitFilterSpecialChar(int index, UnitMaterView info);
	
	/**
	 * 按页获取指定物料id中，符合物料类型的数据
	 * 
	 * @param ids 指定的物料id
	 * @param wuliaoType 指定的物料类型，为null则认为所有物料类型
	 * @return
	 */
	public List<UnitMaterView> findUnitWithSpecifiedWuliaoType(int userid, List<Long> ids, List<Integer> wuliaoType);
	
	/**
	 * 获取planid对应的物料id列表
	 * 
	 * @param userid
	 * @param planids
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	public List<Long> findUnitIdsByPlanIds(int userid, List<Integer> planIds);
	
	/**
	 * 分页获取planid对应的物料id列表
	 * 
	 * @param userid
	 * @param planids
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	public DataPage<Long> findUnitIdsByPlanIds(int userid, List<Integer> planIds, int pageSize, int pageNo);
	
    public void deleteMater(DelMaterial delMaterial);
}
