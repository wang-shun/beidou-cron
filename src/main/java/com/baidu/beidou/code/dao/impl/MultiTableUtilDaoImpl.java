/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.code.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.transaction.annotation.Transactional;

import com.baidu.beidou.code.dao.MultiTableUtilDao;
import com.baidu.beidou.util.Pair;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.string.StringUtil;

/**
 * Created by hewei18 on 2016-04-07.
 */
public class MultiTableUtilDaoImpl extends GenericDaoImpl implements MultiTableUtilDao {

    @Override
    public List<Pair<Object, Object>> findPkAndColumnValue(String tableName, String pkColumnName, String columnName,
                                                           Collection<Integer> userIds) {
        String sql = String.format("SELECT %s, %s FROM %s", pkColumnName, columnName, tableName);
        if (CollectionUtils.isNotEmpty(userIds)) {
            sql = String.format("%s WHERE userid in (%s)", sql, StringUtil.join(",", userIds));
        }
        return this.findBySql(new GenericRowMapping<Pair<Object, Object>>() {
            @Override
            public Pair<Object, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                Pair<Object, Object> rowValue = new Pair<Object, Object>();
                rowValue.setFirst(rs.getObject(1));
                rowValue.setSecond(rs.getObject(2));
                return rowValue;
            }
        }, sql, null);
    }

    @Transactional
    @Override
    public int[] updateTableColumn(String tableName, String pkColumName, String columnName,
                                   final List<Pair<Object, Object>> pkValueList) {
        String sql = String.format("UPDATE %s SET %s = ? WHERE %s = ?", tableName, columnName, pkColumName);
        return this.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Pair<Object, Object> pkValue = pkValueList.get(i);
                Object pk = pkValue.getFirst();
                Object value = pkValue.getSecond();
                ps.setObject(1, value);
                ps.setObject(2, pk);
            }

            @Override
            public int getBatchSize() {
                return pkValueList.size();
            }
        });
    }
}
