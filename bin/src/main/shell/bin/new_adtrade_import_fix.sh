#!/bin/bash

#filename: new_adtrade_import_fix.sh
#@auther: genglei01
#@date: 2013-12-17
#@version: 1.0.0.0 

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="../bin/alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/new_adtrade_import.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

function check_path()
{
    if ! [ -d $IMPORT_INPUT_FIX_PATH ]
    then 
        mkdir -p $IMPORT_INPUT_FIX_PATH
    fi
}

#backup file, delete files generate in the day before 7 days/last month
function backup_file()
{
    local timeNow=`date +%Y%m%d%H%M`
    backup_file=$1

    if [ -f $backup_file ]
    then
        cp $backup_file ${backup_file}.$timeNow
    fi

    if [ -f ${backup_file}.md5 ]
    then
        rm ${backup_file}.md5
    fi
}

function download_dic()
{
    if [ -f $REMOTE_TRADEDIC_LABEL3 ]
    then
        if [ -f ${REMOTE_TRADEDIC_LABEL3}.last ]
        then
            rm ${REMOTE_TRADEDIC_LABEL3}.last
        fi
        
        mv ${REMOTE_TRADEDIC_LABEL3} ${REMOTE_TRADEDIC_LABEL3}.last
    fi

    wget ftp://${REMOTE_TRADEDIC_HOST}${REMOTE_TRADEDIC_PATH}${REMOTE_TRADEDIC_LABEL3} -nd -nH  --limit-rate=30M ||  alert $? "Fetch[\
        new trade dic label 3 errror!"
    if [ -e ${REMOTE_TRADEDIC_LABEL3} -a -e ${REMOTE_TRADEDIC_LABEL3}.last ] && [ "`diff ${REMOTE_TRADEDIC_LABEL3} ${REMOTE_TRADEDIC_LABEL3}.last`" != "" ];
    then
        alert $? "New trade label 3 dic is different from last dic!"
    fi
}

function fix_import()
{
    check_path
    cd ${IMPORT_INPUT_FIX_PATH}
    download_dic

    #back up last fix-data
    if [ -f $REMOTE_FIX_INPUTDATA_FILE ]
    then
        if [ -f ${REMOTE_FIX_INPUTDATA_FILE}.last ]
        then
            rm ${REMOTE_FIX_INPUTDATA_FILE}.last
        fi

        mv ${REMOTE_FIX_INPUTDATA_FILE} ${REMOTE_FIX_INPUTDATA_FILE}.last
    fi
    
    PRINT_LOG "[fix]Begin to download fix data!"
    wget ftp://${REMOTE_FIX_INPUTDATA_SERVER_URL}${REMOTE_FIX_INPUTDATA_PATH}${REMOTE_FIX_INPUTDATA_FILE} -nd \
         -nH  --limit-rate=30M || alert $? "Fetch[ ${REMOTE_FIX_INPUTDATA_FILE} ] error!"
    
    if ! [ -s ${REMOTE_FIX_INPUTDATA_FILE} ]
    then
        alert 1 "Size of file[ ${REMOTE_FIX_INPUTDATA_FILE} ] is zero!"
    fi

    if [  -e "${REMOTE_FIX_INPUTDATA_FILE}"  -a ! -e "${REMOTE_FIX_INPUTDATA_FILE}.last" ]  || \
       [  -e "${REMOTE_FIX_INPUTDATA_FILE}" -a -e "${REMOTE_FIX_INPUTDATA_FILE}.last" -a \
       "`diff ${REMOTE_FIX_INPUTDATA_FILE} ${REMOTE_FIX_INPUTDATA_FILE}.last`" != "" ]
    then
        PRINT_LOG "[fix]Begin to download import data!"
        #import
        data_file=${IMPORT_INPUT_FIX_PATH}${IMPORT_INPUT_FIX_FILE}
        file_name=${IMPORT_INPUT_FIX_FILE}
        backup_file $file_name
        
		PRINT_LOG "[fix]Begin to import sql!"
		import_file ${file_name} "fix"
		
        PRINT_LOG "[fix]End to import sql!"
    else
        PRINT_LOG "No available data to import!"
    fi
    
    return 0
}

#$1:file_name
#$2:full/incr/fix
# input file format: adid userid newadtradeid
function import_file(){
	file_name=$1
	local db_count=8
	local table_count=8;
	
	if [ -f ${file_name}.fail ]
    then
    	rm ${file_name}.fail
    fi
    
    for ((idx=0; idx < ${db_count}; idx++))
	do
		for ((itx=0; itx < ${table_count}; itx++))
		do
			if [ -f ${file_name}.${idx}.${itx} ]
		    then
		    	rm ${file_name}.${idx}.${itx}
		    fi
		done
	done
	 
	awk -F"\t" 'ARGIND==1{trademap[$1]=$2}ARGIND==2{
        if($3 in trademap){
	        shard=int($2/2^6)%8;
	        table=$2%8;
	        #id, new_adtradeid, confidence_level(3, highest level)
	        outdata=$1"\t"$3"\t3" 
			print outdata >> "'${file_name}.'"shard"."table;
        }else{
            print ""$0"" >> "'${file_name}.fail'";
        }
     }' ${REMOTE_TRADEDIC_LABEL3} ${file_name}
     
    if [ -s ${file_name}.fail ]
    then
        #PRINT_LOG "$0-Check adtrade import data ERROR!"
        alert 1 "$0-[$2]Check adtrade import data ERROR!"
    fi
 
	for ((idx=0; idx < ${db_count}; idx++))
	do
		for ((itx=0; itx < ${table_count}; itx++))
		do
			if [ -s ${file_name}.${idx}.${itx} ]
			then
				PRINT_LOG "[$2]Begin to import sql[${file_name}.${idx}.${itx}]"
				exc_sql ${file_name}.${idx}.${itx} ${idx} ${itx}
				rm ${file_name}.${idx}.${itx}
				PRINT_LOG "[$2]End to import sql[${file_name}.${idx}.${itx}]"
			fi
		done
	done
	
	return $? 
}

