package com.baidu.beidou.cprogroup.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.cprogroup.bo.CproKeyword;
import com.baidu.beidou.cprogroup.dao.CproKeywordDao;
import com.baidu.beidou.cprogroup.dao.rowmap.CproKeyWordComplexRowMapping;
import com.baidu.beidou.cprogroup.dao.rowmap.CproKeyWordSimpleRowMapping;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;
import com.baidu.beidou.util.string.StringUtil;

/**
 * @author zhuqian
 * 
 */
public class CproKeywordDaoImpl extends MultiDataSourceDaoImpl<CproKeyword> implements CproKeywordDao {

	@Deprecated
	public List<CproKeyword> getCproKeywordsByGroup(Integer groupId, Integer userId) {
		if (groupId == null || userId == null) {
			return Collections.emptyList();
		}
		StringBuffer sql = new StringBuffer();
		String tableName = "beidou.cprokeyword" + (userId % CPROKEYWORD_TABLE_SLICE);
		sql.append("select wordid from ");
		sql.append(tableName);
		sql.append(" where groupid = ? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		sql.append(" order by keywordid");
		return super.findBySql(userId, new CproKeyWordSimpleRowMapping(), sql.toString(), new Object[] { groupId }, new int[] { Types.INTEGER });
	}

	public List<CproKeyword> findByGroupIds(List<Integer> groupIdList, Integer userId) {
		if (CollectionUtils.isEmpty(groupIdList) || userId == null || userId < 1) {
			return Collections.emptyList();
		}
		String groupIdStr = StringUtil.join(",", groupIdList);

		StringBuffer sql = new StringBuffer();
		String tableName = "beidou.cprokeyword" + (userId % CPROKEYWORD_TABLE_SLICE);
		sql.append("select userid,planid,groupid,wordid from ");
		sql.append(tableName);
		sql.append(" where groupid in (" + groupIdStr + ") and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		sql.append(" order by groupid");

		return super.findBySql(userId, new CproKeyWordComplexRowMapping(), sql.toString(), new Object[] {}, new int[] {});
	}

    public int countByGroupId(Integer groupId, Integer userId) {
        if (userId == null || userId < 1 || groupId == null) {
            return 0;
        }

        StringBuffer sql = new StringBuffer();
        String tableName = "beidou.cprokeyword" + (userId % CPROKEYWORD_TABLE_SLICE);
        sql.append("select count(keywordid) as wordnum from ");
        sql.append(tableName);
        sql.append(" where groupid =" + groupId + " and ");
        sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));

        List<Integer> count = super.findBySql(userId, new GenericRowMapping<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("wordnum");
            }

        }, sql.toString(), new Object[] {}, new int[] {});

        if (CollectionUtils.isEmpty(count) || count.size() > 1) {
            return 0;
        }

        return count.get(0);
    }
    
    public List<Integer> filterGroupIdByKeywordCount(List<Integer> groupIds, Integer userId, int countLimit) {
        if (userId == null || userId < 1 || CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }

        String groupIdStr = StringUtil.join(",", groupIds);
        StringBuffer sql = new StringBuffer();
        String tableName = "beidou.cprokeyword" + (userId % CPROKEYWORD_TABLE_SLICE);
        sql.append("select groupid from ");
        sql.append(tableName);
        sql.append(" where userid = " + userId + " and groupid in(" + groupIdStr + ") and ");
        sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));
        sql.append("group by groupid having count(keywordid)>" + countLimit);

        List<Integer> resultGroups = super.findBySql(userId, new GenericRowMapping<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("groupid");
            }

        }, sql.toString(), new Object[] {}, new int[] {});

        return resultGroups;
    }

    public List<Long> getCproKeywordIdsByGroup(Integer groupId, Integer userId) {
        if (groupId == null || userId == null) {
            return Collections.emptyList();
        }
        StringBuffer sql = new StringBuffer();
        String tableName = "beidou.cprokeyword" + (userId % CPROKEYWORD_TABLE_SLICE);
        sql.append("select keywordid from ");
        sql.append(tableName);
        sql.append(" where groupid = ? and ");
        sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));
        sql.append(" order by keywordid");
        return super.findBySql(userId, new GenericRowMapping<Long>() {

            @Override
            public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getLong("keywordid");
            }

        }, sql.toString(), new Object[] { groupId }, new int[] { Types.INTEGER });
    }
}