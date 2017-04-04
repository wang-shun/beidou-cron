/**
 * Copyright (C) 2015年10月26日 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.indexgrade.filehandle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件操作的共用handler
 * 
 * @author wangxiongjie
 * 
 */
public class FileHandler {

    private static Logger LOG = LoggerFactory.getLogger(FileHandler.class);

    public static <T> void exportToFile(List<T> info, File dest) {

        BufferedWriter writer = null;
        try {
            if (!dest.exists()) {
                dest.createNewFile();
            }
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest), "utf-8"));

            for (T o : info) {
                writer.write(o.toString());
                writer.newLine();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("output group info failed");
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static <T> void loadFromFile(File fromFile, Collection<T> destCollection, RecordHandler<T> rowHandler) {

        BufferedReader reader = null;
        String line = null;
        try {
            if (!fromFile.exists()) {
                LOG.error("the load file is not exist.");
                return;
            }
            reader = new BufferedReader(new FileReader(fromFile));
            while ((line = reader.readLine()) != null) {
                T obj = rowHandler.handleRow(line);
                destCollection.add(obj);
            }
        } catch (FileNotFoundException e) {
            LOG.error("the load file can not found.");
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("read file cause io exception.");
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
