#!/bin/bash

#@file: site_whitelist.sh
#@author: caichao


CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


CONF_SH="./alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


program=wm123_filter_site.sh



WORK_PATH=${DATA_PATH}/wm123_filter_site
LOG_PATH=${LOG_PATH}/wm123_filter_site
LOG_NAME=wm123
LOG_FILE=${LOG_PATH}/${LOG_NAME}.${curr_date}.log 


mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
rm -rf ${WORK_PATH}
mkdir -p ${WORK_PATH}

function INF()
{
 echo $1
 echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` "$1 >> $LOG_FILE
}
function ERR()
{
 echo $1
 echo "[ERROR] `date +"%Y-%m-%d %H:%M:%S"` "$1 >> $LOG_FILE
}

INF "begin task..."


ANTI_DP=${WORK_PATH}/anti_dp_data.txt
ANTI_NET_CAFE=${WORK_PATH}/anti_net_cafe_data.txt
ANTI_SOFTWARE_WINDOW_MINISITE=${WORK_PATH}/anti_net_cafe_data.txt
WM123_SITE=${WORK_PATH}/wm123_site.sql


IP="10.42.7.105"

cd ${WORK_PATH}
msg="down file anti_dp_data fail"
wget -t 3 -c -q http://${IP}/dr-mgr/common/downloadpublishfile/anti_dp/anti_dp_data.txt 
#wget ftp://cq01-rdqa-pool181.cq01.baidu.com/home/beidou/caichao/temp/anti_dp_data.txt
alert $? "${msg}"

msg="down file anti_dp_data md5 fail"
wget -t 3 -cq http://${IP}/dr-mgr/common/downloadpublishfile/anti_dp/anti_dp_data.txt.md5
alert $? "${msg}"

msg="check md5 fail"
md5sum -c anti_dp_data.txt.md5
alert $? "${msg}"

msg="down file anti_net_cafe_data fail"
wget -t 3 -cq http://${IP}/dr-mgr/common/downloadpublishfile/anti_net_cafe/anti_net_cafe_data.txt
#wget ftp://cq01-rdqa-pool181.cq01.baidu.com/home/beidou/caichao/temp/anti_net_cafe_data.txt
alert $? "${msg}"

msg="down file anti_net_cafe_data md5 fail"
wget -t 3 -cq http://${IP}/dr-mgr/common/downloadpublishfile/anti_net_cafe/anti_net_cafe_data.txt.md5
alert $? "${msg}"

msg="check md5 fail"
md5sum -c anti_net_cafe_data.txt.md5
alert $? "${msg}"

msg="down file anti_software_window_minisite_data fail"
wget -t 3 -cq http://${IP}/dr-mgr/common/downloadpublishfile/anti_software_window_minisite/anti_software_window_minisite_data.txt
#wget ftp://cq01-rdqa-pool181.cq01.baidu.com/home/beidou/caichao/temp/anti_software_window_minisite_data.txt
alert $? "${msg}"

mgs="down file anti_software_window_minisite_data md5 fail" 
wget -t 3 -cq http://${IP}/dr-mgr/common/downloadpublishfile/anti_software_window_minisite/anti_software_window_minisite_data.txt.md5
alert $? "${msg}"

msg="check md5 fail"
md5sum -c anti_software_window_minisite_data.txt.md5
alert $? "${msg}"

cat anti_dp_data.txt anti_net_cafe_data.txt anti_software_window_minisite_data.txt | awk -F'\t' '{print $1}' | sort -u | awk '{print "insert into beidouext.site_whitelist(site,addtime) values(\""$1"\",now());"}' > ${WM123_SITE}

msg="delete data fail"
runsql_xdb "delete from beidouext.site_whitelist";
alert $? "${msg}"

msg="insert data fail"
runsql_xdb "source ${WM123_SITE}"
alert $? "${msg}"

INF "finished"
