#!/bin/env python
# coding=utf-8
#
#   Author: tianxin
#   E-mail: tianxin@baidu.com
#   Function: sync the interest data
#

import os
import re
import logging
import ConfigParser
import glob
import datetime
import time
import calendar
import traceback
import commands
import sys

# init logger
def initlog(logFile):
    # 创建一个logger 
    logger = logging.getLogger('interestSyncLogger')
    logger.setLevel(logging.DEBUG)
    # 创建一个handler，用于写入日志文件 
    fh = logging.FileHandler(logFile) 
    fh.setLevel(logging.DEBUG) 
    # 再创建一个handler，用于输出到控制台 
    ch = logging.StreamHandler() 
    ch.setLevel(logging.DEBUG) 
    # 定义handler的输出格式 
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s') 
    fh.setFormatter(formatter)
    ch.setFormatter(formatter) 
    # 给logger添加handler 
    logger.addHandler(fh) 
    logger.addHandler(ch) 
    return logger

# execute system command
def executeSystemCmd(cmd):
	(status,output)=commands.getstatusoutput(cmd)
	if status != 0:
            LOG.error("run system command fail:%s. error info:%s" %(cmd, output))
            raise Exception("error to execute cmd")
	return (status,output)

# query from interest and transfer result to map
# query from interest and transfer result to map
def queryInterest():
        sql = "select interestid,name,parentid,type,orderId from beidoucode.interest"
        (s,rs) = executeSystemCmd(cf.get("db", "prefix") + '"' + sql + '"')
        map = {}
        if len(rs) > 0 :
                for line in rs.split('\n'):
                        es = line.split('\t')
                        map[es[0]] = es
        return map

# analysis interest file and transfer the content to map
def analysisFile(interestFile):
        file = open(interestFile)
        lines = file.readlines()
        map = {}
        order = 1;
        for line in lines:
            line = line[:-1]
            words = line.split('\t')
            interestId = words[0]
            parentId = words[1]
            name = words[2]
            type = words[3]
            newArray = [interestId,parentId,name,type,str(order)]
            map[interestId] = newArray
            order += 1
        file.close()
        return map

# define interest info into database
def mergeInterest2DB(interestFile):
        recordMap = queryInterest()
        fileMap = analysisFile(interestFile)

        sql = "start transaction;"
        
        for key,value in recordMap.items():
                newValue = fileMap.get(key);
                if newValue is None:
                        print("节点" + key + " 被删除!")
                        exit(1)
                if not isSame(value, newValue):
                        sql = updateNewValue(newValue, sql)

        for key,value in fileMap.items():
                if recordMap.get(key) is None:
                        sql = insertNewValue(value, sql)

        sql += "commit;"
        print(sql)
        (s,rs) = executeSystemCmd(cf.get("db", "prefix") + '"' + sql + '"')
        if s != 0:
            exit(1)

def isSame(value, newValue):
        return value[2] == newValue[1] and value[1] == newValue[2] and value[3] == newValue[3] and value[4] == newValue[4]
        
# update new record into interest table
def updateNewValue(value,mainSql):
        sql = "update beidoucode.interest set name='" + value[2] + "',parentid=" + value[1] + ",type=" + value[3] + ",orderId=" + value[4] + " where interestid=" + value[0] + ";"
        mainSql += sql 
        return mainSql

# insert new record into interest table
def insertNewValue(value, mainSql):
        sql = "insert into beidoucode.interest(interestid,parentid,name,type,orderId) values(" + value[0] + "," + value[1] + ",'" + value[2] + "'," + value[3] + "," + value[4] + ");"
        mainSql += sql
        return mainSql
    
LOG = initlog(sys.argv[3]) 
cf = ConfigParser.ConfigParser()

# main method
if __name__ == "__main__":
        cf.read(sys.argv[2])
        interestFile = sys.argv[1]
        mergeInterest2DB(interestFile)
