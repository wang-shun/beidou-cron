package com.baidu.beidou.cprounit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.util.UrlParser;

/**
 * 统计端url点击数据入库
 * 
 * @author Wang Yu
 * 
 */
public class ImportClickUrl {

    private static final Log log = LogFactory.getLog(ImportClickUrl.class);

    private static Map<Long, List<String>> clickMap = new HashMap<Long, List<String>>();
    private static final String TAB = "\t";

    /**
     * 主函数
     * @param args 入口参数
     * @throws Exception Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            log.error("Usage: ImportClickUrl [file_click file_adinfo file_out date table_count]");
            throw new Exception("Usage: ImportClickUrl [file_click file_adinfo file_out date table_count]");
        }

        String fileClick = args[0]; // 点击日志
        String fileAdinfo = args[1]; // 创意日志
        String fileOut = args[2]; // 输出merge文件
        String date = args[3]; // 日期
        Integer tableCount = Integer.parseInt(args[4]); // 文件分片数

        initClickInfo(fileClick);
        analyze(fileAdinfo, fileOut, date, tableCount);
    }

    /**
     * merge点击日志和创意日志
     * 
     * @param unitFile 创意日志
     * @param outFile 输出日志
     * @param date 日期
     * @param tableCount merge文件数
     * @throws FileNotFoundException FileNotFoundException
     * @throws IOException IOException
     */
    private static void analyze(String unitFile, String outFile, String date, Integer tableCount)
            throws FileNotFoundException, IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(unitFile));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        DBFile dbFile = new DBFile(outFile, tableCount);
        String unitStr;
        String[] unitArr;
        List<String> clickList;
        String[] clickArr;
        int index;
        while ((unitStr = br.readLine()) != null) {
            try {
                if (StringUtils.isEmpty(unitStr)) {
                    continue;
                }

                unitArr = unitStr.split(TAB);
                if (unitArr == null || unitArr.length != 4) {
                    log.warn("unit data exception: " + unitStr);
                    continue;
                }

                Long aid = Long.parseLong(unitArr[0]);
                clickList = clickMap.get(aid);
                if (clickList != null && clickList.size() > 0) {
                    for (String clickStr : clickList) {
                        clickArr = clickStr.split(TAB);
                        index = unitStr.indexOf(TAB);
                        dbFile.write(unitArr[1], UrlParser.getMainDomain(clickArr[0]) + TAB + clickStr + TAB
                                + new String(unitStr.substring(index + 1)) + TAB + date);
                    }
                }
            } catch (Exception e) {
                log.warn("unit data exception: " + unitStr);
                continue;
            }
        }
        in.close();
        dbFile.close();
    }

    /**
     * 将点击日志写入HashMap
     * 
     * @param file 点击日志文件
     * @throws FileNotFoundException FileNotFoundException
     * @throws IOException IOException
     */
    private static void initClickInfo(String file) throws FileNotFoundException, IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        String[] tempArr;
        int count = 0;
        while ((strLine = br.readLine()) != null) {
            if (StringUtils.isEmpty(strLine)) {
                continue;
            }

            tempArr = strLine.split(TAB);
            if (tempArr == null || tempArr.length != 4) {
                log.warn("click data exception: " + strLine);
                continue;
            }

            try {
                long aid = Long.parseLong(tempArr[1]);
                if (clickMap.get(aid) == null) {
                    List<String> clickList = new ArrayList<String>();
                    clickList.add(strLine);
                    clickMap.put(aid, clickList);
                } else {
                    clickMap.get(aid).add(strLine);
                }
                count++;
            } catch (NumberFormatException e) {
                log.info("click info exception:" + strLine);
            }
        }
        log.info("load" + count + " clicks info to memory!");
        log.info("load" + clickMap.size() + " clicks info to memory!");
        in.close();
    }

    /**
     * 文件操作类
     * 
     * @author Wang Yu
     * 
     */
    private static class DBFile {

        private Map<Integer, PrintWriter> writerMap = new HashMap<Integer, PrintWriter>();
        private Integer tableCount;

        /**
         * 构造函数
         * 
         * @param file 文件名
         * @param tableCount 文件分片数
         * @throws FileNotFoundException FileNotFoundException
         * @throws IOException IOException
         */
        public DBFile(String file, Integer tableCount) throws FileNotFoundException, IOException {
            this.tableCount = tableCount;
            for (int i = 0; i < tableCount; i++) {
                writerMap.put(i, new PrintWriter(new BufferedWriter(new FileWriter((new File(file + i)), true))));
            }
        }

        /**
         * 写文件
         * 
         * @param uid 用户ID
         * @param data 写入日志内容
         */
        public void write(String uid, String data) {
            Integer userid = Integer.parseInt(uid);
            writerMap.get(userid % tableCount).println(data);
        }

        /**
         * 关闭文件
         */
        public void close() {
            for (int i = 0; i < tableCount; i++) {
                writerMap.get(i).close();
            }
        }
    }
}