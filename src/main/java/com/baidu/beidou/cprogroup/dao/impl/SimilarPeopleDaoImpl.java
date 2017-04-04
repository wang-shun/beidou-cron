/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.hsqldb.Types;

import com.baidu.beidou.cprogroup.bo.SimilarPeople;
import com.baidu.beidou.cprogroup.dao.SimilarPeopleDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.unbiz.common.CollectionUtil;

/**
 * 相似人群DAO层实现类
 * 
 * @author Wang Yu
 * 
 */
public class SimilarPeopleDaoImpl extends GenericDaoImpl implements SimilarPeopleDao {
    @Override
    public SimilarPeople findSimilarPeopleByGroupId(Integer groupId) {
        if (groupId == null) {
            return null;
        }

        StringBuilder sql = new StringBuilder(128);
        sql.append("SELECT groupid, pid, hpid, name, stat, alivedays, cookienum,");
        sql.append(" userid, activetime, addtime, modtime, adduser, moduser");
        sql.append(" FROM similar_people where groupid = ?");

        List<SimilarPeople> result = super.findBySql(new GenericRowMapping<SimilarPeople>() {

            @Override
            public SimilarPeople mapRow(ResultSet rs, int rowNum) throws SQLException {
                SimilarPeople item = new SimilarPeople();
                item.setGroupId(rs.getInt("groupid"));
                item.setPid(rs.getLong("pid"));
                item.setHpid(rs.getLong("hpid"));
                item.setName(rs.getString("name"));
                item.setStat(rs.getInt("stat"));
                item.setAlivedays(rs.getInt("alivedays"));
                item.setCookienum(rs.getLong("cookienum"));
                item.setUserid(rs.getInt("userid"));
                item.setActivetime(rs.getDate("activetime"));
                item.setAddtime(rs.getDate("addtime"));
                item.setModtime(rs.getDate("modtime"));
                item.setAdduser(rs.getInt("adduser"));
                item.setModuser(rs.getInt("moduser"));
                return item;
            }

        }, sql.toString(), new Object[] { groupId }, new int[] { Types.INTEGER });

        if (CollectionUtil.isEmpty(result)) {
            return null;
        } else {
            return result.get(0);
        }
    }

    @Override
    public boolean createSimilarPeople(SimilarPeople similarPeople) {
        if (similarPeople == null) {
            return false;
        }

        StringBuilder sql = new StringBuilder(128);
        sql.append("INSERT INTO similar_people(groupid, pid, hpid, name, stat, alivedays,");
        sql.append(" cookienum, userid, activetime, addtime, modtime, adduser, moduser)");
        sql.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        Object[] params =
                new Object[] { similarPeople.getGroupId(), similarPeople.getPid(), similarPeople.getHpid(),
                        similarPeople.getName(), similarPeople.getStat(), similarPeople.getAlivedays(),
                        similarPeople.getCookienum(), similarPeople.getUserid(), similarPeople.getActivetime(),
                        similarPeople.getAddtime(), similarPeople.getModtime(), similarPeople.getAdduser(),
                        similarPeople.getModuser() };
        int[] paramTypes =
                new int[] { Types.INTEGER, Types.BIGINT, Types.BIGINT, Types.VARCHAR, Types.INTEGER, Types.INTEGER,
                        Types.BIGINT, Types.INTEGER, Types.TIMESTAMP, Types.TIMESTAMP, Types.TIMESTAMP, Types.INTEGER,
                        Types.INTEGER };

        int effectRow = super.updateBySql(sql.toString(), params, paramTypes);
        return effectRow == 1 ? true : false;
    }
}
