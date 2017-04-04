/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.code.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import com.baidu.beidou.code.bo.Interest;
import com.baidu.beidou.code.dao.InterestDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;

/**
 * Created by hewei18 on 2016-03-29.
 */
public class InterestDaoImpl extends GenericDaoImpl implements InterestDao {

    @Override
    public int[] saveOrReplace(final List<Interest> interestList) {
        String sql =
                "REPLACE INTO beidoucode.new_interest (interestid, name, parentid, orderid, type) VALUES "
                        + "(?,?,?,?,?)";
        return this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Interest interest = interestList.get(i);
                if (interest == null) {
                    return;
                }
                ps.setInt(1, interest.getInterestId());
                ps.setString(2, interest.getName());
                ps.setInt(3, interest.getParentId());
                ps.setInt(4, interest.getOrderId());
                ps.setInt(5, interest.getType());
            }

            @Override
            public int getBatchSize() {
                return interestList.size();
            }
        });
    }

    @Override
    public boolean deleteAll() {
        return this.getJdbcTemplate().update("DELETE FROM beidoucode.new_interest") == 1;
    }

}
