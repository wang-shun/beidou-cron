#!/bin/sh
#database router for shell
#@author zhangpingan
#@date 2012-12-01
#############################################################################################################################
#funtion list as follows:
#1.runsql_clk--------------------------------------------execute sql on clk db
#2.runsql_clk_read---------------------------------------read data from clk db

#3.runsql_xdb--------------------------------------------execute sql on xdb
#4.runsql_xdb_read---------------------------------------read data from xdb
#4-2.runsql_xdb_master_read------------------------------read data from xdb

#5.runsql_cap--------------------------------------------execute sql on cap db
#6.runsql_cap_read---------------------------------------read data from on cap db

#7.runsql_sharding_read----------------------------------execute sql on all shardings
#8.runsql_sharding---------------------------------------read data from all shardings

#9.runsql_single_read------------------------------------execute sql on single sharding
#10.runsql_single----------------------------------------read data from single sharding

#11.runfilesql_sharding_read-----------------------------read data from all shardings
#12.runfilesql_sharding----------------------------------execute sql file on all shardings

#13.runfilesql_one_sharding------------------------------execute sql file on one single sharding

#14.runsql_audit_read------------------------------------read data from audit db
#15.runsql_audit-----------------------------------------write daba to audit db
#16.runsql_user_audit_read-------------------------------read data from one_adx db
#############################################################################################################################

CONF_SH=../conf/db.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../bin/alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

if [ -z $SHARDING_SLICE ];then
SHARDING_SLICE=8
fi

#here is table slice configuration in beidou
TAB_UNIT_SLICE=8
TAB_UNIT_AUDIT_SLICE=8
TAB_RT_SLICE=8
TAB_VT_SLICE=8
TAB_PRE_MATER_SLICE=8
TAB_KEYWORD_SLICE=64
TAB_WORD_PACK_KEYWORD_SLICE=8

#Gloabal Settings
ROUTING_REPLACE_LEFT='['
ROUTING_REPLACE_RIGHT=']'
ROUTING_RULE_LEFT="(("
ROUTING_RULE_RIGHT=">>6)%${SHARDING_SLICE})=#"
DB_REPLACER='\#'
TABLE_REPLACER='\?'


function getUserSlice()
{
    if [ -z $1  ];then
        exit 1
    fi

    local slice;
    slice=$((($1>>6)%${SHARDING_SLICE}))
    echo $slice
}


#Print Log to /home/work/beidou-cron/log/db.log
function PRINT_DB_LOG()
{
    timeNow=`date +%Y%m%d-%H:%M:%S`
    echo "[${timeNow}]${0}-${1}" >> ${DB_LOG}
}

#############################################################################################################################
##author          zhangpingan
#$1:db_info        please input db_info like : 'mysql -h hostname -P portnum -u username -p password'
#$2:operation_sql  operation_sql should be simple sentense 
#                  if you have serveral sql operation, split them into several singel sql
#$3:export_file    if this is a export db operation, you must set this param to save the dbdata, recommand use all path way 
#############################################################################################################################
function db_execute(){
#init parms
if [[ -z $1 ]]; then
    PRINT_DB_LOG "[error used] please input db_info like : 'mysql -h hostname -P portnum -u username -p password'"
    return 1
else    
    db_info=$1
fi

if [[ -z $2 ]]; then
    PRINT_DB_LOG "[error used] please input operation_sql for param 1"
    return 1
else    
    operation_sql=$2
fi

if [[ ! -z $3 ]]; then
    export_file=$3
fi

#try to exec clear_sql and operation_sql for times
cnt=0   
while [[ $cnt -lt $RETRY_TIMES ]]; do
        if [[ ! -z $export_file ]];then
        $db_info -e "$operation_sql" > $export_file  2>> ${DB_LOG}
		else
		$db_info -e "$operation_sql"  2>> ${DB_LOG}
		fi
        if [[ $? -eq 0 ]]; then
            return 0
        fi          
		cnt=$(($cnt+1))
		sleep 5 
done    
PRINT_DB_LOG "$0:Execute Query Failed-$2"
return 1
}
#############################################################################################################################


