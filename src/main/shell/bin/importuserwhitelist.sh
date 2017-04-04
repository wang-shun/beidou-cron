#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/unionsite.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importuserwhitelist.sh
reader_list=zhuqian

LOG_FILE=${LOG_PATH}/importuserwhitelist.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="cd ${SITE_DATA_PATH} failed"
cd ${SITE_DATA_PATH}
alert $? ${msg}

#source conf file
CONF_SH=${WHITE_USER_FILE}
[ -f "${CONF_SH}" ] && mv $CONF_SH ${WHITE_USER_FILE_BAK}
CONF_SH=${WHITE_USER_FILE_MD5}
[ -f "${CONF_SH}" ] && rm $CONF_SH 

#download MD5 file
msg="wget ${WHITE_USER_FILE} failed"
wget -q  -t$MAX_RETRY ${WHITE_USER_URL}/${WHITE_USER_FILE}
alert $? ${msg}

msg="wget ${WHITE_USER_FILE_MD5} failed"
wget  -q -t$MAX_RETRY ${WHITE_USER_URL}/${WHITE_USER_FILE_MD5}
alert $? ${msg}

msg="check ${WHITE_USER_FILE_MD5} failed"
md5sum -c ${WHITE_USER_FILE_MD5}
alert $? ${msg}

msg="${WHITE_USER_FILE} is empty"
size=`cat ${WHITE_USER_FILE} | wc -w`
if [ ${size} -eq 0 ]; then
    alert 1 ${msg}
fi

msg="cd {BIN_PATH} failed"
cd ${BIN_PATH}
alert $? ${msg}

msg="exception occurred when execute ImportUserWhiteList"
java -Xms512m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprogroup.ImportUserWhiteList ${SITE_DATA_PATH}/${WHITE_USER_FILE}>> ${LOG_FILE}

# if the relt of "java" is wrong then send error message
alert $? ${msg}

