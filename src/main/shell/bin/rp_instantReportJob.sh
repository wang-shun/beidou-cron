#!/bin/sh

CONF_SH=./rp_common.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "


program=rp_instantReportJob.sh
reader_list=hejinggen


RP_RPC_FUNC_DESC="远程调用-老report生成单次报表"
RP_RPC_FUNC_URL="${RP_RPC_URL_instantReportJob}"
LOG_NAME="rp_instantReportJob"
LOG_FILE="${LOG_PATH}/${LOG_NAME}.log"


mkdir -p ${LOG_PATH}
open_log
main
statusCode=$?
close_log ${statusCode}
exit ${statusCode}
