#/bin/bash

#@file: history_operation_backup.sh
#@author: lingbing
#@date: 2010-04-21
#@version: 1.0.0.0
#@brief: backup history operation data 90 days before, and delete them from online database


CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"d

CONF_SH="${CONF_PATH}/opthistory_backup.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${BIN_PATH}/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=opthistory_backup.sh
reader_list=lingbing
DEBUG_MOD=0

function check_path()
{
	if ! [ -w $EXPORT_PATH ]
	then
		if ! [ -e $EXPORT_PATH ]
		then
			mkdir -p $EXPORT_PATH
			if [ $? -ne 0 ]
			then
				log "FATAL" "Fail to mkdir [$EXPORT_PATH]!"
				return 1
			fi
		else
			log "FATAL" "Path[$EXPORT_PATH] is not writable!"
			return 1
		fi
	fi

	return 0	
}


function check_conf()
{
	if ! [[ $PREVIOUS_DAYS ]]
	then
		echo "Conf[PREVIOUS_DAYS] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $MAX_PER_PAGE ]]
	then
		echo "Conf[MAX_PER_PAGE] is empty or its value is invalid"
		return 1
	fi
	
	return 0
}

function ensure_table_exist
{
	workingMonth=$1
		
	#create operationhistory backup table if not exist
	runsql_xdb "use history; create table if not exists operationhistory_bak_$workingMonth (id bigint(20) NOT NULL,userid int(10) NOT NULL,opid bigint(20) NOT NULL,opuser int(10) NOT NULL,opip char(16) NOT NULL, opclient tinyint(3) unsigned NOT NULL, optime datetime NOT NULL, optype int(10) unsigned NOT NULL,oplevel tinyint(3) unsigned NOT NULL,groupid int(10) unsigned NOT NULL,opobjid bigint(20) unsigned NOT NULL,beforeid bigint(20) unsigned NOT NULL,afterid bigint(20) unsigned NOT NULL,PRIMARY KEY (id),KEY OPHISTORY_KEY_USERID (userid),KEY OPHISTORY_KEY_TIME (optime)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;"
	if [ $? -ne 0 ]
	then 
    	log "FATAL" "history_operation_backup.sh : operationhistory backup table create failed" 
    	return -1 
	fi

	#create operationtext backup table if not exist
	runsql_xdb "use history; create table if not exists operationtext_bak_$workingMonth (id bigint(20) NOT NULL, text mediumtext NOT NULL, PRIMARY KEY  (id))ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;"
	if [ $? -ne 0 ]
	then
		log "FATAL" "history_operation_backup.sh : operationtext backup table create failed"
		return -1
	fi

	return 0
}

function backup_subtable
{
	index=$1
	month=$2
	fromDate=$3
	toDate=$4
	
	runsql_xdb_read "use history; select * from operationhistory${index} where optime>='${fromDate}' and optime<'${toDate}'" "${EXPORT_PATH}${OUTPUT_FILE}${index}"
	if [ $? -ne 0 ]
	then
		log "FATAL" "history_operation_baskup.sh : export from table operationhistory$index failed"
		return -1
	fi

	if [ ! -s ${EXPORT_PATH}${OUTPUT_FILE}${index} ]
	then
		log "WARNING" "history_operation_baskup.sh : no record for ${fromDate} in table ${index}"
		return 0
	fi
	
	awk -vmth="$month" -vidx="$index" -vmax="$MAX_PER_PAGE" '
		BEGIN{ORS="";count=1;nvalue="0";}
		{if(NR==2)
		{
			print "insert into history.operationtext_bak_"mth" select * from history.operationtext"idx" where id in (0";
			if($(NF-1)!=nvalue){print ","$(NF-1);}
			if($NF!=nvalue){print ","$NF;}
		}
		if(NR>2)
		{
			if(count==max)
			{
				print ");\n";
				print"insert into history.operationtext_bak_"mth" select * from history.operationtext"idx" where id in (0";
				count=0;
			}
			if($(NF-1)!=nvalue){print ","$(NF-1);}
			if($NF!=nvalue){print ","$NF;}
			count+=1;
		}}
		END{print ")";}
	' ${EXPORT_PATH}${OUTPUT_FILE}${index} > ${EXPORT_PATH}${OUTPUT_FILE}${index}.importoptxt.sql
	if [ $? -ne 0 ]
	then
		log "FATAL" "history_operation_backup.sh : export from table historytext$index failed"
		return -1
	fi

	runsql_xdb "source ${EXPORT_PATH}${OUTPUT_FILE}${index}.importoptxt.sql"
	if [ $? -ne 0 ]
	then
		log "FATAL" "history_operation_backup.sh : import into table historytext_bak_$month at subtable$index failed"
		return -1
	fi

	runsql_xdb "use history; load data local infile '${EXPORT_PATH}${OUTPUT_FILE}${index}' into table operationhistory_bak_$month ignore 1 lines;"
    if [ $? -ne 0 ] 
    then    
        log "FATAL" "history_operation_backup.sh : import into table historyoperation_bak_$month at subtable$index failed" 
        return -1
    fi 

	return 0
}

