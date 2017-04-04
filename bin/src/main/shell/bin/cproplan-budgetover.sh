#!/bin/sh
#@modify: wangchongjie since 2012.12.10 for cpweb525

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=cproplan_budgetover.sh
reader_list=zhangpingan

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE
if [ $? -ne 0 ]
then
	echo "Conf error: Fail to load libfile[$LIB_FILE]!"
	exit 1
fi 

##########################
##
#config
#
#########################

#log
LOG_NAME=cproplan_budgetover
LOG_LEVEL=8
LOG_SIZE=1800000 

SLEEP_TIME=100
SUB_NUMBER=10

########################

mkdir -p ../data/cproplan_budgetover
cd ../data/cproplan_budgetover

CPROPLANBUDGETOVER_COUNT_SQL="SELECT MAX(planid) FROM beidou.cproplan t where [userid]"

runsql_sharding_read "$CPROPLANBUDGETOVER_COUNT_SQL" maxPlanIds.tmp
CPROPLANBUDGETOVER_COUNT=`cat maxPlanIds.tmp|awk 'BEGIN{max=0}{if($1>max)max=$1}END{print max}'`

FOR_COUNT_SUB=$(( ${CPROPLANBUDGETOVER_COUNT}/${SUB_NUMBER} ))
if [ "${FOR_COUNT_SUB}" -eq "0" ]; then	
	log "NOTICE" "CPROPLANBUDGETOVER_COUNT is 0!"
 	exit 0
fi 

CPROPLANBUDGETOVER_UPDATE_SQL="UPDATE beidou.cproplan t set t.budgetover = 0 WHERE planid <= ${FOR_COUNT_SUB} and [t.userid]"
 
runsql_sharding "$CPROPLANBUDGETOVER_UPDATE_SQL"

if [ $? -ne 0 ]
then
	log "FATAL" "cproplan_budgetover : UPDATE sub number 0 fail"
	SendMail "cproplan_budgetover : UPDATE sub number 0 fail." "${MAILLIST}"
	exit 1
fi 
		
sleep ${SLEEP_TIME} 

INDEX=1
while (( $INDEX <= 8 ))
do
	BEIGIN_INDEX=$(( ${INDEX} * ${FOR_COUNT_SUB} )) 
	END_INDEX=$(( (${INDEX} + 1) * ${FOR_COUNT_SUB} )) 
	
	CPROPLANBUDGETOVER_UPDATE_SQL="UPDATE beidou.cproplan t set t.budgetover = 0 WHERE planid > ${BEIGIN_INDEX} AND planid <= ${END_INDEX} AND [t.userid]"

	runsql_sharding "$CPROPLANBUDGETOVER_UPDATE_SQL"
	
	if [ $? -ne 0 ]
	then
		log "FATAL" "cproplan_budgetover : UPDATE sub number ${INDEX} fail"
		SendMail "cproplan_budgetover : UPDATE sub number ${INDEX} fail." "${MAILLIST}"
		exit 1
	fi  

	sleep ${SLEEP_TIME}
	
	let INDEX=INDEX+1
done

BEIGIN_INDEX=$(( ${INDEX} * ${FOR_COUNT_SUB} ))
CPROPLANBUDGETOVER_UPDATE_SQL="UPDATE beidou.cproplan t set t.budgetover = 0 WHERE planid > ${BEIGIN_INDEX} and [t.userid]" 

runsql_sharding "$CPROPLANBUDGETOVER_UPDATE_SQL"

if [ $? -ne 0 ]
	then
		log "FATAL" "cproplan_budgetover : UPDATE sub number 5 fail"
		SendMail "cproplan_budgetover : UPDATE sub number 5 fail." "${MAILLIST}"
		exit 1
	fi  
