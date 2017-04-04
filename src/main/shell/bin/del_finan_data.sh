#!/bin/sh
CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

LOG_FILE=${LOG_PATH}/output_shifen.log.${CURR_DATE}

#点击日志文件下载位置
COST_PATH="/home/work/var/beidou_cost/"${CURR_DATE}

#点击日志保存服务器
COST_FTP_SERVER="jx-sf-remora04.jx.baidu.com"

#点击日志保存路径
COST_FTP_PATH="/home/work/lihengyu/20110831_bdrecover/"${CURR_DATE}

#点击日志保存路径
COST_SAVE_PATH="/home/work/var/beidou_cost/"


CUR_TIME=`date +"%Y%m%d%H%M"`
CURR_DATE=`date -d "1 day ago" +"%Y%m%d"`
TODAY_DATE=`date +"%Y%m%d"`
if [ "-$1" != "-" ]; then
  if echo $1 | grep -q '\<[0-9]\{8\}\>'; then
    CURR_DATE=$1;
  else
    echo "日期格式不正确，请检查输入"
    exit 1
  fi
fi


if [ ! -f ${COST_PATH} ]; then
	mkdir -p ${COST_PATH}
fi

cd ${COST_PATH}
rm *

runsql_clk "truncate table beidoufinan.cost_${CURR_DATE};"
