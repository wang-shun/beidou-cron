#!/bin/sh


#@file:importSystemIcon.sh
#@author: tiejing
#@date:2011-08-09
#@version:1.0.0.0
#@brief: import system icon to database(t_table:systemicons) and insert icon information to drmc

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importSystemIcon.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importSystemIcon.sh
reader_list=tiejing


LOG_FILE=${ICON_LOG_PATH}/importSystemIcon.log

mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${ICON_DATA_PATH}
mkdir -p ${ICON_LOG_PATH}


echo ${LOG_PATH}
echo ${DATA_PATH}
echo ${ICON_DATA_PATH}
echo ${ICON_LOG_PATH}

CURR_DATETIME=`date +%F\ %T`
echo "start at "$CURR_DATETIME >> ${LOG_FILE}


msg="��������Ŀ¼${DATA_PATH}ʧ��"
cd ${DATA_PATH}

#�鿴ϵͳͼ��Ŀ¼�Ƿ����
ICON_PATH=${SYSTEM_ICON_PATH}

if [ -d "${ICON_PATH}" ];then
   echo "remove dir ${ICON_PATH}"
   rm -rf ${ICON_PATH}
fi



msg="��������Ŀ¼${ICON_DATA_PATH}ʧ��"
cd ${ICON_DATA_PATH}
alert $? ${msg}

msg="wget�ļ�${ICON_FTP_URL}/${ICON_FILE_NAME}ʧ��"
echo "${ICON_FTP_URL}/${ICON_FILE_NAME}"
wget -q  "${ICON_FTP_URL}/${ICON_FILE_NAME}" -O ${ICON_FILE_NAME}
alert $? ${msg}

msg="wget�ļ�${ICON_FILE_NAME_MD5}ʧ��"
wget  -q ${ICON_FTP_URL}/${ICON_FILE_NAME_MD5} -O ${ICON_FILE_NAME_MD5}
alert $? ${msg}

msg="${ICON_FILE_NAME}�ļ���md5У��ʧ��"
md5sum -c ${ICON_FILE_NAME_MD5}
alert $? ${msg}



#��ѹ�ļ���${DATA_PATH}����Ŀ¼��
unzip ${ICON_FILE_NAME}

msg="����ϵͳͼ��վ�㷢���쳣"



java -Xms512m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.icon.InitIconRepository ${SYSTEM_ICON_IMAGE_PATH} ${SYSTEM_ICON_INFOS_FILE_NAME} >>${LOG_FILE}


if [ "$?" -ne "0" ];then
     echo "`date +"%Y-%m-%d %H:%M:%S"`,import System Icon java  failed">>${LOG_FILE}
     alert 1 "java failed"
     exit 1
fi


CURR_DATETIME=`date +%F\ %T`
echo "end at "$CURR_DATETIME >> ${LOG_FILE}
