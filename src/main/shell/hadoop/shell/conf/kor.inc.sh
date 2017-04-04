#!/bin/bash

JAR_FILE="${LIB_PATH}/beidou-cf.jar"

L0DAY=`date +%Y%m%d`
L1DAY=`date -d '1 days ago' +%Y%m%d`
L2DAY=`date -d '2 days ago' +%Y%m%d`
L3DAY=`date -d '3 days ago' +%Y%m%d`
L4DAY=`date -d '4 days ago' +%Y%m%d`
L5DAY=`date -d '5 days ago' +%Y%m%d`
L6DAY=`date -d '6 days ago' +%Y%m%d`
L7DAY=`date -d '7 days ago' +%Y%m%d`
LXDAYS="${L1DAY} ${L2DAY} ${L3DAY} ${L4DAY} ${L5DAY} ${L6DAY} ${L7DAY}"

LOCAL_DATA_PATH="${DATA_PATH}/ktkor"
LOCAL_OUTPUT_PATH="${LOCAL_DATA_PATH}/output/${L1DAY}"

SRC_SUB_PATH="db-cm-dcapp03.db01/"
SRC_KTWORD_PATH="/log/1080/beidou_xtkor_db_data"
SRC_FILTER_FILE="/log/1080/beidou_xtkor_filter/${L1DAY}/0000/db-cm-dcapp03.db01/krRefuseWords.${L1DAY}"
OUTPUT_AVERAGE_PATH="/app/ecom/cm/cm_rank/afs/xtkor/average/${L1DAY}/0000/"
OUTPUT_ADVIEW_RECOMMEND_PATH="/app/ecom/cm/cm_rank/afs/xtkor/recommend/${L1DAY}/0000/adview/"

NUM_RECOMMEND_ADVIEW=100

