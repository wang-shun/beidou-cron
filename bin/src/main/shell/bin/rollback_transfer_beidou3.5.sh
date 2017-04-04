#!/bin/bash

#@file: rollback_transfer_beidou3.5.sh
#@author: caichao


CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


CONF_SH="./alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


program=transfer.sh
curr_date=`date  "+%Y%m%d"`

LOG_PATH=${LOG_PATH}/rollback_transfer_data
LOG_NAME=rollback
LOG_FILE=${LOG_PATH}/${LOG_NAME}.${curr_date}.log

ROLLBACK_WORK_PATH=${DATA_PATH}/transfer_data_rollback



mkdir -p $ROLLBACK_WORK_PATH
mkdir -p $LOG_PATH

GROUP_DELETE_ROLLBACK=${ROLLBACK_WORK_PATH}/group_delete_rollback
KT_IT_UPDATE_TARGETTYPE_ROLLBACK=${ROLLBACK_WORK_PATH}/kt_it_update_targettype_rollback
IT_RELATION_INSERT_ROLLBACK=${ROLLBACK_WORK_PATH}/it_relation_insert_rollback
RT_RELATION_INSERT_ROLLBACK=${ROLLBACK_WORK_PATH}/people_relation_insert_rollback
IT_RT_UPDATE_TARGETTYPE_ROLLBACK=${ROLLBACK_WORK_PATH}/it_rt_update_targettype_rollback
IT_RELATION_INSERT_KT_ROLLBACK=${ROLLBACK_WORK_PATH}/it_relation_insert_kt_rollback
SINGLE_IT_RELATION_INSERT_ROLLBACK=${ROLLBACK_WORK_PATH}/single_it_relation_insert_rollback
SINGLE_IT_UPDATE_TARGETTYPE_ROLLBACK=${ROLLBACK_WORK_PATH}/single_it_update_targettype_rollback
SINGLE_RT_RELATION_INSERT_ROLLBACK=${ROLLBACK_WORK_PATH}/single_rt_relation_insert_rollback
SINGLE_RT_UPDATE_TARGETTYPE_ROLLBACK=${ROLLBACK_WORK_PATH}/single_rt_update_targettype_rollback
SINGLE_INTEREST_PRICE_INSERT_ROLLBACK=${ROLLBACK_WORK_PATH}/price_it_single_insert_rollback
INTEREST_PRICE_INSERT_KT_ROLLBACK=${ROLLBACK_WORK_PATH}/price_it_kt_insert_rollback
INTEREST_PRICE_INSERT_RT_ROLLBACK=${ROLLBACK_WORK_PATH}/price_it_rt_insert_rollback

ROLLBACK_FINAL=${ROLLBACK_WORK_PATH}/rollback_final

function INF()
{
 echo $1
 echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` "$1 >> $LOG_FILE
}
function ERR()
{
 echo $1
 echo "[ERROR] `date +"%Y-%m-%d %H:%M:%S"` "$1 >> $LOG_FILE
}


function runsql_file_with_transaction()
{
   local sharding_idx=$1
   local file=$2


   sed -i '1 i\begin;' $file 
   echo "commit;" >> $file


   
   if [ -z $sharding_idx ];then
      PRINT_DB_LOG "$0:The 2rd Parameter For Function runsql_single can not be null"
      return 1
   fi
   
   local machine_id=${DB_HOST_BD_MAID[${sharding_idx}]}
   local mysql_conn="${MYSQL_CLIENT} -B -N -h${machine_id} -P${DB_PORT_BD_MAID[$sharding_idx]} -u${DB_USER_BD_MAID} -p${DB_PASSWORD_BD_MAID} -Dbeidoucap --default-character-set=gbk --skip-column-names"
   $mysql_conn < $file
}

function rollbackDatabase()
{
	cat "$GROUP_DELETE_ROLLBACK.$i" "$KT_IT_UPDATE_TARGETTYPE_ROLLBACK.$i" "$IT_RELATION_INSERT_ROLLBACK.$i" "$RT_RELATION_INSERT_ROLLBACK.$i" "$IT_RT_UPDATE_TARGETTYPE_ROLLBACK.$i" "$IT_RELATION_INSERT_KT_ROLLBACK.$i" "$SINGLE_IT_RELATION_INSERT_ROLLBACK.$i" "$SINGLE_IT_UPDATE_TARGETTYPE_ROLLBACK.$i" "$SINGLE_RT_RELATION_INSERT_ROLLBACK.$i" "$SINGLE_RT_UPDATE_TARGETTYPE_ROLLBACK.$i" "$SINGLE_INTEREST_PRICE_INSERT_ROLLBACK.$i" "$INTEREST_PRICE_INSERT_KT_ROLLBACK.$i" "$INTEREST_PRICE_INSERT_RT_ROLLBACK.$i" >$ROLLBACK_FINAL.$i

	runsql_file_with_transaction $i "$ROLLBACK_FINAL.$i"

	if [ $? -eq 0 ] ;then
		echo -e "rollback database $i finish" >> $LOG_FILE
	fi
}
INF "begin task..."
for ((i=0;i<8;i++))
do
	rollbackDatabase
	echo $i
done
INF "finished"
