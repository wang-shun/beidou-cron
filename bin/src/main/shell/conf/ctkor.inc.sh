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

LOCAL_DATA_PATH="${DATA_PATH}/ctkor"
LOCAL_OUTPUT_PATH="${LOCAL_DATA_PATH}/output/${L1DAY}"

SRC_CTWORD_PATH="/app/ecom/cm/cm_rank/afs/ctkor/ctword"
OUTPUT_AVERAGE_PATH="/app/ecom/cm/cm_rank/afs/ctkor/average/${L1DAY}/0000/"
OUTPUT_ADVIEW_RECOMMEND_PATH="/app/ecom/cm/cm_rank/afs/ctkor/recommend/${L1DAY}/0000/adview/"
OUTPUT_ADVIEW_TRANSPOSE_PATH="/app/ecom/cm/cm_rank/afs/ctkor/transpose/${L1DAY}/0000/adview/"
OUTPUT_CTR_RECOMMEND_PATH="/app/ecom/cm/cm_rank/afs/ctkor/recommend/${L1DAY}/0000/ctr/"
OUTPUT_CTR_TRANSPOSE_PATH="/app/ecom/cm/cm_rank/afs/ctkor/transpose/${L1DAY}/0000/ctr/"

NUM_RECOMMEND_ADVIEW=120
NUM_TRANSPOSE_ADVIEW=100
NUM_RECOMMEND_CTR=120
NUM_TRANSPOSE_CTR=100

