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

program=statFileBinary2Text.sh
reader_list=zhuqian

LOG_FILE=${LOG_PATH}/statFileBinary2Text.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${BEIDOU_DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录${BEIDOU_DATA_PATH}失败"
cd ${BEIDOU_DATA_PATH}
alert $? "${msg}"


for file in `ls daysitestat.[0-6]`; do
    echo "processing file=" $file;

    msg="转换${file}时出错"
    java -Xms1024m -Xmx6144m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.StatFileBinary2Text ${file} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf

    # if the relt of "java" is wrong then send error message
    alert $? "${msg}"

    mv $file $file.bak;
    mv $file.txt $file;

done;