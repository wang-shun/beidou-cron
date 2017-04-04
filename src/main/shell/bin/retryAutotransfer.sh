#!/bin/sh
# 
# code migration
# @author zhangpingan
# @version 1.0.0

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/retryAutotransfer.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "



mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

program=retryAutotransfer.sh
reader_list=zhangpingan
LOG_FILE=${LOG_PATH}/retryAutotransfer.log

function PRINT_LOG()
{
    timeNow=`date +%Y%m%d-%H:%M:%S`
	echo "[${timeNow}]${1}" >> ${LOG_FILE}
}

    cd ${DATA_PATH}
    #抓取财务中心凤巢余额文件
	msg="从财务中心抓取凤巢余额文件失败"
	retryCount=0
    sucFlag=0
    while [[ $retryCount -lt $MAX_RETRY ]] && [[ $sucFlag -eq 0 ]]
    do
	   retryCount=$(($retryCount+1))
       rm -f ${FC_BALANCE_NAME}
	   wget -t 3 -q --limit-rate=30M ftp://${FC_BALANCE_SERVER}/${FC_BALANCE_PATH}/${FC_BALANCE_NAME}
	   rm -f ${FC_BALANCE_NAME}.md5
	   wget -t 3 -q --limit-rate=30M ftp://${FC_BALANCE_SERVER}/${FC_BALANCE_PATH}/${FC_BALANCE_NAME}.md5
	   md5sum -c ${FC_BALANCE_NAME}.md5
       if [ $? -eq 0 ]
       then
         sucFlag=1
       else
         sleep 5
       fi
    done
	if [ $sucFlag -eq 0 ]
    then
	   PRINT_LOG "${msg}"
       alert 1 "${msg}"
    fi
	
	msg="生成搜索账户用户余额文件失败"
    awk '{printf("%s\t%s\n",$1,$5)}' ${FC_BALANCE_NAME} > ${FC_BALANCE_FILE}
    alert $? "${msg}"

	#从autotransfer表中删除已经不是自动转账的用户
	msg="从db中删除已经非自动转账用户列表失败"
	runsql_xdb "delete from beidouext.autotransfer where userid not in (select userid from beidouext.userfundperday where transfertype=1);"
    alert $? "${msg}"
	
	sleep 10
	
	#从db中查找当前转账失败的有效用户列表
	msg="从db中查找当前转账失败的有效用户列表失败"
	
	runsql_cap_read "select userid from beidoucap.useraccount where ustate=0 and ushifenstatid in (2,3,6)"  ${DATA_PATH}/userid.tmp
	alert $? "从db中查找有效用户列表失败"
	
	runsql_xdb_read "select userid, fund from beidouext.userfundperday where transfertype =1 and userid in (select userid from beidouext.autotransfer where is_success=1)"   ${DATA_PATH}/transfer.tmp
	alert $? "从db中查找用户转账信息失败"
	
	awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($1 in map){print $0}}' ${DATA_PATH}/userid.tmp  ${DATA_PATH}/transfer.tmp > ${USER_RETRY_LIST}.tmp
	
	# 只对一站式KA小流量用户做处理
	#ONE_STATION_FILE=${DATA_PATH}/oneStation.exp
	#if [ -f "${ONE_STATION_FILE}" ] ; then
	#	awk -F'\t' 'ARGIND==1{map[$1]} ARGIND==2{if($1 in map){print $1}}' ${ONE_STATION_FILE} ${USER_RETRY_LIST}.tmp > retryAutotransfer.tmp.exp 
	#	mv  retryAutotransfer.tmp.exp ${USER_RETRY_LIST}.tmp
	#fi
	
	#比较${USER_RETRY_LIST}.tmp和${FC_BALANCE_FILE}，筛选出本次可以进行转账的用户列表
	awk 'ARGIND==1{
	    USER_BALANCE[$1]=$2;
	} ARGIND==2{
	    if(($1 in USER_BALANCE) && (100*USER_BALANCE[$1] > $2)){
		    printf("%s\n",$0);
		}
	}' ${FC_BALANCE_FILE} ${USER_RETRY_LIST}.tmp | sort -k1n -u > ${USER_RETRY_LIST}
	
	
	msg="${USER_RETRY_LIST}文件不存在"
	if [ ! -f ${USER_RETRY_LIST} ]; then 
     PRINT_LOG "${USER_RETRY_LIST}文件不存在"
	 alert 1 "${msg}"
    fi;
	#如果${USER_RETRY_LIST}行数为0，无需跑以下代码
	lines=`cat ${USER_RETRY_LIST} | wc -l`
	PRINT_LOG "User Counts Need To ReAutoTransfer is ${lines}";
	
	if [ ${lines} -eq 0 ]
	then
	  PRINT_LOG "Directly Exit"
	  exit 0
	fi
	
    msg="每日自动转账补充汇款任务失败，请追查"
    java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.RetryAutotransferPerDay  &> ${LOG_FILE}
    alert $? "${msg}"