#############################################################################################################################
##author          zhangpingan
#$1:db_info        please input db_info like : 'mysql -h hostname -P portnum -u username -p password'
#$2:operation_sql  operation_sql should be simple sentense 
#                  if you have serveral sql operation, split them into several singel sql
#$3:export_file    if this is a export db operation, you must set this param to save the dbdata, recommand use all path way 
#$4:sql_file_suffix
#############################################################################################################################
function db_file_execute(){
#init parms
if [[ -z "${1}" ]]; then
    PRINT_DB_LOG "[error used] please input db_info like : 'mysql -h hostname -P portnum -u username -p password'"
    return 1
else    
    db_info=$1
fi

if [[ -z "$2" ]]; then
    PRINT_DB_LOG "[error used] please input operation_sql for param 1"
    return 1
else    
    operation_sql=$2
fi

if [[ ! -z "$3" ]]; then
    export_file=$3
fi


if [[ ! -z "$4" ]]; then
    file_suffix=$4
else
	file_suffix=$RANDOM
fi

local tmp_sql="/home/work/beidou-cron/log/"${0}"_db_execute_"${file_suffix}".sql"

#try to exec clear_sql and operation_sql for times
cnt=0   
while [[ $cnt -lt $RETRY_TIMES ]]; do
        echo "${operation_sql}" > ${tmp_sql}
        if [[ ! -z $export_file ]];then
        $db_info < "${tmp_sql}" > $export_file  2>> ${DB_LOG}
		
		else
		$db_info < "${tmp_sql}"  2>> ${DB_LOG}
		fi
        if [[ $? -eq 0 ]]; then
			rm -f ${tmp_sql}
            return 0
        fi          
		cnt=$(($cnt+1))
		sleep 5 
done    
PRINT_DB_LOG "$0:Execute Query Failed-$2"
return 1
}


function db_file_execute_with_cat(){
#init parms
if [[ -z "${1}" ]]; then
    PRINT_DB_LOG "[error used] please input db_info like : 'mysql -h hostname -P portnum -u username -p password'"
    return 1
else    
    db_info=$1
fi

if [[ ! -f "$2" ]]; then
    PRINT_DB_LOG "[error used] please input operation_sql file for param 1"
    return 1
else    
    operation_sql=$2
fi

if [[ ! -z "$3" ]]; then
    export_file=$3
fi


if [[ ! -z "$4" ]]; then
    file_suffix=$4
else
	file_suffix=$RANDOM
fi

local tmp_sql="/home/work/beidou-cron/log/"${0}"_db_execute_"${file_suffix}".sql"

#try to exec clear_sql and operation_sql for times
cnt=0   
while [[ $cnt -lt $RETRY_TIMES ]]; do
        cat ${operation_sql} > ${tmp_sql}
        if [[ ! -z $export_file ]];then
        $db_info < "${tmp_sql}" > $export_file  2>> ${DB_LOG}
		
		else
		$db_info < "${tmp_sql}"  2>> ${DB_LOG}
		fi
        if [[ $? -eq 0 ]]; then
			rm -f ${tmp_sql}
            return 0
        fi          
		cnt=$(($cnt+1))
		sleep 5 
done    
PRINT_DB_LOG "$0:Execute Query Failed-$2"
return 1
}
#############################################################################################################################




#############################################################################################################################
#write daba to clk db
#$1:insert/update sql to execute------notice that the statement with dbname.XXX is required 
function runsql_clk()
{
	local sql=$1
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_CLK} -P${BEIDOU_DB_PORT_CLK} -u${BEIDOU_DB_USER_CLK} -p${BEIDOU_DB_PASSWORD_CLK} -Dbeidoufinan"
	db_execute "${mysql_conn}" "${sql}" "/dev/null"
}


#read data from clk db
#$1:select sql to query------notice that the statement with dbname.XXX is required 
#$2:dump file name------notice that the full path is recommended
function runsql_clk_read()
{
	local sql=$1
    local dumpfile=$2
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_CLK_READ} -P${BEIDOU_DB_PORT_CLK_READ} -u${BEIDOU_DB_USER_CLK_READ} -p${BEIDOU_DB_PASSWORD_CLK_READ} -Dbeidoufinan --default-character-set=gbk --skip-column-names"
	db_execute "${mysql_conn}" "${sql}" "${dumpfile}"
}
#############################################################################################################################





#############################################################################################################################
#write data to xdb
#$1:insert/update sql to execute------notice that the statement with dbname.XXX is required
function runsql_xdb()
{
	local sql=$1
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_XDB} -P${BEIDOU_DB_PORT_XDB} -u${BEIDOU_DB_USER_XDB} -p${BEIDOU_DB_PASSWORD_XDB} -Dbeidouext --default-character-set=gbk"
	db_execute "${mysql_conn}" "${sql}" "/dev/null"
}

