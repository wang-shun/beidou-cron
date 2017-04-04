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

program=siterecover.sh
reader_list=zengyunfeng,zhuqian

LOG_FILE=${LOG_PATH}/siterecover.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${BEIDOU_DATA_PATH}

###
# �� MODULE ������ָ����Ҫ�ָ��ļ���׶�
#
# MODULE="-q ../data/unionsite/input/beidousitestat.20100203"  # ��ʼ���ݵ������
#
# MODULE="-avg"  #����ͳ�Ƶ�ƽ��ֵ����
#
# MODULE="-stat"  #���ݵ��룬�����ȶȣ��ȼ�����


#MODULE="-avg"
MODULE="-b ../data/unionsite/input/beidousitestat.20101213"

CURR_DATETIME=`date +%F\ %T`

echo $CURR_DATETIME >> ${LOG_FILE}

msg="���빤��Ŀ¼${BIN_PATH}ʧ��"
cd ${BIN_PATH}
alert $? "${msg}"

msg="����beidouվ�㷢���쳣,��ʹ�ûָ��ű����лָ�"
java -Xms512m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.SiteRecover ${MODULE} >> ${LOG_FILE}

# if the relt of "java" is wrong then send error message
alert $? "${msg}"

