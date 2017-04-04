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
# 用 MODULE 变量来指定需要恢复的计算阶段
#
# MODULE="-q ../data/unionsite/input/beidousitestat.20100203"  # 初始数据导入出错
#
# MODULE="-avg"  #计算统计的平均值出错
#
# MODULE="-stat"  #数据导入，计算热度，等级出错


#MODULE="-avg"
MODULE="-b ../data/unionsite/input/beidousitestat.20101213"

CURR_DATETIME=`date +%F\ %T`

echo $CURR_DATETIME >> ${LOG_FILE}

msg="进入工作目录${BIN_PATH}失败"
cd ${BIN_PATH}
alert $? "${msg}"

msg="导入beidou站点发生异常,请使用恢复脚本进行恢复"
java -Xms512m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.SiteRecover ${MODULE} >> ${LOG_FILE}

# if the relt of "java" is wrong then send error message
alert $? "${msg}"