#read data from master xdb
#add by wangchongjie for generate sequenceid
#$1:select sql to query master db------notice that the statement with dbname.XXX is required
#$2:dump file name------notice that the full path is recommended
function runsql_xdb_master_read()
{
	local sql=$1
	local dumpfile=$2
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_XDB} -P${BEIDOU_DB_PORT_XDB} -u${BEIDOU_DB_USER_XDB} -p${BEIDOU_DB_PASSWORD_XDB} -Dbeidouext --default-character-set=gbk"
	db_execute "${mysql_conn}" "${sql}" "${dumpfile}"
}

#read data from xdb db
#$1:select sql to query------notice that the statement with dbname.XXX is required 
#$2:dump file name------notice that the full path is recommended
#3:if enforce read on master db------1:true;2:false 
function runsql_xdb_read()
{
	local sql=$1
    local dumpfile=$2
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_XDB_READ} -P${BEIDOU_DB_PORT_XDB_READ} -u${BEIDOU_DB_USER_XDB_READ} -p${BEIDOU_DB_PASSWORD_XDB_READ} -Dbeidouext --default-character-set=gbk --skip-column-names"
	if [[ 1==$3 ]];then
		mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_XDB} -P${BEIDOU_DB_PORT_XDB} -u${BEIDOU_DB_USER_XDB} -p${BEIDOU_DB_PASSWORD_XDB} -Dbeidouext --default-character-set=gbk"
	fi
	db_execute "${mysql_conn}" "${sql}" "${dumpfile}"
}
#############################################################################################################################



#############################################################################################################################
#write daba to re db
#$1:insert/update sql to execute------notice that the statement with dbname.XXX is required 
function runsql_re()
{
	local sql=$1
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_RE} -P${BEIDOU_DB_PORT_RE} -u${BEIDOU_DB_USER_RE} -p${BEIDOU_DB_PASSWORD_RE} -Dbeidoure"
	db_execute "${mysql_conn}" "${sql}" "/dev/null"
}


#read data from re db
#$1:select sql to query------notice that the statement with dbname.XXX is required 
#$2:dump file name------notice that the full path is recommended
function runsql_re_read()
{
	local sql=$1
    local dumpfile=$2
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_RE} -P${BEIDOU_DB_PORT_RE} -u${BEIDOU_DB_USER_RE} -p${BEIDOU_DB_PASSWORD_RE} -Dbeidoure --default-character-set=gbk --skip-column-names"
	db_execute "${mysql_conn}" "${sql}" "${dumpfile}"
}
#############################################################################################################################



#write data on stat db
#$1:select sql to query------notice that the statement with dbname.XXX is required 
#$2: which stat db used ,defautl is number 2
function runsql_stat()
{
	local sql=$1
	local number=$2
	if [ -z $number ];then
		number=2
	fi
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_STAT_READ[$number]} -P${BEIDOU_DB_PORT_STAT_READ[$number]} -u${BEIDOU_DB_USER_STAT_READ} -p${BEIDOU_DB_PASSWORD_STAT_READ} -Dbeidoustat --default-character-set=utf8 --skip-column-names"
	db_execute "${mysql_conn}" "${sql}" "/dev/null"
}
#############################################################################################################################


#read data from stat db
#$1:select sql to query------notice that the statement with dbname.XXX is required 
#$2:dump file name------notice that the full path is recommended
#$3: which stat db used ,defautl is number 2
function runsql_stat_read()
{
	local sql=$1
    local dumpfile=$2
	local number=$3
	if [ -z $number ];then
		number=2
	fi
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_STAT_READ[$number]} -P${BEIDOU_DB_PORT_STAT_READ[$number]} -u${BEIDOU_DB_USER_STAT_READ} -p${BEIDOU_DB_PASSWORD_STAT_READ} -Dbeidoustat --default-character-set=utf8 --skip-column-names"
	db_execute "${mysql_conn}" "${sql}" "${dumpfile}"
}
#############################################################################################################################



#############################################################################################################################
#write data to cap db
#$1:insert/update sql to execute------notice that the statement with dbname.XXX is required
function runsql_cap()
{
	local sql=$1
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_CAP} -P${BEIDOU_DB_PORT_CAP} -u${BEIDOU_DB_USER_CAP} -p${BEIDOU_DB_PASSWORD_CAP} -Dbeidoucap --default-character-set=gbk"
	db_execute "${mysql_conn}" "${sql}" "/dev/null"
}
#############################################################################################################################




