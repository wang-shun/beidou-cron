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

msg="��������Ŀ¼${CRON_SNAPSHOT_PATH}/${YESTERDAY_YYYYMMDD}ʧ��"
cd ${CRON_SNAPSHOT_PATH}/${YESTERDAY_YYYYMMDD}
alert $? ${msg}


#ץȡ�ļ�
msg="wget�ļ������ͼ�ļ�ʧ��"
#wget -q  ${SNAPSHOT_RESULT_URL}/${SNAPSHOT_FILE}
wget -ndpr ${SNAPSHOT_IMAGE_URL}/${YESTERDAY_YYYYMMDD}/
#echo ${SNAPSHOT_IMAGE_URL}/${YESTERDAY_YYYYMMDD}/
alert $? ${msg}

cd ..
msg="����ͼƬ��${aimgr00}ʧ��"
rsync -auv -e ssh ${YESTERDAY_YYYYMMDD}/ ${aimgr00}:${WM123_SNAPSHOT_PATH}/
alert $? ${msg}
msg="����ͼƬ��${aimgr01}ʧ��"
rsync -auv -e ssh ${YESTERDAY_YYYYMMDD}/ ${aimgr01}:${WM123_SNAPSHOT_PATH}/
alert $? ${msg}
msg="����ͼƬ��${tcmgr00}ʧ��"
rsync -auv -e ssh ${YESTERDAY_YYYYMMDD}/ ${tcmgr00}:${WM123_SNAPSHOT_PATH}/
alert $? ${msg}
msg="����ͼƬ��${tcmgr01}ʧ��"
rsync -auv -e ssh ${YESTERDAY_YYYYMMDD}/ ${tcmgr01}:${WM123_SNAPSHOT_PATH}/
alert $? ${msg}

