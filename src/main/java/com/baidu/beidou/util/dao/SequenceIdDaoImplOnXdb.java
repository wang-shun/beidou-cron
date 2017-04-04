/**
 * beidou-core-535#com.baidu.beidou.util.dao.SequenceIdDaoImplOnXdb.java
 * 下午1:08:16 created by Darwin(Tianxin)
 */
package com.baidu.beidou.util.dao;

import java.sql.Types;

import com.baidu.beidou.stat.util.dao.BaseDAOSupport;

/**
 * 位于扩展库上的SequenceId
 * @author hanxu03
 */
public class SequenceIdDaoImplOnXdb extends BaseDAOSupport implements SequenceIdDaoOnXdb {
	
	private final static String SEQ_OPT_HISTORY = "opthistoryid";
	private final static String SEQ_ACTION_HISTORY = "actionhistoryid";
	private final static String SEQ_HISTORY_TEXT = "historytextid";
	private final static String SEQ_UNION_SITEID_TYPE = "unionsiteidtype";

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.beidou.util.dao.SequenceIdDao#getNextId(java.lang.String)
     */
    protected Long getNextId(String type) {
    	
        Long result = null;
        String procedure = "select history.get_next_value (?)";
        result = super.getJdbcTemplate().queryForLong(procedure, new Object[] { type },
                        new int[] { Types.VARCHAR });
        
        if (result < 1) {
            throw new ArithmeticException("sequence id is not allow to be less than 1");
        }
        return result;
    }

    protected Long getNextIdBatch(String type, int step) {
    	
        if (step <= 0) {
        	return 0L;
        }

        // 调用function
        String procedure = "select history.get_next_values (?, ?)";
        Long o = super.getJdbcTemplate().queryForLong(procedure, new Object[] { type, step },
                        new int[] { Types.VARCHAR, Types.INTEGER });

        // 结果验证
        if (o < 1) {
            throw new ArithmeticException("sequence id is not allow to be less than 1");
        }
        
        return o;
    }
    
    protected Long[] getNextId(String type, int step) {
    	
        if (step <= 0) {
        	return new Long[0];
        }

        long start = getNextIdBatch(type, step);
        
        Long [] ids = new Long[step];
        for(int i = 0; i < step; i ++ ){
			ids[i] = start + i;
        }
        
        return ids;
    }
    

	public Long getOptHistoryId() {
		return getNextId(SEQ_OPT_HISTORY);
	}

	public Long getActionHistoryId() {
		return getNextId(SEQ_ACTION_HISTORY);
	}

	public Long getHistoryTextId() {
		return getNextId(SEQ_HISTORY_TEXT);
	}

	public Long getUnionSiteidTypeId() {
		return getNextId(SEQ_UNION_SITEID_TYPE);
	}

}
