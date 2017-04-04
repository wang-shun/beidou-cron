#!/bin/bash
#@file: recover_finish_task.sh
#@author: zhangxu04
#@date: 2011-05-18
#@version: 1.0.0.0
#@brief: 导出司南系统里已经跑完的所有任务，在data目录下生成finish_tid_url.txt文件

#--------------- var  --------------
CONF_SH=common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

DATA_PATH=$BASE_PATH/"data"

FINISH_TID_URL_FILE=$DATA_PATH/"finish_tid_url.txt"

#--------------- main  --------------

# 导出所有已经完成任务的tid和siteurl映射关系文件，已经完成任务的status为16
EXPORT_TID_SITEURL_SQL="SELECT a.tid, b.multiUrl FROM sn_task a JOIN sn_url_s b on a.tid=b.tid WHERE a.tstatus in (12,32);"
msg="导出司南已经运行完的成功任务失败"
$MYSQL_BIN -h$SINAN_DB_IP -P$SINAN_DB_PORT -u$SINAN_DB_USER -p$SINAN_DB_PASSWORD sinan_beidou --skip-column-name -e "$EXPORT_TID_SITEURL_SQL" > $FINISH_TID_URL_FILE
alert $? ${msg}
echo "导出司南已经运行完的成功任务成功"

exit 0

