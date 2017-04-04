package com.baidu.beidou.cprogroup.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.cprogroup.bo.CproGroup;
import com.baidu.beidou.cprogroup.bo.CproGroupMoreInfo;
import com.baidu.beidou.cprogroup.bo.CproGroupRegion;
import com.baidu.beidou.cprogroup.dao.CproGroupDaoOnMultiDataSource;
import com.baidu.beidou.cprogroup.dao.rowmap.CproGroupMoreRowMapping;
import com.baidu.beidou.cprogroup.dao.rowmap.CproGroupRowMapping;
import com.baidu.beidou.indexgrade.bo.SimpleGroup;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;
import com.baidu.beidou.util.string.StringUtil;

/**
 * 
 * @author kanghongwei
 * 
 *         refactor by kanghongwei since 2012-10-30
 */
public class CproGroupDaoOnMultiDataSourceImpl extends MultiDataSourceDaoImpl<CproGroup> implements CproGroupDaoOnMultiDataSource {

	private static Comparator<Integer> integer_comparator = new Comparator<Integer>() {
		public int compare(Integer o1, Integer o2) {
			return (o1 - o2);
		}
	};

	public List<CproGroup> findGroupInfobyUserId(final Integer userId) {
		StringBuffer sql = new StringBuffer();
		sql.append("select groupid, sitelist, sitetradelist,userid from beidou.cprogroupinfo where userid = ? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		sql.append(" order by groupid");
		return super.findBySql(userId, new CproGroupRowMapping(), sql.toString(), new Object[] { userId }, new int[] { Types.INTEGER });
	}

	public List<CproGroupMoreInfo> findEffectGroupInfoMorebyPlanId(final Integer planId, int userId) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.groupid as groupid, g.groupname as groupname, t.price as price, t.isallsite as isallsite, t.sitelist as sitelist, t.sitetradelist as sitetradelist, t.isallregion as isallregion, t.reglist as reglist from beidou.cprogroupinfo t, beidou.cprogroup g where t.groupid = g.groupid and g.planid = ? and g.groupstate = ? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("g.userid"));
		sql.append(" order by t.groupid");
		return super.findBySql(userId, new CproGroupMoreRowMapping(), sql.toString(), new Object[] { planId, Integer.valueOf(0) }, new int[] { Types.INTEGER, Types.INTEGER });
	}

	public List<Integer> getAllCproGroupIds() {
		StringBuffer sql = new StringBuffer();
		sql.append("select distinct groupid from beidou.cprogroupinfo where ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));

		List<Map<String, Object>> resultSet = super.findBySql(sql.toString(), new Object[] {}, new int[] {});
		List<Integer> idList = new ArrayList<Integer>();
		for (Map<String, Object> row : resultSet) {
			Integer id = (Integer) row.get("groupid");
			idList.add(id);
		}
		return idList;
	}

	public CproGroup findGroupInfoByGroupId(Integer groupId) {
		StringBuffer sql = new StringBuffer();
		sql.append("select groupid, sitelist, sitetradelist, userid" + " from beidou.cprogroupinfo  where groupid = ? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));

		List<CproGroup> result = super.findBySql(new CproGroupRowMapping(), sql.toString(), new Object[] { groupId }, new int[] { Types.INTEGER });
		if (result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}

	public String findGroupNameByGroupId(Integer groupId, int userId) {
		StringBuffer sql = new StringBuffer();
		sql.append("select groupname as name" + " from beidou.cprogroup where groupid = ? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));

		List<Map<String, Object>> result = super.findBySql(userId, sql.toString(), new Object[] { groupId }, new int[] { Types.INTEGER });
		if (result.isEmpty()) {
			return null;
		} else {
			return result.get(0).get("name").toString();
		}
	}

	public List<Integer> filterGroupByAllSite(List<Integer> groupIds) {
		if (CollectionUtils.isEmpty(groupIds)) {
			return Collections.emptyList();
		}
		String strList = StringUtil.join(",", groupIds);

		StringBuffer sql = new StringBuffer("select groupid from beidou.cprogroupinfo where groupid in (" + strList + ") and isallsite = 1 and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));

		List<Map<String, Object>> resultSet = super.findBySql(sql.toString(), new Object[] {}, new int[] {});
		List<Integer> resList = new ArrayList<Integer>(resultSet.size());

		for (Map<String, Object> row : resultSet) {
			Integer id = (Integer) row.get("groupid");
			resList.add(id);
		}
		return resList;
	}

	public Long countAllGroupIdofEffPlan(int sharding) {
		StringBuffer sql = new StringBuffer("select count(t.groupid) as num from  beidou.cprogroup t, beidou.cproplan s where t.planid = s.planid and s.planstate = 0 and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("t.userid"));
		List<Map<String, Object>> resultSet = super.findBySqlOnSharding(sharding, sql.toString(), new Object[] {}, new int[] {});

		long count = 0;
		for (Map<String, Object> row : resultSet) {
			count += (Long) row.get("num");
		}
		return count;
	}

	public List<Integer> findAllGroupIdofEffPlan(int sharding, final int currPage, final int pageSize) {
		// 指定具体的库，然后查询【避免一次load全部分片的全部数据】
		StringBuffer sql = new StringBuffer("select t.groupid as gid from beidou.cprogroup t, beidou.cproplan s where t.planid = s.planid and s.planstate = 0 and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("t.userid"));
		GenericRowMapping<Integer> mapper = new GenericRowMapping<Integer>() {
			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				Integer intResult = rs.getInt("gid");
				return intResult;
			}
		};
		return super.findBySqlOnSharding(sharding, mapper, sql.toString(), null, null, integer_comparator, currPage, pageSize);
	}

	public List<Map<String, Object>> findGroupByRegId(final int regId) {
		// 目前地域最大是三位数，而且舍弃的也都是，不会有|9999|的情况，这样提高性能
		StringBuffer sql = new StringBuffer("select g.userid, p.planid , p.planname ," + "g.groupid, g.groupname, a.reglist from beidou.cproplan p, beidou.cprogroup g, beidou.cprogroupinfo a where " + "g.groupid=a.groupid and p.planid=g.planid and a.isallregion=0 and a.reglist like '%" + regId
				+ "|%' and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("g.userid"));
		return super.findBySql(sql.toString(), new Object[] {}, new int[] {});
	}

	public List<Integer> findGroupIdsByUserId(Integer userId) {
		if (userId == null) {
			return Collections.emptyList();
		}
		StringBuffer sql = new StringBuffer("select groupid from beidou.cprogroup where userid=? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));

		List<Map<String, Object>> result = super.findBySql(userId, sql.toString(), new Object[] { userId }, new int[] { Types.INTEGER });
		List<Integer> groupIdList = new ArrayList<Integer>();
		if (CollectionUtils.isNotEmpty(result)) {
			for (Map<String, Object> map : result) {
				groupIdList.add((Integer) (map.get("groupid")));
			}
		}
		return groupIdList;
	}

	public List<Integer> findUserIdByGroupIds(List<Integer> groupIds) {
		if (CollectionUtils.isEmpty(groupIds)) {
			return Collections.emptyList();
		}
		String groupIdStr = StringUtil.join(",", groupIds);

		StringBuffer sql = new StringBuffer("select distinct userid from beidou.cprogroup where groupid in (" + groupIdStr + ") and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		List<Map<String, Object>> result = super.findBySql(sql.toString(), new Object[] {}, new int[] {});

		List<Integer> userIdList = new ArrayList<Integer>();
		if (CollectionUtils.isNotEmpty(result)) {
			for (Map<String, Object> map : result) {
				userIdList.add((Integer) (map.get("userid")));
			}
		}
		return userIdList;
	}

	public List<Integer> findGroupIdsByUserIdAndTargettype(int userId, int targetType) {
		StringBuffer sql = new StringBuffer("select groupid from beidou.cprogroup where userid=? and targettype=? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		List<Map<String, Object>> result = super.findBySql(userId, sql.toString(), new Object[] { userId, targetType }, new int[] { Types.INTEGER, Types.INTEGER });

		List<Integer> groupIdList = new ArrayList<Integer>();
		for (Map<String, Object> row : result) {
			groupIdList.add((Integer) (row.get("groupid")));
		}
		return groupIdList;
	}

	public List<Integer> getRegionList(int groupId, int userId) {
		StringBuffer sql = new StringBuffer("select reglist from beidou.cprogroupinfo where groupid=? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));

		List<Map<String, Object>> result = super.findBySql(userId, sql.toString(), new Object[] { groupId }, new int[] { Types.INTEGER });
		if (result.isEmpty() || result.get(0).get("reglist") == null || result.get(0).get("reglist").equals("NULL")) {
			return Collections.emptyList();
		}

		String[] regArr = ((String) result.get(0).get("reglist")).trim().split("\\|");
		List<Integer> reglist = new ArrayList<Integer>();
		for (int i = 0; i < regArr.length; i++) {
			reglist.add(Integer.parseInt(regArr[i]));
		}
		return reglist;
	}

	public int getIsallregionTag(int groupId, int userId) {
		StringBuffer sql = new StringBuffer("select isallregion from beidou.cprogroupinfo where groupid=? and userid = ? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));

		GenericRowMapping<Integer> mapper = new GenericRowMapping<Integer>() {
			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				Integer intResult = rs.getInt("isallregion");
				return intResult;
			}
		};

		List<Integer> result = super.findBySql(userId, mapper, sql.toString(), new Object[] { groupId, userId }, new int[] { Types.INTEGER, Types.INTEGER });
		if (CollectionUtils.isEmpty(result)) {
			return 1;
		}
		return result.get(0);
	}

    @Override
    public void delInvalidGroupDelInfo() {
        StringBuilder sql = new StringBuilder(128);
        sql.append("DELETE FROM groupdelinfo USING groupdelinfo,cprogroup");
        sql.append(" WHERE groupdelinfo.groupid=cprogroup.groupid");
        sql.append(" AND cprogroup.groupstate<>2 and ");
        sql.append(MultiDataSourceSupport.geneateUseridStr("cprogroup.userid"));
        
        super.updateBySql(sql.toString());
    }
    
    @Override
    public List<CproGroupRegion> getGroupRegion(int sharding) {
        StringBuffer sql = new StringBuffer();
        sql.append("select userid, groupid, reglist from beidou.cprogroupinfo "
        		+ "where isallregion = 0 and reglist is NOT NULL and ");
        sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));

        GenericRowMapping<CproGroupRegion> mapper = new GenericRowMapping<CproGroupRegion>() {

            @Override
            public CproGroupRegion mapRow(ResultSet rs, int rowNum) throws SQLException {
                CproGroupRegion result = new CproGroupRegion();
                result.setGroupId(rs.getInt("groupid"));
                result.setRegListStr(rs.getString("reglist"));
                result.setUserId(rs.getInt("userid"));
                return result;
            }

        };

        return super.findBySqlOnSharding(sharding, mapper, sql.toString(), new Object[] {}, new int[] {});
    }

    @Override
    public List<SimpleGroup> getAllGroupIdByPrice(int price) {
        StringBuffer sql = new StringBuffer();
        sql.append("select g.userid, g.groupid from beidou.cprogroup g, beidou.cprogroupinfo f "
                + "where g.groupid=f.groupid and f.price<" + price + " and g.activity_state = 0 and ");
        sql.append(MultiDataSourceSupport.geneateUseridStr("g.userid"));

        GenericRowMapping<SimpleGroup> mapper = new GenericRowMapping<SimpleGroup>() {

            @Override
            public SimpleGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
                SimpleGroup result = new SimpleGroup();
                result.setGroupId(rs.getInt("groupid"));
                result.setUserId(rs.getInt("userid"));
                return result;
            }

        };
        
        return super.findBySql(mapper, sql.toString(), new Object[] {}, new int[] {});
    }
}