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

program=generateTradeSiteXml.sh
reader_list=zhuqian

LOG_FILE=${LOG_PATH}/generateTradeSiteXml.log

TOP_N=100

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_OUTPUT_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="进入工作目录${BIN_PATH}失败"
cd ${BIN_PATH}
alert $? "${msg}"

msg="导入beidou站点Q值发生异常,请使用恢复脚本进行恢复"
java -Xms1024m -Xmx6144m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.TopSitesByTrade ${TOP_N} ${TRADE_SITE_XML_FILEPATH} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf


