/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.code;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.code.service.InterestService;

/**
 * Created by hewei18 on 2016-04-07.
 */
public class InterestConvertMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterestImportMain.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            LOGGER.error("Wrong argument. The 1st argument is user id file. "
                    + "The 2nd argument is old to new id map file.");
            return;
        }
        System.out.println(Arrays.toString(args));
        File userIdFile = new File(args[0]);
        File idMapFile = new File(args[1]);

        String[] fn =
                new String[] { "applicationContext.xml"};
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fn);
        InterestService service = ctx.getBean(InterestService.class);
        boolean success = false;
        try {
            success = service.convertItIdToNew(userIdFile, idMapFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (! success) {
            System.exit(1);
        }
    }
}
