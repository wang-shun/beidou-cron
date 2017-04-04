/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.impl.CproGroupDaoImpl.java
 * 上午11:50:11 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import com.baidu.beidou.cprogroup.bo.CproGroup;
import com.baidu.beidou.cprogroup.bo.CproGroupSimilarPeople;
import com.baidu.beidou.cprogroup.dao.CproGroupDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.unbiz.common.CollectionUtil;

/**
 * 
 * @author kanghongwei
 */

public class CproGroupDaoImpl extends GenericDaoImpl implements CproGroupDao {

	public void updateGroupInfoSiteTradeList(Integer groupId, String siteTradeList) {
		String sql = "update beidou.cprogroupinfo set sitetradelist = ? where groupid = ? ";
		super.executeBySql(sql, new Object[] { siteTradeList, groupId });
	}

	public void updateCproGroup(CproGroup group) {
		if (group == null)
			return;
		String sql = " update beidou.cprogroupinfo set sitelist = ?, sitetradelist = ? where groupid = ? ";
		super.executeBySql(sql, new Object[] { group.getSiteList(), group.getSiteTradeList(), group.getGroupId() });
	}

	public void updateGroupSiteSum(final int groupId, final int siteSum) {
		String sql = " update beidou.cprogroupinfo set sitesum = ? where groupid = ? ";
		super.executeBySql(sql, new Object[] { siteSum, groupId });
	}
	
    @Override
    public CproGroupSimilarPeople findCproGroupSimilarPeople(Integer groupId) {
        String sql =
                "SELECT groupid, userid, groupstate, targettype, similar_flag FROM beidou.cprogroup where groupid = ?";
        List<CproGroupSimilarPeople> result = super.findBySql(new GenericRowMapping<CproGroupSimilarPeople>() {

            @Override
            public CproGroupSimilarPeople mapRow(ResultSet rs, int rowNum) throws SQLException {
                CproGroupSimilarPeople item = new CproGroupSimilarPeople();
                item.setGroupId(rs.getInt("groupid"));
                item.setUserId(rs.getInt("userid"));
                item.setGroupstate(rs.getInt("groupstate"));
                item.setTargetType(rs.getInt("targettype"));
                item.setSimilarFlag(rs.getInt("similar_flag"));
                return item;
            }

        }, sql, new Object[] { groupId }, new int[] { Types.INTEGER });

        if (CollectionUtil.isEmpty(result)) {
            return null;
        } else {
            return result.get(0);
        }
    }

    @Override
    public boolean modSimilarFlag(Integer userId, Integer groupId, int srcSimilarFlag, int destSimilarFlag) {
        if (userId == null || groupId == null) {
            return false;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE cprogroup SET similar_flag = ?, moduserid = ?, modtime = now()");
        sql.append(" WHERE groupid = ? and similar_flag = ?");

        int effectRow =
                super.updateBySql(sql.toString(), new Object[] { destSimilarFlag, userId, groupId, srcSimilarFlag },
                        new int[] { Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, });
        return effectRow == 1;
    }

    @Override
    public boolean updateGroupSysRegion(Integer userId, Integer groupId, String sysRegionStr) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE cprogroupinfo SET sys_reglist=? WHERE groupid=?");
        int effectRow =
                super.updateBySql(sql.toString(), new Object[] { sysRegionStr, groupId }, new int[] { Types.VARCHAR,
                        Types.INTEGER });
        return effectRow == 1;
    }
}
