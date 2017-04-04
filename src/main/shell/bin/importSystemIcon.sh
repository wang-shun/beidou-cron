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


msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}

#查看系统图标目录是否存在
ICON_PATH=${SYSTEM_ICON_PATH}

if [ -d "${ICON_PATH}" ];then
   echo "remove dir ${ICON_PATH}"
   rm -rf ${ICON_PATH}
fi



msg="进入数据目录${ICON_DATA_PATH}失败"
cd ${ICON_DATA_PATH}
alert $? ${msg}

msg="wget文件${ICON_FTP_URL}/${ICON_FILE_NAME}失败"
echo "${ICON_FTP_URL}/${ICON_FILE_NAME}"
wget -q  "${ICON_FTP_URL}/${ICON_FILE_NAME}" -O ${ICON_FILE_NAME}
alert $? ${msg}

msg="wget文件${ICON_FILE_NAME_MD5}失败"
wget  -q ${ICON_FTP_URL}/${ICON_FILE_NAME_MD5} -O ${ICON_FILE_NAME_MD5}
alert $? ${msg}

msg="${ICON_FILE_NAME}文件的md5校验失败"
md5sum -c ${ICON_FILE_NAME_MD5}
alert $? ${msg}



#解压文件到${DATA_PATH}所在目录下
unzip ${ICON_FILE_NAME}

msg="导入系统图标站点发生异常"



java -Xms512m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.icon.InitIconRepository ${SYSTEM_ICON_IMAGE_PATH} ${SYSTEM_ICON_INFOS_FILE_NAME} >>${LOG_FILE}


if [ "$?" -ne "0" ];then
     echo "`date +"%Y-%m-%d %H:%M:%S"`,import System Icon java  failed">>${LOG_FILE}
     alert 1 "java failed"
     exit 1
fi


CURR_DATETIME=`date +%F\ %T`
echo "end at "$CURR_DATETIME >> ${LOG_FILE}
