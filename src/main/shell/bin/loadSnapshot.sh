#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importSnapshot.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=loadSnapshot.sh
reader_list=liangshimu

LOG_FILE=${LOG_PATH}/loadSnapshot.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_DATA_PATH}
mkdir -p ${BEIDOU_DATA_PATH}
mkdir -p ${CRON_SNAPSHOT_PATH}/${YESTERDAY_YYYYMMDD}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录${CRON_SNAPSHOT_PATH}/${YESTERDAY_YYYYMMDD}失败"
cd ${CRON_SNAPSHOT_PATH}/${YESTERDAY_YYYYMMDD}
alert $? ${msg}


#抓取文件
msg="wget文件昨天截图文件失败"
#wget -q  ${SNAPSHOT_RESULT_URL}/${SNAPSHOT_FILE}
wget -ndpr ${SNAPSHOT_IMAGE_URL}/${YESTERDAY_YYYYMMDD}/
#echo ${SNAPSHOT_IMAGE_URL}/${YESTERDAY_YYYYMMDD}/
alert $? ${msg}

cd ..
msg="拷贝图片到${aimgr00}失败"
rsync -auv -e ssh ${YESTERDAY_YYYYMMDD}/ ${aimgr00}:${WM123_SNAPSHOT_PATH}/
alert $? ${msg}
msg="拷贝图片到${aimgr01}失败"
rsync -auv -e ssh ${YESTERDAY_YYYYMMDD}/ ${aimgr01}:${WM123_SNAPSHOT_PATH}/
alert $? ${msg}
msg="拷贝图片到${tcmgr00}失败"
rsync -auv -e ssh ${YESTERDAY_YYYYMMDD}/ ${tcmgr00}:${WM123_SNAPSHOT_PATH}/
alert $? ${msg}
msg="拷贝图片到${tcmgr01}失败"
rsync -auv -e ssh ${YESTERDAY_YYYYMMDD}/ ${tcmgr01}:${WM123_SNAPSHOT_PATH}/
alert $? ${msg}