#############################################################################################################################
#read data from single sharding db, can also be userd to get cap data
#$1:select sql to query------notice that the statement with dbname.XXX is required 
#$2:dump file name------notice that the full path is recommended
function runsql_random_read()
{
   local sql=$1
   local dumpfile=$2
   local sharding_idx=`expr $RANDOM % ${SHARDING_SLICE}`
   local machind=${DB_HOST_BD_MAID_READ[${sharding_idx}]}
   local mysql_conn="${MYSQL_CLIENT} -B -N -h${machind} -P${DB_PORT_BD_MAID_READ[$sharding_idx]} -u${DB_USER_BD_MAID_READ} -p${DB_PASSWORD_BD_MAID_READ} -Dbeidoucap --default-character-set=gbk --skip-column-names"
   db_execute "${mysql_conn}" "${sql}" "${dumpfile}"
}

#############################################################################################################################
#read data from single sharding db, can also be userd to get cap data
#$1:select sql to query------notice that the statement with dbname.XXX is required 
#$2:dump file name------notice that the full path is recommended
function runsql_cap_read()
{
   local sql=$1
   local dumpfile=$2
   local sharding_idx=`expr $RANDOM % ${SHARDING_SLICE}`
   local machind=${DB_HOST_BD_MAID_READ[${sharding_idx}]}
   local mysql_conn="${MYSQL_CLIENT} -B -N -h${machind} -P${DB_PORT_BD_MAID_READ[$sharding_idx]} -u${DB_USER_BD_MAID_READ} -p${DB_PASSWORD_BD_MAID_READ} -Dbeidoucap --default-character-set=gbk --skip-column-names"
   db_execute "${mysql_conn}" "${sql}" "${dumpfile}"
}



