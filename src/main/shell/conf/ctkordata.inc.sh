#!/bin/bash

L1DAY=`date -d '1 days ago' +%Y%m%d`

LOCAL_DATA_PATH="${DATA_PATH}/ctkor"
LOCAL_CTWORD_PATH="${LOCAL_DATA_PATH}/ctword/${L1DAY}"

MYSQL_PATH="/home/work/beidou/mysql/bin/mysql"
MYSQL_HOST="db-bd-stdb-01.db01.baidu.com"
MYSQL_PORT="3306"
MYSQL_USER="beidou_cron_w"
MYSQL_PASS="bdcw7504Jnvhqira"
MYSQL_BIN="${MYSQL_PATH} -N -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASS} "

