#!/bin/bash
#@file: recover_finish_task.sh
#@author: zhangxu04
#@date: 2011-05-18
#@version: 1.0.0.0
#@brief: ����˾��ϵͳ���Ѿ����������������dataĿ¼������finish_tid_url.txt�ļ�

#--------------- var  --------------
CONF_SH=common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

DATA_PATH=$BASE_PATH/"data"

FINISH_TID_URL_FILE=$DATA_PATH/"finish_tid_url.txt"

#--------------- main  --------------

# ���������Ѿ���������tid��siteurlӳ���ϵ�ļ����Ѿ���������statusΪ16
EXPORT_TID_SITEURL_SQL="SELECT a.tid, b.multiUrl FROM sn_task a JOIN sn_url_s b on a.tid=b.tid WHERE a.tstatus in (12,32);"
msg="����˾���Ѿ�������ĳɹ�����ʧ��"
$MYSQL_BIN -h$SINAN_DB_IP -P$SINAN_DB_PORT -u$SINAN_DB_USER -p$SINAN_DB_PASSWORD sinan_beidou --skip-column-name -e "$EXPORT_TID_SITEURL_SQL" > $FINISH_TID_URL_FILE
alert $? ${msg}
echo "����˾���Ѿ�������ĳɹ�����ɹ�"

exit 0