#############################################################################################################################
#read data from single sharding db, can also be userd to get cap data
#$1:select sql to query------notice that the statement with dbname.XXX is required 
#$2:dump file name------notice that the full path is recommended
#$3:the assiged sharding slice if required
function runsql_single_read()
{
   local sql=$1
   local dumpfile=$2
   
   #replace sharding placeholder '[]' if exists in sql statement 
   sql=${sql//${ROUTING_REPLACE_LEFT}/${ROUTING_RULE_LEFT}}
   sql=${sql//${ROUTING_REPLACE_RIGHT}/${ROUTING_RULE_RIGHT}}
   
   
   
   local sharding_idx=$3
   
   if [ -z $sharding_idx ];then
      sharding_idx=`expr $RANDOM % ${SHARDING_SLICE}`
   fi
   
   tmp_sql=${sql}
   
   #replace db placeholder '#' if exists in sql statement 
   tmp_sql=${tmp_sql//${DB_REPLACER}/${sharding_idx}}
   
   local machine_id=${DB_HOST_BD_MAID_READ[${sharding_idx}]}
   local mysql_conn="${MYSQL_CLIENT} -B -N -h${machine_id} -P${DB_PORT_BD_MAID_READ[$sharding_idx]} -u${DB_USER_BD_MAID_READ} -p${DB_PASSWORD_BD_MAID_READ} -Dbeidoucap --default-character-set=gbk --skip-column-names"
   db_execute "${mysql_conn}" "${tmp_sql}" "${dumpfile}"
}




#############################################################################################################################
#write data to a single sharding db
#$1:insert/update sql to execute------notice that the statement with dbname.XXX is required 
#$2:the assiged sharding slice, cannot be null
function runsql_single()
{
   local sql=$1
   
   #replace sharding placeholder '[]' if exists in sql statement 
   sql=${sql//${ROUTING_REPLACE_LEFT}/${ROUTING_RULE_LEFT}}
   sql=${sql//${ROUTING_REPLACE_RIGHT}/${ROUTING_RULE_RIGHT}}
   
   
   
   local sharding_idx=$2
   
   if [ -z $sharding_idx ];then
      PRINT_DB_LOG "$0:The 2rd Parameter For Function runsql_single can not be null"
      return 1
   fi
   
   tmp_sql=${sql}
   
   #replace db placeholder '#' if exists in sql statement 
   tmp_sql=${tmp_sql//${DB_REPLACER}/${sharding_idx}}
   
   local machine_id=${DB_HOST_BD_MAID[${sharding_idx}]}
   local mysql_conn="${MYSQL_CLIENT} -B -N -h${machine_id} -P${DB_PORT_BD_MAID[$sharding_idx]} -u${DB_USER_BD_MAID} -p${DB_PASSWORD_BD_MAID} -Dbeidoucap --default-character-set=gbk --skip-column-names"
   db_execute "${mysql_conn}" "${tmp_sql}" "${dumpfile}" "/dev/null"
}

#############################################################################################################################
#write data to a single sharding db
#$1:insert/update/select sql to execute------notice that the statement with dbname.XXX is required 
#$2:the assiged sharding slice, cannot be null
#$3:the file need to save data, cannot be null
function runsql_single_file()
{
   local sql=$1
   
   #replace sharding placeholder '[]' if exists in sql statement 
   #sql=${sql//${ROUTING_REPLACE_LEFT}/${ROUTING_RULE_LEFT}}
   #sql=${sql//${ROUTING_REPLACE_RIGHT}/${ROUTING_RULE_RIGHT}}
   
   
   
   local sharding_idx=$2
   local dumpfile=$3
   if [ -z $sharding_idx ];then
      PRINT_DB_LOG "$0:The 2rd Parameter For Function runsql_single can not be null"
      return 1
   fi
   
   tmp_sql=${sql}
   
   #replace db placeholder '#' if exists in sql statement 
   #tmp_sql=${tmp_sql//${DB_REPLACER}/${sharding_idx}}
   
   local machine_id=${DB_HOST_BD_MAID[${sharding_idx}]}
   local mysql_conn="${MYSQL_CLIENT} -B -N -h${machine_id} -P${DB_PORT_BD_MAID[$sharding_idx]} -u${DB_USER_BD_MAID} -p${DB_PASSWORD_BD_MAID} -Dbeidoucap --default-character-set=gbk --skip-column-names"
   db_execute "${mysql_conn}" "${tmp_sql}" "${dumpfile}" "/dev/null"
}



#############################################################################################################################
#read data from all sharding dbs
#$1:select sql to query------notice that the statement with dbname.XXX is required
#to fileter duplicated data, the statement should be used like "select planid from beidou.planid where [userid]"
#$2:dump file name------notice that the full path is recommended
#$3:number of table counts in sql if the table is sharded
function runsql_sharding_read()
{
	if [ $# -lt 2 ]
	then 
		PRINT_DB_LOG "args isless than 2"
		return 1
	fi
	
	local sql=$1
	local dumpfile=$2
	local table_count
	local suffix
	
	if [[ ! -z $3 ]]; then
		table_count=$3
	else
		table_count=1
	fi
	
	rm -f ${dumpfile}
	local array
	local tmp_sql
	local curr_sql
	local db_count=${SHARDING_SLICE}
	

	#replace sharding placeholder '[]' if exists in sql statement 
	sql=${sql//${ROUTING_REPLACE_LEFT}/${ROUTING_RULE_LEFT}}
	sql=${sql//${ROUTING_REPLACE_RIGHT}/${ROUTING_RULE_RIGHT}}
	
	for((db_idx=0;db_idx<db_count;db_idx++))
	do
	    tmp_sql=${sql}
		
		#replace db placeholder '#' if exists in sql statement 
		tmp_sql=${tmp_sql//${DB_REPLACER}/${db_idx}}
		for((idx=0;idx<table_count;idx++))
    	do
        	#replace table placeholder '?' if exists in sql statement 
        	curr_sql=${tmp_sql//${TABLE_REPLACER}/${idx}}
			if [ $idx -eq 0 ];then 
				array[$db_idx]="${curr_sql}"
			else
				array[$db_idx]="${array[$db_idx]};${curr_sql}"
			fi
    	done
  	done
 
    suffix=`expr $RANDOM \* $RANDOM`
    for((idx=0;idx<db_count;idx++))
    do
		local mysql_conn="${MYSQL_CLIENT} -B -N -h${DB_HOST_BD_MAID_READ[$idx]} -P${DB_PORT_BD_MAID_READ[$idx]} -u${DB_USER_BD_MAID_READ} -p${DB_PASSWORD_BD_MAID_READ} -Dbeidou --default-character-set=gbk --skip-column-names"
		db_execute "${mysql_conn}" "${array[$idx]}" ${dumpfile}_${suffix}.${idx} || alert $? "$0-Error Read Data From Sharding No $idx:${DB_HOST_BD_MAID_READ[$idx]}"  &
    done

    wait
		
    for((idx=0;idx<db_count;idx++))
    do
        cat ${dumpfile}_${suffix}.${idx} >> ${dumpfile}
        rm ${dumpfile}_${suffix}.${idx}
    done
}


function runsql_sharding()
#run sql on all sharding dbs
#$1:insert/update sql to execute-----notice that the statement with dbname.XXX is required
#$2:number of table counts in sql if the table is sharded
{
	if [ $# -lt 1 ]
	then 
		PRINT_DB_LOG "args isless than 1"
		exit 1
	fi
	
	local sql=$1
	local table_count
	if [[ ! -z $2 ]]; then
		table_count=$2
	else
		table_count=1
	fi
	
	local array
	local tmp_sql
	local curr_sql
	local db_count=${SHARDING_SLICE}
	
	#replace sharding placeholder '[]' if exists in sql statement 
	sql=${sql//${ROUTING_REPLACE_LEFT}/${ROUTING_RULE_LEFT}}
	sql=${sql//${ROUTING_REPLACE_RIGHT}/${ROUTING_RULE_RIGHT}}
	

	for((db_idx=0;db_idx<db_count;db_idx++))
	do
	    tmp_sql=${sql}
	    #replace db placeholder '#' if exists in sql statement 
		tmp_sql=${tmp_sql//${DB_REPLACER}/${db_idx}}
		for((idx=0;idx<table_count;idx++))
    	do
			#replace table placeholder '?' if exists in sql statement 
        	curr_sql=${tmp_sql//${TABLE_REPLACER}/${idx}}
			if [ $idx -eq 0 ];then 
				array[$db_idx]="${curr_sql}"
			else
				array[$db_idx]="${array[$db_idx]};${curr_sql}"
			fi
    	done
  	done

    for((idx=0;idx<db_count;idx++))
    do
		local mysql_conn="${MYSQL_CLIENT} -B -N -h${DB_HOST_BD_MAID[$idx]} -P${DB_PORT_BD_MAID[$idx]} -u${DB_USER_BD_MAID} -p${DB_PASSWORD_BD_MAID} -Dbeidou --default-character-set=gbk --skip-column-names"
		db_execute "${mysql_conn}" "${array[$idx]}" "/dev/null" || alert $? "$0-Error Write Data From Sharding No $idx:${DB_HOST_BD_MAID[$idx]}"  &
    done

    wait
	return 0
}
#############################################################################################################################









#11.runfilesql_sharding_read-----execute sql file on all shardings
#12.runfilesql_sharding----------read data from all shardings

#############################################################################################################################
#read data from all sharding dbs
#$1:sqlfile to query------notice that the statement with dbname.XXX is required,and the path should bd absolutely
#to fileter duplicated data, the statement should be used like "select planid from beidou.planid where [userid]"
#$2:dump file name------notice that the full path is recommended
#$3:number of table counts in sql if the table is sharded
function runfilesql_sharding_read()
{
	if [ $# -lt 2 ]
	then 
		PRINT_DB_LOG "args isless than 2"
		return 1
	fi
	
	if [[ ! -f $1 ]]; then
		PRINT_DB_LOG "SQL File $1 IS Empty"
		return 1
	fi
	
	local sql=`cat $1`
	
	local dumpfile=$2
	local table_count
	local suffix
	
	if [[ ! -z $3 ]]; then
		table_count=$3
	else
		table_count=1
	fi
	
	rm -f ${dumpfile}
	local array
	local tmp_sql
	local curr_sql
	local db_count=${SHARDING_SLICE}
	
	local ROUTING_REPLACE_LEFT="\["
	local ROUTING_REPLACE_RIGHT="\]"
	local ROUTING_RULE_LEFT="(("
	local ROUTING_RULE_RIGHT=">>6)%8)=#"
	local TMP_SQL_FILE_PATH="/home/work/beidou-cron/log/"

	local threadname=$0"_"${RANDOM}"_"`date +%s`

	#replace sharding placeholder '[]' if exists in sql statement 
	sed   "s/${ROUTING_REPLACE_LEFT}/${ROUTING_RULE_LEFT}/g" $1 > ${TMP_SQL_FILE_PATH}/${threadname}"_left.data"
	sed   "s/${ROUTING_REPLACE_RIGHT}/${ROUTING_RULE_RIGHT}/g"  ${TMP_SQL_FILE_PATH}/${threadname}"_left.data" >${TMP_SQL_FILE_PATH}/${threadname}"_right.data"
	
	for((db_idx=0;db_idx<db_count;db_idx++))
	do
	    #tmp_sql=${sql}
		
		#replace db placeholder '#' if exists in sql statement 
		#tmp_sql=${tmp_sql//${DB_REPLACER}/${db_idx}}
		sed   "s/#/${db_idx}/g"  ${TMP_SQL_FILE_PATH}/${threadname}"_right.data"  > ${TMP_SQL_FILE_PATH}/${threadname}"_${db_idx}_db.data"
		for((idx=0;idx<table_count;idx++))
    	do
        	#replace table placeholder '?' if exists in sql statement 
        	#curr_sql=${tmp_sql//${TABLE_REPLACER}/${idx}}
			sed   "s/?/${idx}/g"  ${TMP_SQL_FILE_PATH}/${threadname}"_${db_idx}_db.data" > ${TMP_SQL_FILE_PATH}/${threadname}"_${db_idx}_tb${idx}.data"
        	curr_sql=`cat ${TMP_SQL_FILE_PATH}/${threadname}"_"${db_idx}"_tb"${idx}".data"`
			if [ $idx -eq 0 ];then 
				array[$db_idx]="${curr_sql}"
			else
				array[$db_idx]="${array[$db_idx]};${curr_sql}"
			fi
    	done
  	done
 
    suffix=`expr $RANDOM \* $RANDOM`_`date +%s`
    for((idx=0;idx<db_count;idx++))
    do
	    sleep 5
		local mysql_conn="${MYSQL_CLIENT} -B -N -h${DB_HOST_BD_MAID_READ[$idx]} -P${DB_PORT_BD_MAID_READ[$idx]} -u${DB_USER_BD_MAID_READ} -p${DB_PASSWORD_BD_MAID_READ} -Dbeidou --default-character-set=gbk --skip-column-names"
		db_file_execute "${mysql_conn}" "${array[$idx]}" ${dumpfile}_${suffix}.${idx} ${idx}_`date +%s` || alert $? "$0-Error Read Data From Sharding No $idx:${DB_HOST_BD_MAID_READ[$idx]}"  &
    done

    wait
		
    for((idx=0;idx<db_count;idx++))
    do
        cat ${dumpfile}_${suffix}.${idx} >> ${dumpfile}
        rm ${dumpfile}_${suffix}.${idx}
    done
	rm -f ${TMP_SQL_FILE_PATH}/${threadname}*.data
}


function runfilesql_sharding()
#run sql on all sharding dbs
#$1:insert/update sql to execute-----notice that the statement with dbname.XXX is required
#$2:number of table counts in sql if the table is sharded
{
    
	
	if [ $# -lt 1 ]
	then 
		PRINT_DB_LOG "args isless than 1"
		exit 1
	fi
	
	if [[ ! -f $1 ]]; then
		PRINT_DB_LOG "SQL File $1 IS Empty"
		return 1
	fi
	

	
	local table_count
	if [[ ! -z $2 ]]; then
		table_count=$2
	else
		table_count=1
	fi
	
	local array
	local tmp_sql
	local curr_sql
	local db_count=${SHARDING_SLICE}
	
	local ROUTING_REPLACE_LEFT="\["
	local ROUTING_REPLACE_RIGHT="\]"
	local ROUTING_RULE_LEFT="(("
	local ROUTING_RULE_RIGHT=">>6)%8)=#"
	local TMP_SQL_FILE_PATH="/home/work/beidou-cron/log/"

	local threadname=$0"_"${RANDOM}"_"`date +%s`

	sed   "s/${ROUTING_REPLACE_LEFT}/${ROUTING_RULE_LEFT}/g" $1 > ${TMP_SQL_FILE_PATH}/${threadname}"_left.data"
	sed   "s/${ROUTING_REPLACE_RIGHT}/${ROUTING_RULE_RIGHT}/g"  ${TMP_SQL_FILE_PATH}/${threadname}"_left.data" > ${TMP_SQL_FILE_PATH}/${threadname}"_right.data"
	

	for((db_idx=0;db_idx<db_count;db_idx++))
	do
	    #tmp_sql=${sql}
	    #replace db placeholder '#' if exists in sql statement 
		sed   "s/#/${db_idx}/g"  ${TMP_SQL_FILE_PATH}/${threadname}"_right.data"  > ${TMP_SQL_FILE_PATH}/${threadname}"_${db_idx}_db.data"
		#tmp_sql=${tmp_sql//${DB_REPLACER}/${db_idx}}
		for((idx=0;idx<table_count;idx++))
    	do
			#replace table placeholder '?' if exists in sql statement
			sed   "s/?/${idx}/g"  ${TMP_SQL_FILE_PATH}/${threadname}"_${db_idx}_db.data" > ${TMP_SQL_FILE_PATH}/${threadname}"_${db_idx}_tb${idx}.data"
        	curr_sql=`cat ${TMP_SQL_FILE_PATH}/${threadname}"_"${db_idx}"_tb"${idx}".data"` 
			if [ $idx -eq 0 ];then 
				array[$db_idx]="${curr_sql}"
			else
				array[$db_idx]="${array[$db_idx]};${curr_sql}"
			fi
    	done
  	done

    for((idx=0;idx<db_count;idx++))
    do
	    sleep 5
		local mysql_conn="${MYSQL_CLIENT} -B -N -h${DB_HOST_BD_MAID[$idx]} -P${DB_PORT_BD_MAID[$idx]} -u${DB_USER_BD_MAID} -p${DB_PASSWORD_BD_MAID} -Dbeidou --default-character-set=gbk --skip-column-names"
		db_file_execute "${mysql_conn}" "${array[$idx]}" "/dev/null" ${idx}_`date +%s`|| alert $? "$0-Error Write Data From Sharding No $idx:${DB_HOST_BD_MAID[$idx]}"  &
    done

    wait
	rm -f ${TMP_SQL_FILE_PATH}/${threadname}*.data
	return 0
}


#run sql on one sharding
#$1:insert/update/delete sql file to execute-----notice that the statement with dbname.XXX is required
#$2:sharding number[0-7]
function runfilesql_one_sharding()
{
    
	if [ $# -lt 1 ]
	then 
		PRINT_DB_LOG "args isless than 1"
		exit 1
	fi
	
	if [[ ! -f $1 ]]
	then
		PRINT_DB_LOG "SQL File $1 IS Empty"
		return 1
	fi
		
	local sharding_idx=$2
   	if [ -z $sharding_idx ]
   	then
      PRINT_LOG "$0:The 2rd Parameter For Function runfilesql_one_sharding can not be null"
      return 1
   	fi
		
	local ROUTING_REPLACE_LEFT="\["
	local ROUTING_REPLACE_RIGHT="\]"
	local ROUTING_RULE_LEFT="(("
	local ROUTING_RULE_RIGHT=">>6)%8)=#"
	local TMP_SQL_FILE_PATH="/home/work/beidou-cron/log/"

	local threadname=$0"_"${RANDOM}"_"`date +%s`

	sed   "s/${ROUTING_REPLACE_LEFT}/${ROUTING_RULE_LEFT}/g" $1 > ${TMP_SQL_FILE_PATH}/${threadname}"_left.data"
	sed   "s/${ROUTING_REPLACE_RIGHT}/${ROUTING_RULE_RIGHT}/g"  ${TMP_SQL_FILE_PATH}/${threadname}"_left.data" > ${TMP_SQL_FILE_PATH}/${threadname}"_right.data"
	

	local mysql_conn="${MYSQL_CLIENT} -B -N -h${DB_HOST_BD_MAID[$sharding_idx]} -P${DB_PORT_BD_MAID[$sharding_idx]} -u${DB_USER_BD_MAID} -p${DB_PASSWORD_BD_MAID} -Dbeidou --default-character-set=gbk --skip-column-names"
	db_file_execute_with_cat "${mysql_conn}" ${TMP_SQL_FILE_PATH}/${threadname}"_right.data" "/dev/null" ${sharding_idx}_`date +%s`|| alert $? "$0-Error Write Data From Sharding No. $sharding_idx:${DB_HOST_BD_MAID[$sharding_idx]}"
	
	rm -f ${TMP_SQL_FILE_PATH}/${threadname}*.data
	return 0
}
#############################################################################################################################


#############################################################################################################################
#write daba to audit db
#$1:insert/update sql to execute------notice that the statement with dbname.XXX is required 
function runsql_audit()
{
	local sql=$1
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_AUDIT} -P${BEIDOU_DB_PORT_AUDIT} -u${BEIDOU_DB_USER_AUDIT} -p${BEIDOU_DB_PASSWORD_AUDIT} -Daudit"
	db_execute "${mysql_conn}" "${sql}" "/dev/null"
}


#read data from audit db
#$1:select sql to query------notice that the statement with dbname.XXX is required 
#$2:dump file name------notice that the full path is recommended
function runsql_audit_read()
{
	local sql=$1
    local dumpfile=$2
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${BEIDOU_DB_IP_AUDIT_READ} -P${BEIDOU_DB_PORT_AUDIT_READ} -u${BEIDOU_DB_USER_AUDIT_READ} -p${BEIDOU_DB_PASSWORD_AUDIT_READ} -Daudit --default-character-set=gbk --skip-column-names"
	db_execute "${mysql_conn}" "${sql}" "${dumpfile}"
}
#############################################################################################################################


#$1:sql to query---------notice that the statement with dbname.XXX is required
#$2:dump file name------notice that the full path is recommended
function runsql_user_audit_read()
{
	if [ $# -lt 2 ];then
		PRINT_DB_LOG "args is less than 2"
	fi
	
	local sql=$1
	local dumpfile=$2
	
	local mysql_conn="${MYSQL_CLIENT} -B -N -h${DB_HOST_USER_AUDIT_READ} -P${DB_PORT_USER_AUDIT_READ} -u${DB_USER_AUDIT_READ} -p${DB_PASSWORD_ADUIT_READ} -Done_adx --default-character-set=utf8 --skip-column-names"
	
	db_execute "${mysql_conn}" "${sql}" "${dumpfile}"
}
