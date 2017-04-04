#!/bin/bash

#filename: unit_tag_import_new.sh
#@auther:  dongying
#@fuction: import unit's newtradeid,beauty_level,vulgar_level,cheat_level to db
#@date:    2014-05-26
#@version: 1.0.0.0 

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="../bin/alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/db.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/unit_tag_import_new.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

#common config
LOG_FILE=${LOG_PATH}/unit_tag_import_new.log
MANUL_FILE=${MANUL_DATA_PATH}/manul.txt

#default file timestamp
DEFAULT_TIME=`date +%Y%m%d`

#is local file flag, 0:is not local 1:local
IS_LOCAL=0
MANUL_FLAG="manul"

function PRINT_LOG()
{
    local logTime=`date +%Y%m%d-%H:%M:%S`
    echo "[${logTime}]$0-$1" >> $LOG_FILE
}

function db_execute()
{
	if [[ -z $1 ]]; then
		PRINT_LOG "[error used] please input db_info like : 'mysql -h hostname -P portnum -u username -p password'"
		return 1
	else
		db_info=$1
	fi

	if [[ -z $2 ]]; then
		PRINT_LOG "[error used] please input operation_sql_file for param 1"
		return 1
	else
		operation_sql_file=$2
	fi
	
	if [[ ! -z $3 ]]; then
		export_file=$3
	fi

	#try to exec clear_sql and operation_sql for times
	cnt=0
	while [[ $cnt -lt $RETRY_TIMES ]]; do
		if [ $# -eq 3 ];then
			$db_info -e "source $operation_sql_file" > $export_file  2>> ${LOG_FILE}
		else
			$db_info -e "source $operation_sql_file"  2>> ${LOG_FILE}
		fi
		if [[ $? -eq 0 ]]; then
			return 0
		fi
		cnt=$(($cnt+1))
		sleep 3
	done
	PRINT_LOG "$0:Execute Query Failed-$2"
	return 1
}

function merge()
{
	if [[ -z $1 ]]; then
		PRINT_LOG "[error used] please input db_query_res_file"
	  return 1
	else
		db_query_res=$1
	fi

	if [[ -z $2 ]]; then
		PRINT_LOG "[error used] please input source_file"
		return 1
	else
		source_file=$2
	fi
	
	if [[ -z $3 ]]; then
		PRINT_LOG "[error used] please input out_file for unit update sql"
		return 1
	else
		unit_update_file=$3
	fi
	
		if [[ -z $4 ]]; then
		PRINT_LOG "[error used] please input out_file for history sql"
		return 1
	else
		audit_history_file=$4
	fi
	
	#merge file from db_query_res and source_file to export_file
	#which unit in both db_query_res and source_file and have the same last_modify_time
	awk -F"\t" -v SQ="'" '{
		if(NR==FNR){
			unitMap[$1]=$2
		}else{
			unitId=$1
		
			modifyTime=$9

			tableId=$13
			newTradeId=$5
			oldTradeId=$11
			beautyLevel=$6
			vulgarLevel=$7
			userId=$2
			groupId=$3
			planId=$4
			auditId=$8
			cheatLevel=$10

			now=strftime("%Y-%m-%d %T")
		
			if(unitId in unitMap){
				if(unitMap[unitId]==modifyTime){
					beautyLevelUpdateStr = (beautyLevel>0)?",m.beauty_level="beautyLevel:""
					vulgarLevelUpdateStr = (vulgarLevel>0)?",m.vulgar_level="vulgarLevel:""
					cheatLevelUpdateStr = (cheatLevel>0)?",m.cheat_level="cheatLevel:""
					
					beautyLevelInsertStr = (beautyLevel>0)?beautyLevel:-1
					vulgarLevelInsertStr = (vulgarLevel>0)?vulgarLevel:-1
					cheatLevelInsertStr = (cheatLevel>0)?cheatLevel:-1
					
					print "update cprounitmater"tableId" m, cprounitstate"tableId" s set m.adtradeid="oldTradeId" ,m.new_adtradeid="newTradeId" "beautyLevelUpdateStr" "vulgarLevelUpdateStr" "cheatLevelUpdateStr" ,m.confidence_level=3, s.helpstatus=s.helpstatus|2  where m.id="unitId" and m.id=s.id and s.chaTime="SQ modifyTime SQ";" > "'"$unit_update_file"'"
					#update tag and trade info to online_unit table
					print "update online_unit m set m.adtradeid="oldTradeId" ,m.new_adtradeid="newTradeId" "beautyLevelUpdateStr" "vulgarLevelUpdateStr" "cheatLevelUpdateStr" ,m.confidence_level=3 where m.id="unitId" and m.modtime="SQ modifyTime SQ";" >> "'"$unit_update_file"'"
					print "insert into unit_tag_history(unitid,userid,groupid,planid,newtradeid,beauty_level,vulgar_level,cheat_level,type,auditid,time) values("unitId","userId","groupId","planId","newTradeId","beautyLevelInsertStr","vulgarLevelInsertStr","cheatLevelInsertStr",1,"auditId","SQ now SQ");" > "'"$audit_history_file"'"
				}
			}
		}
	}'	$db_query_res $source_file 
	
	return 0
}

