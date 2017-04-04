#!/bin/bash

#@file: fix_ubmc_material.sh
#@author: genglei
#@date: 2009-06-29
#@version: 1.0.0.0

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=fix_ubmc_material.sh
reader_list=genglei

CONF_SH=../lib/db_sharding_ubmc.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE
if [ $? -ne 0 ]
then
	echo "Conf error: Fail to load libfile[$LIB_FILE]!"
	exit 1
fi

LOG_NAME=fix_ubmc_material
LOG_LEVEL=8
LOG_SIZE=1800000

LOCAL_TEMP=${DATA_PATH}/fix_ubmc_material/input
FILE_PRE=fix_ubmc.sql

mkdir -p $LOCAL_TEMP
DAT=`date +%Y%m%d`

cd $LOCAL_TEMP
mv $FILE_PRE $FILE_PRE"."$DAT
wget ftp://db-beidou-rd02.db01.baidu.com/home/work/genglei/online/rd_op/ubmc_fix_data/$FILE_PRE
if [ $? -ne 0 ]
then
	exit 0
fi

cd $BIN_PATH

log "INFO" "start to fix ubmc material"

SQL_FILE=$LOCAL_TEMP/$FILE_PRE
log "INFO" "start to execute file $SQL_FILE"
runfilesql_sharding "$SQL_FILE" $TAB_UNIT_SLICE
unitNum=`cat $SQL_FILE | wc -l`
log "INFO" "execute fix ubmc material num:\t$unitNum"

log "INFO" "end to fix ubmc material"
