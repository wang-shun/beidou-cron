package com.baidu.beidou.cprounit.service.syncubmc.vo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.util.DateUtils;

/**
 * admaker 物料检查对象
 * @author work
 *
 */
public class AdmakerUnitMaterCheckSet {

    private static final Log log = LogFactory.getLog(AdmakerUnitMaterCheckSet.class);

    private int total;
    private BufferedReader reader = null;

    /**
     * 构造函数
     * @param fileName
     */
    public AdmakerUnitMaterCheckSet(String fileName) {
        total = 0;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取下一批量
     * @param maxNum
     * @return
     */
    public synchronized List<UnitMaterCheckView> getNextList(int maxNum) {
        List<UnitMaterCheckView> result = new ArrayList<UnitMaterCheckView>();
        if (maxNum <= 0) {
            return result;
        }

        int num = 0;
        String line = null;
        try {
            int column = 0;
            while ((line = reader.readLine()) != null && (!StringUtils.isEmpty(line))) {
                String[] items = line.split("\t");

                column = 0;
                UnitMaterCheckView mater = new UnitMaterCheckView();
                mater.setId(Long.valueOf(items[column++]));
                mater.setUserId(Integer.valueOf(items[column++]));
                Date chaTime = DateUtils.getDateFromStr(items[column++]);
                mater.setChaTime(chaTime);

                mater.setWuliaoType(Integer.valueOf(items[column++]));

                mater.setHeight(dealEmptyInt(items[column++]));
                mater.setWidth(dealEmptyInt(items[column++]));

                mater.setMcId(Long.valueOf(items[column++]));
                mater.setMcVersionId(Integer.valueOf(items[column++]));
                mater.setState(Integer.valueOf(items[column++]));
                result.add(mater);

                num++;
                total++;
                if (num >= maxNum) {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            log.error("file not found...", e);
        } catch (IOException e) {
            log.error("read data from file failed...", e);
        } catch (Exception e) {
            log.error("read data from file failed for line=" + line, e);
        }

        return result;
    }

    /**
     * 特殊字符
     * @param str
     * @return
     */
    private synchronized String dealEscapeChar(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        try {
            str = str.replace("\\\\", "\\");
            str = str.replace("\\t", "\t");
            str = str.replace("\\n", "\n");
            str = str.replace("\\0", "\0");
            str = str.replace("\\r", "\r");
        } catch (Exception e) {
            return str;
        }
        return str;
    }

    /**
     * 空字符处理
     * @param str
     * @return
     */
    private synchronized String dealEmptyStr(String str) {
        if (StringUtils.isEmpty(str) || str.equalsIgnoreCase("null")) {
            return null;
        }
        return str;
    }

    /**
     * 空整型
     * @param str
     * @return
     */
    private synchronized Integer dealEmptyInt(String str) {
        if (StringUtils.isEmpty(str) || str.equalsIgnoreCase("null")) {
            return 0;
        }
        return Integer.valueOf(str);
    }

    /**
     * 关闭文件
     */
    public void closeFile() {
        try {
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取total
     * @return
     */
    public int getTotal() {
        return total;
    }

    /**
     * 设置total
     * @param total
     */
    public void setTotal(int total) {
        this.total = total;
    }
}