PRINT_LOG "-------------------BENGIN TO IMPORT UNIT_TAG INFORMATION---------------------"

#param check 
#no param means use yestoday for source
if [ $# -eq 0 ]; then
	REMOTE_FILE_TIME=$DEFAULT_TIME
#1 param support local file and remote file
elif [ $# -eq 1 ]; then
	#local file must user $MANUL_FLAG for param 1
	if [ $1 = ${MANUL_FLAG} ]; then
		if [ ! -f $MANUL_FILE ]; then
			PRINT_LOG "******use manul.txt to load, but manul.txt is not exist!!******"
			exit 1
		else
			PRINT_LOG "******use manul.txt to load******"
			IS_LOCAL=1
		fi
	#remote file use yyyymmdd for date param
	else
		if [[ $1 =~ "^[0-9]{8}$" ]]; then
			REMOTE_FILE_TIME=$1;
		else
    		PRINT_LOG "******illegal param for date, please input yyyymmdd!!******"
    		exit 1
  		fi
	fi
#other means error
else
	PRINT_LOG "******illegal param num!! exit******"
	exit 1
fi


#if file is not local, first need download file
RETRY_NUM=3
DOWN_SUCCESS=0
#need get remote file
if [ $IS_LOCAL -eq 0 ]; then
	SOURCE_FILE=$SOURCE_INPUT_DATA_PATH/source.${REMOTE_FILE_TIME}
	SOURCE_FILE_MD5=$SOURCE_INPUT_DATA_PATH/source.${REMOTE_FILE_TIME}.md5
	
	#down file for data 
	while ((1>0)); do
		CUR_HOUR=`date +%H`
		if [ ${CUR_HOUR} -ge ${KILL_TIME} ]; then
			PRINT_LOG "******download unitTag file for date "$REMOTE_FILE_TIME" failed******"
			alert 1 "download unitTag file for date "$REMOTE_FILE_TIME" failed"
		fi
	
		for (( i=0; i<$RETRY_NUM; i++)){
			wget ${FTP_PATH}/adTag.${REMOTE_FILE_TIME} -nd -nH  --limit-rate=20M -O $SOURCE_FILE
			if [ $? -eq 0 ]; then
				DOWN_SUCCESS=1
				break
			fi
		}
		if [ $DOWN_SUCCESS -eq 1 ]; then
			PRINT_LOG "******download unitTag file for date "$REMOTE_FILE_TIME" success******"
			break
		fi
		
		sleep ${SLEEP_TIME}
	done
	
	#down md5
	DOWN_SUCCESS=0
	for (( i=0; i<$RETRY_NUM; i++)){
		wget ${FTP_PATH}/adTag.${REMOTE_FILE_TIME}.md5 -nd -nH  --limit-rate=20M -O $SOURCE_FILE_MD5
		if [ $? -eq 0 ]; then
			DOWN_SUCCESS=1
			break
		fi
	}
	if [ $DOWN_SUCCESS -eq 1 ]; then
		PRINT_LOG "******download unitTag md5 for date "$REMOTE_FILE_TIME" success******"
	else
		PRINT_LOG "******download unitTag md5 for date "$REMOTE_FILE_TIME" failed******"
		alert 1 "download unitTag md5 for date "$REMOTE_FILE_TIME" fail"
	fi

	#check md5
	md5_local=`md5sum $SOURCE_FILE|awk '{print $1}'`
	md5_remote=`awk '{print $1}' $SOURCE_FILE_MD5`
	if [ $md5_local != $md5_remote ]; then
		alert 1 "check md5 for date "$REMOTE_FILE_TIME" failed"
	fi

else
	SOURCE_FILE=$SOURCE_INPUT_DATA_PATH/source.manul
	cp $MANUL_FILE $SOURCE_FILE
	PRINT_LOG "******copy manul to  "$SOURCE_FILE" success******"
fi

function generate_finish_file() {
	FINISH_FILE=$FINISHED_OUTPUT_DATA_PATH/unit_tag_import_finished.$REMOTE_FILE_TIME
	ms=`date +%s`
	echo -e "$ms" > $FINISH_FILE
}

#file check
#�ļ��������0
#�ļ�����������n
#����ҵid�Ͷ�����ҵid�Ϸ�(�����ǵ��¾���ҵ�ʵ�����ӳ���ϵ��ȷ)
#���Ŷȡ����۶ȡ����׷�ΧΪ0-3����թ��ΧΪ0-2
COL_ERROR_MSG="colum num error"
TRADE_ERROR_MSG="trade id error"
TAG_ERROR_MSG="tag id error"

#first check trade dic file is exist
if [ ! -f $NEWTRADEID_OLDTRADEID_FILE ]; then
	PRINT_LOG "unitTag import need tradeid dic file not exsit "$NEWTRADEID_OLDTRADEID_FILE"!!!!"
	alert 1 "unitTag import need tradeid dic file not exsit "$NEWTRADEID_OLDTRADEID_FILE"!!!!"
fi

#check file num, normally exit if the data is empty
file_num=`wc -l ${SOURCE_FILE}|awk '{print $1}'`
if [ $file_num -lt 1 ]; then
	PRINT_LOG "unitTag import file is empty "$SOURCE_FILE"!!!!"
	generate_finish_file
	exit 0
fi

#check file size
file_size=`ls -l ${SOURCE_FILE}|awk '{print $5}'`
if [ $file_size -gt $REMOTE_FILE_MAX_SIZE ]; then
	PRINT_LOG "unitTag import file is too large "$SOURCE_FILE"!!!!"
	alert 1 "unitTag import file is too large "$SOURCE_FILE"!!!!"
fi
#check 

#check file date column
date_colume_num=`cut -f9 ${SOURCE_FILE} | sort -u | wc -l`
if [ $date_colume_num -eq 1 ]; then
	PRINT_LOG "unitTag date column is 0000-00-00 00:00:00 "$SOURCE_FILE"!!!!"
	alert 1 "unitTag date column is 0000-00-00 00:00:00 "$SOURCE_FILE"!!!!"
fi

#clear tmp dir
rm ${TMP_DATA_PATH}/*

#check content and add oldtradeid,dbid,tableid to pass_file
error_file="${TMP_DATA_PATH}/source.check.error.txt"
pass_file="${TMP_DATA_PATH}/source.check.pass.txt"

awk -F"\t" '{if(NR==FNR){
		tradeMap[$1]=$2
	}else{
		if(NF!='"$REMOTE_FILE_COL_NUM"'){
			print $0"\t'"$COL_ERROR_MSG"'" > "'"$error_file"'"
		}else{
			userId=$2
			newTradeId=$5
			beautyLevel=$6
			vulgarLevel=$7
			cheatLevel=$10
			
			if(newTradeId in tradeMap){
				if((beautyLevel<0||beautyLevel>3)||(vulgarLevel<0||vulgarLevel>3)||(cheatLevel<0||cheatLevel>2)){
					print $0"\t'"$TAG_ERROR_MSG"'" > "'"$error_file"'"
				}else{
					dbid=int(userId/2^6)%8
					tableid=userId%8
					print $0"\t"tradeMap[newTradeId]"\t"dbid"\t"tableid
				}
			}else{
				print $0"\t'"$TRADE_ERROR_MSG"'" > "'"$error_file"'"
			}
		}
	}
		
}' $NEWTRADEID_OLDTRADEID_FILE $SOURCE_FILE > $pass_file

if [ -s $error_file ]; then
	PRINT_LOG "check import file fail "$SOURCE_FILE"!!!!"
	alert 1 "check import file fail "$SOURCE_FILE"!!!!"
fi

PRINT_LOG "******source file check success******"

#file split, now use pass_file for input
cd $SPLIT_INPUT_DATA_PATH 
rm $SPLIT_INPUT_DATA_PATH/*

split -l $SPLIT_FILE_LINE ${pass_file} ${SPLIT_FILE_PREFIX}$DEFAULT_TIME"."
PRINT_LOG "******split file ${pass_file}******"

for split_file in `ls $SPLIT_INPUT_DATA_PATH`
do
	PRINT_LOG "--------Begin to handle [$split_file], split to 8 file base on dbid--------"
	awk -F"\t" '{file="'"$split_file"'."$12;print $0 > file}' $split_file
	for dbid in 0 1 2 3 4 5 6 7
	do
		#db connection config
		mysql_conn_beidou_RD="${MYSQL_CLIENT} -h${DB_HOST_BD_MAID_READ[$dbid]} -P${DB_PORT_BD_MAID_READ[$dbid]} -u${DB_USER_BD_MAID_READ} -p${DB_PASSWORD_BD_MAID_READ} -Dbeidou --default-character-set=utf8 --skip-column-names"
		mysql_conn_beidou="${MYSQL_CLIENT} -h${DB_HOST_BD_MAID[$dbid]} -P${DB_PORT_BD_MAID[$dbid]} -u${DB_USER_BD_MAID} -p${DB_PASSWORD_BD_MAID} -Dbeidou --default-character-set=utf8 --skip-column-names"
		mysql_conn_audit="${MYSQL_CLIENT} -h${BEIDOU_DB_IP_AUDIT} -P${BEIDOU_DB_PORT_AUDIT} -u${BEIDOU_DB_USER_AUDIT} -p${BEIDOU_DB_PASSWORD_AUDIT} -Daudit --default-character-set=utf8 --skip-column-names"
	
		split_dbid_file=$split_file"."$dbid
		split_dbid_query_sql=$ROLLBACK_INPUT_DATA_PATH/${split_dbid_file}.sql
		split_dbid_query_res=$ROLLBACK_INPUT_DATA_PATH/${split_dbid_file}.res
		split_dbid_merge_unit_res=$ROLLBACK_INPUT_DATA_PATH/${split_dbid_file}.merge.unit
		split_dbid_merge_history_res=$ROLLBACK_INPUT_DATA_PATH/${split_dbid_file}.merge.history
		if [ -s $split_dbid_file ]; then

			PRINT_LOG "******	[${split_dbid_file}]	******"
			awk -F"\t" '{
				unitid=$1
				tableid=$13
				userid=$2
				groupid=$3
				planid=$4
				print "select a.id, a.chaTime,a.state,b.adtradeid,b.new_adtradeid,b.beauty_level,b.vulgar_level,b.cheat_level from cprounitstate"tableid" a, cprounitmater"tableid" b where a.id=b.id and a.id="unitid" and a.uid="userid" and a.gid="groupid" and a.pid="planid";" 
				}' $split_dbid_file > $split_dbid_query_sql
				
			PRINT_LOG "******query unit info to [${split_dbid_query_res}]******"
			db_execute "${mysql_conn_beidou_RD}" "${split_dbid_query_sql}" "${split_dbid_query_res}" || alert $? "$0-Error Read Data\
    	              From Sharding No $dbid: ${DB_HOST_BD_MAID_READ[$dbid]}"
			
			if [ ! -s $split_dbid_query_res ]; then
				PRINT_LOG "******$split_dbid_query_res file is empty, continue******"
				continue
			fi

			PRINT_LOG "******merge file [${split_dbid_file}] and [${split_dbid_query_res}]******"
			merge ${split_dbid_query_res} ${split_dbid_file} ${split_dbid_merge_unit_res} ${split_dbid_merge_history_res}

			if [ -s ${split_dbid_merge_unit_res} ]; then
          		PRINT_LOG "******update unit and insert audit log ******"
          		tempUpdatefile=tempUpdatefile.sql
          		tempHistoryfile=tempHistoryfile.sql
				
				sqlNum=`wc -l ${split_dbid_merge_unit_res}|awk '{print $1}'`
				PRINT_LOG "******total unit num is $sqlNum ******"
				if [ $sqlNum -gt 0 ];then
					batchNum=$(($sqlNum/$DB_BATCH_EXE_MAX_NUM))
					for ((i=0;i<$batchNum;i++)); do
						beginLine=$(( $i*$DB_BATCH_EXE_MAX_NUM+1 ))
						endLine=$(( $i*$DB_BATCH_EXE_MAX_NUM+$DB_BATCH_EXE_MAX_NUM ))

						sed -n "${beginLine},${endLine}p" ${split_dbid_merge_unit_res} > $TMP_DATA_PATH/tempUpdatefile.sql
						db_execute "${mysql_conn_beidou}" "${TMP_DATA_PATH}/tempUpdatefile.sql" || alert $? "$0-Error Update Unit Info\
								To Sharding No $dbid: ${DB_HOST_BD_MAID[$dbid]}"
								
						sed -n "${beginLine},${endLine}p" ${split_dbid_merge_history_res} > $TMP_DATA_PATH/tempHistoryfile.sql
						db_execute "${mysql_conn_audit}" "${TMP_DATA_PATH}/tempHistoryfile.sql" || alert $? "$0-Error Insert Audit Log\
								To audit db: ${BEIDOU_DB_IP_AUDIT}"
					done

					endNum=$(( $batchNum*$DB_BATCH_EXE_MAX_NUM ))	
					if [ $endNum -lt $sqlNum ]; then
						beginLine=$(( $batchNum*$DB_BATCH_EXE_MAX_NUM+1 ))
						endLine=$sqlNum
						
						sed -n "${beginLine},${endLine}p" ${split_dbid_merge_unit_res} > $TMP_DATA_PATH/tempUpdatefile.sql
						db_execute "${mysql_conn_beidou}" "${TMP_DATA_PATH}/tempUpdatefile.sql" || alert $? "$0-Error Update Unit Info\
								To Sharding No $dbid: ${DB_HOST_BD_MAID[$dbid]}"
								
						sed -n "${beginLine},${endLine}p" ${split_dbid_merge_history_res} > $TMP_DATA_PATH/tempHistoryfile.sql
						db_execute "${mysql_conn_audit}" "${TMP_DATA_PATH}/tempHistoryfile.sql" || alert $? "$0-Error Insert Audit Log\
								To audit db: ${BEIDOU_DB_IP_AUDIT}"

					fi
				fi
          else
			  PRINT_LOG "******merge file [${split_dbid_merge_unit_res}] is empty, continue******"
		  fi
	  else
		  PRINT_LOG "******split file [${split_dbid_file}] has no data******"
	  fi
  done
done

generate_finish_file

PRINT_LOG "-------------------END TO IMPORT UNIT_TAG INFORMATION---------------------"

