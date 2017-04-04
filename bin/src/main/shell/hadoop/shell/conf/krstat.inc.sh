#!/bin/bash

L0DAY=`date +%Y%m%d`
L1DAY=`date -d '1 days ago' +%Y%m%d`
L2DAY=`date -d '2 days ago' +%Y%m%d`
L3DAY=`date -d '3 days ago' +%Y%m%d`
L4DAY=`date -d '4 days ago' +%Y%m%d`
L5DAY=`date -d '5 days ago' +%Y%m%d`
L6DAY=`date -d '6 days ago' +%Y%m%d`
L7DAY=`date -d '7 days ago' +%Y%m%d`
LXDAYS="${L1DAY} ${L2DAY} ${L3DAY} ${L4DAY} ${L5DAY} ${L6DAY} ${L7DAY}"

JAR_FILE="${LIB_PATH}/qtkr.jar"


LOCAL_DATA_PATH="${DATA_PATH}/krstat"
LOCAL_ADVIEW_PATH="${LOCAL_DATA_PATH}/adview/${L1DAY}"
LOCAL_OUTPUT_PATH="${LOCAL_DATA_PATH}/output/${L1DAY}"

SRC_UFS_PATH="/app/ecom/cm/cm_ufs/ufs/qtkr/cookie/"
SRC_ADVIEW_PATH="/app/ecom/cm/cm_rank/afs/qtkr/adview"

OUTPUT_PATH="/app/ecom/cm/cm_rank/afs/qtkr/output/${L1DAY}/0000/"