function clear_subtable
{
	index=$1
	
	if [ ! -s ${EXPORT_PATH}${OUTPUT_FILE}${index} ]
    then
        log "WARNING" "history_operation_baskup.sh : no record to delete in table ${index}"
        return 0
    fi
	
	echo "delete from history.operationhistory$index where id in (" > ${EXPORT_PATH}${OUTPUT_FILE}${index}.clearophis.sql
	awk 'BEGIN{ORS="";}NR==2{print $1;}NR>2{print ","$1;}END{print ")";}' ${EXPORT_PATH}${OUTPUT_FILE}${index} >> ${EXPORT_PATH}${OUTPUT_FILE}${index}.clearophis.sql
	runsql_xdb "source ${EXPORT_PATH}${OUTPUT_FILE}${index}.clearophis.sql"
	if [ $? -ne 0 ]
	then
		log "FATAL" "history_operation_backup.sh : clear table historyoperation$index failed"
		return -1
	fi

	echo "delete from history.operationtext$index where id in (0" > ${EXPORT_PATH}${OUTPUT_FILE}${index}.clearoptxt.sql
	awk 'BEGIN{ORS="";nvalue="0";}NR>1{if($(NF-1)!=nvalue){print ","$(NF-1);}if($NF!=nvalue){print ","$NF;}}END{print ")";}' ${EXPORT_PATH}${OUTPUT_FILE}${index} >> ${EXPORT_PATH}${OUTPUT_FILE}${index}.clearoptxt.sql
	runsql_xdb "source ${EXPORT_PATH}${OUTPUT_FILE}${index}.clearoptxt.sql"
	if [ $? -ne 0 ]
	then
		log "FATAL" "history_operation_backup.sh : clear table historytext$index failed"
		return -1
	fi 

	return 0
}

function backup_alltable
{
	INDEX=0
	while (( INDEX < $1 ))
	do
		backup_subtable $INDEX $2 $3 $4
		if [ $? -ne 0 ]
		then
			return 1
		fi
		let INDEX=INDEX+1
	done
	
	return 0
}

function clear_alltable
{
	INDEX=0
	while (( INDEX < $1 ))
	do
		clear_subtable $INDEX
		if [ $? -ne 0 ]
		then
			return 1
		fi
		let INDEX=INDEX+1
	done

	return 0
}


function history_operation_backup()
{
	log "TRACE" "start check conf"
	check_conf
	if [ $? -ne 0 ]
	then
		exit -1
	fi
	
	log "TRACE" "end check conf & start check path"
	check_path
	if [ $? -ne 0 ]
	then
		exit -1
	fi

	toDate=`date +%Y-%m-%d -d"$PREVIOUS_DAYS days ago"`
	let FROM=$PREVIOUS_DAYS+1
	fromDate=`date +%Y-%m-%d -d"$FROM days ago"`	
	month=`date +%Y%m --date=$fromDate`	

	#create backup table if not exist	
	log "TRACE" "end check path & start check table"
	ensure_table_exist $month
	if [ $? -ne 0 ]
	then
		exit -1
	fi

	#backup history operation data
	log "TRACE" "end check table & start backup table"
	backup_alltable 8 $month $fromDate $toDate
	if [ $? -ne 0 ]
	then
		if [ $? -eq 1 ]
		then
			exit 0
		else
			exit -1
		fi
	fi	

	#delete history operation data
	log "TRACE" "end backup table & start clear table"
	clear_alltable 8
	if [ $? -ne 0 ]
	then
		exit -1
	fi	

	#clear export file & sql
	log "TRACE" "end clear table & start clear file"
	rm ${EXPORT_PATH}*

	log "INFO" "all task done"
	exit 0
}

history_operation_backup
exit $?

