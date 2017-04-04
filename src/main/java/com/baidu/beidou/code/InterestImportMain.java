/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.code;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.code.service.InterestService;

/**
 * Created by hewei18 on 2016-03-29.
 */
public class InterestImportMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterestImportMain.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            LOGGER.error("Wrong argument. The 1st argument is interest file. The 2nd argument is valid "
                    + "interest id file");
            return;
        }
        File interestFile = new File(args[0]);
        File validIdFile = new File(args[1]);

        String[] fn =
                new String[] { "applicationContext.xml"};
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fn);
        InterestService service = ctx.getBean(InterestService.class);
        try {
            service.importInterest(interestFile, validIdFile);
        } catch (IOException e) {
            LOGGER.error("read from file error=" + args[1]);
        }
    }
}
