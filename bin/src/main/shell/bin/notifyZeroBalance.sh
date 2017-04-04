#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} " 

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} " 

ZERO_BALANCE_PATH=${DATA_PATH}/notifyZeroBalance

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${ZERO_BALANCE_PATH}


LOG_FILE=${LOG_PATH}/notifyZeroBalance.log 
OUTPUT_FILE=${ZERO_BALANCE_PATH}/notifyZeroBalance.data
USER_FUND_DATA=${ZERO_BALANCE_PATH}/userfundperday.data
ZERO_BALANCE_USER=${ZERO_BALANCE_PATH}/zero_balance_user.data
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=notifyZeroBalance.sh
reader_list=zhuqian

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

function call() {
msg="notify zeroBalance fail"
java -Xms4096m -Xmx8192m -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.SendZeroBalanceRemind &> ${LOG_FILE}
alert $? "${msg}"
return 0;
}

#生成需要通知的用户列表

runsql_xdb_read "select userid from beidouext.userfundperday"  "${USER_FUND_DATA}"

runsql_cap_read "select userid from beidoucap.useraccount ua where ua.balancestat <= 0 and ua.ustate =0 " "${ZERO_BALANCE_USER}"

awk -F'\t' 'ARGIND==1{map[$1]} ARGIND==2{if(!($1 in map)){print $1}}' ${USER_FUND_DATA} ${ZERO_BALANCE_USER} > "${OUTPUT_FILE}"

# 去掉一站式小流量用户
ONE_STATION_FILE=${DATA_PATH}/oneStation.exp
if [ -f "${ONE_STATION_FILE}" ] ; then
	cp ${OUTPUT_FILE} ${OUTPUT_FILE}.bak
	awk -F'\t' 'ARGIND==1{map[$1]} ARGIND==2{if(!($1 in map)){print $1}}' ${ONE_STATION_FILE} ${OUTPUT_FILE} > ${ZERO_BALANCE_PATH}/tmp
	mv ${ZERO_BALANCE_PATH}/tmp ${OUTPUT_FILE}
fi

call