function exc_sql(){
	#id, new_adtradeid, confidence_level
	local shard_table_file=$1
	local shard=$2
	local table=$3
	
	awk -F'\t' -v v=',' -v m=500 'ARGIND==1{tradeMap[$1]=$2}ARGIND==2{key=$2""$3;
		if(map[key]==""){
			if(lengthm[key]==""){
				lengthm[key]=1
			}
			map[key]=$1
		}else{
			if(int(lengthm[key]%m)==0){
				tempkey=key""lengthm[key]
				map[tempkey]=map[key]
				map[key]=""
			}
			if(map[key]==""){
				map[key]=$1
			}else{
				map[key]=map[key]v$1
			}
			lengthm[key]=lengthm[key]+1
			
		}}END{
		for(i in map){
			if(map[i] != ""){
			  #printf("%s\t%s\t%s\t%s\n",map[i],substr(i,0,6),substr(i,7,1));
			  table="'${table}'"
			  oldtradeid=tradeMap[substr(i,0,6)]
			  print "UPDATE cprounitmater"table" m, cprounitstate"table" s SET m.new_adtradeid="substr(i,0,6)", m.adtradeid="oldtradeid", m.confidence_level="substr(i,7,1)" WHERE \
                m.id IN ("map[i]") AND m.id = s.id;";
			}
		}
	}' ${NEWTRADEID_OLDTRADEID_FILE} ${shard_table_file} > ${shard_table_file}.sql
	
	local tempsqlfile=importtempfile.sql
	local count=0
	echo "use beidou;" > $tempsqlfile
	cat ${shard_table_file}.sql | while read line
	do
		if [ $((count % IMPORT_SQL_PAGE_SIZE)) -eq 0 ]
		then
			echo -e "$line" >> $tempsqlfile
			local_runfilesql_single_sharding "$tempsqlfile" $shard || alert $? "$0-Import \
			Error [$tempsqlfile], Sharding No ${shard}:${DB_HOST_BD_MAID[${shard}]}, Table No $table"
	        #PRINT_LOG "Test----run file sql[$tempsqlfile], Sharding No ${shard}:${DB_HOST_BD_MAID[${shard}]}, Table No $table"
	        sleep $IMPORT_SQL_SLEEP_SECOND
	        echo "use beidou;" >  $tempsqlfile
	    else
	    	echo -e "$line" >> $tempsqlfile
	    fi
		count=$((count+1))
	done 
	        
	tempsqlfileLen=`wc -l $tempsqlfile | awk  '{ print $1 }'`
	#Have "use beidou;" at least
	if [ $tempsqlfileLen -gt 1 ]
	then
		local_runfilesql_single_sharding "$tempsqlfile" $shard || alert $? "$0-Import \
		Error [$tempsqlfile], Sharding No ${shard}:${DB_HOST_BD_MAID[${shard}]}, Table No $table"
	    #PRINT_LOG "Test----run file sql[$tempsqlfile], Sharding No ${shard}:${DB_HOST_BD_MAID[${shard}]}, Table No $table"
	    sleep $IMPORT_SQL_SLEEP_SECOND
	fi 
	
	rm ${shard_table_file}.sql
	rm $tempsqlfile
	return 0;
}

#no sleep 5
#$1:import sql
#$2:sharding_idx
function local_runfilesql_single_sharding()
{
	if [ $# -lt 2 ]
	then 
		PRINT_LOG "args isless than 2"
		exit 1
	fi
	
	if [[ ! -f $1 ]]; then
		PRINT_LOG "SQL File $1 IS Empty"
		return 1
	fi

	local sharding_idx=$2
   	if [ -z $sharding_idx ];then
      PRINT_LOG "$0:The 2rd Parameter For Function local_runfilesql_single_sharding can not be null"
      return 1
   	fi
   
	
   	local mysql_conn="${MYSQL_CLIENT} -B -N -h${DB_HOST_BD_MAID[$sharding_idx]} -P${DB_PORT_BD_MAID[$sharding_idx]} -u${DB_USER_BD_MAID} -p${DB_PASSWORD_BD_MAID} -Dbeidou --default-character-set=gbk --skip-column-names"
	
	local cnt=0   
	while [[ $cnt -lt $RETRY_TIMES ]]; do
			$mysql_conn < $1  2>> $IMPORT_LOG_PATH
	        if [[ $? -eq 0 ]]; then
	            return 0
	        fi          
			cnt=$(($cnt+1))
			sleep 5 
	done    
	PRINT_LOG "$0:Execute Query Failed-$1"
	return 1
}

function PRINT_LOG()
{
    local logTime=`date +%Y%m%d-%H:%M:%S`
    echo "[${logTime}]$0-$1" >> $IMPORT_LOG_PATH
}

fix_import
exit $?
