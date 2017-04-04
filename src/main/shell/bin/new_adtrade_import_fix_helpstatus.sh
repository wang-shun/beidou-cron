#!/bin/bash

#filename: new_adtrade_import.sh
#@auther: xuxiaohu
#@date: 2013-06-18
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
    if ! [ -d $IMPORT_INPUT_INCR_PATH ]
    then 
        mkdir -p $IMPORT_INPUT_INCR_PATH
    fi
    
    if ! [ -d $IMPORT_INPUT_FULL_PATH ]
    then 
        mkdir -p $IMPORT_INPUT_FULL_PATH
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
        rm $backup_file
    fi

    if [ -f ${backup_file}.md5 ]
    then
        rm ${backup_file}.md5
    fi

    #delete files generate in the day before 7 days
    oneWeekAgoPrefix=`date --date "-1 week " +%Y%m%d`
    rm ${backup_file}.${oneWeekAgoPrefix}*
    
	#delete temp files
    oneMonthAgoPrefix=`date --date "-1 month " +%Y%m`
    rm ${backup_file}.${oneMonthAgoPrefix}*
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

function incr_import()
{
    check_path
    cd ${IMPORT_INPUT_INCR_PATH}
    mkdir tmpdir
    cd tmpdir
    download_dic

    #back up donelist
    if [ -f $REMOTE_INCR_INPUTDATA_DONELIST ]
    then
        if [ -f ${REMOTE_INCR_INPUTDATA_DONELIST}.last ]
        then
            rm ${REMOTE_INCR_INPUTDATA_DONELIST}.last
        fi

        mv ${REMOTE_INCR_INPUTDATA_DONELIST} ${REMOTE_INCR_INPUTDATA_DONELIST}.last
    fi
    
    PRINT_LOG "[incr]Begin to download import downlist!"
    wget ftp://${REMOTE_INCR_INPUTDATA_SERVER_URL}${REMOTE_INCR_INPUTDATA_PATH}${REMOTE_INCR_INPUTDATA_DONELIST} -nd \
         -nH  --limit-rate=30M || alert $? "Fetch[ ${REMOTE_INCR_INPUTDATA_DONELIST} ] error!"
    
    if ! [ -s ${REMOTE_INCR_INPUTDATA_DONELIST} ]
    then
        alert 1 "Size of file[ ${REMOTE_INCR_INPUTDATA_DONELIST} ] is zero!"
    fi

    if [  -e "${REMOTE_INCR_INPUTDATA_DONELIST}"  -a ! -e "${REMOTE_INCR_INPUTDATA_DONELIST}.last" ]  || \
       [  -e "${REMOTE_INCR_INPUTDATA_DONELIST}" -a -e "${REMOTE_INCR_INPUTDATA_DONELIST}.last" -a \
       "`diff ${REMOTE_INCR_INPUTDATA_DONELIST} ${REMOTE_INCR_INPUTDATA_DONELIST}.last`" != "" ]
    then
        PRINT_LOG "[incr]Begin to download import data!"
        #import
        #get the last line
        data_file=`awk -F"\t" '{a=$1} END {print a}' ${REMOTE_INCR_INPUTDATA_DONELIST}`
        file_name=`echo  "${data_file}" | awk -F"/" '{print $NF}'`
        backup_file $file_name

        wget ftp://${REMOTE_INCR_INPUTDATA_SERVER_URL}${data_file} -nd -nH  --limit-rate=30M || \
           alert $? "Fetch[ ${REMOTE_INCR_INPUTDATA_SERVER_URL}${data_file} ] error!"
        PRINT_LOG "[incr]End to download import data!"
        
		PRINT_LOG "[incr]Begin to import sql!"
		import_file ${file_name} "incr"
		
        #delete input files
      	oneWeekAgoPrefix=`date --date "-1 week " +%Y%m%d`
      	rm ${oneWeekAgoPrefix}*
      	
      	oneMonthAgoPrefix=`date --date "-1 month " +%Y%m`
      	rm ${oneMonthAgoPrefix}*
        PRINT_LOG "[incr]End to import sql!"
    else
        PRINT_LOG "No available data to import!"
    fi
    
    rm -rf tmpdir
    return 0
}

function full_import()
{
    check_path
    cd ${IMPORT_INPUT_FULL_PATH}
    download_dic

    #back up donelist
    if [ -f $REMOTE_FULL_INPUTDATA_DONELIST ]
    then
        if [ -f ${REMOTE_FULL_INPUTDATA_DONELIST}.last ]
        then
            rm ${REMOTE_FULL_INPUTDATA_DONELIST}.last
        fi

        mv ${REMOTE_FULL_INPUTDATA_DONELIST} ${REMOTE_FULL_INPUTDATA_DONELIST}.last
    fi
    
    PRINT_LOG "[FULL]Begin to download import downlist!"
    wget ftp://${REMOTE_FULL_INPUTDATA_SERVER_URL}${REMOTE_FULL_INPUTDATA_PATH}${REMOTE_FULL_INPUTDATA_DONELIST} -nd \
         -nH  --limit-rate=30M || alert $? "Fetch[ ${REMOTE_FULL_INPUTDATA_DONELIST} ] error!"
    
    if ! [ -s ${REMOTE_FULL_INPUTDATA_DONELIST} ]
    then
        alert 1 "Size of file[ ${REMOTE_FULL_INPUTDATA_DONELIST} ] is zero!"
    fi

    if [  -e "${REMOTE_FULL_INPUTDATA_DONELIST}"  -a ! -e "${REMOTE_FULL_INPUTDATA_DONELIST}.last" ]  || \
       [  -e "${REMOTE_FULL_INPUTDATA_DONELIST}" -a -e "${REMOTE_FULL_INPUTDATA_DONELIST}.last" -a \
       "`diff ${REMOTE_FULL_INPUTDATA_DONELIST} ${REMOTE_FULL_INPUTDATA_DONELIST}.last`" != "" ]
    then
        PRINT_LOG "[FULL]Begin to download import data!"
        #import
        #get the last line
        data_file=`awk -F"\t" '{a=$1} END {print a}' ${REMOTE_FULL_INPUTDATA_DONELIST}`
        file_name=`echo  "${data_file}" | awk -F"/" '{print $NF}'`
        backup_file $file_name

        wget ftp://${REMOTE_FULL_INPUTDATA_SERVER_URL}${data_file} -nd -nH  --limit-rate=30M || \
           alert $? "Fetch[ ${REMOTE_FULL_INPUTDATA_SERVER_URL}${data_file} ] error!"
        PRINT_LOG "[FULL]End to download import data!"

        PRINT_LOG "[FULL]Begin to import sql!"
        #temp dir
        mkdir -p split_temp
        cd split_temp
		local one_file_line=1000000
		local prefix_split=split_prefix
        rm ${prefix_split}*
		split -l $one_file_line ../${file_name} $prefix_split
		cd ${IMPORT_INPUT_FULL_PATH}
		
		for i in `ls split_temp`
		do
			PRINT_LOG "[FULL]Begin to deal with data file [$i]"
			import_file "split_temp/$i" "FULL"
			PRINT_LOG "[FULL]Finish to deal with data file [$i]"
		done
		
        sleep 20
        rm split_temp/*
        
        #delete temp files
      	oneWeekAgoPrefix=`date --date "-1 week " +%Y%m%d`
      	rm ${oneWeekAgoPrefix}*
      	
      	oneMonthAgoPrefix=`date --date "-1 month " +%Y%m`
      	rm ${oneMonthAgoPrefix}*
        PRINT_LOG "[FULL]End to import sql!"
    else
        PRINT_LOG "No available data to import!"
    fi
    
    return 0
}

#$1:file_name
#$2:full/incr
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
        if(($2 in trademap) && (($4==1) || ($4==2) || ($4==3))){
	        shard=int($6/2^6)%8;
	        table=$6%8;
	        #id, new_adtradeid, confidence_level
	        outdata=$1"\t"$2"\t"$4 
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
	
	awk -F'\t' -v v=',' -v m=500 '{key=$2""$3;
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
			  print "UPDATE cprounitmater"table" m, cprounitstate"table" s SET m.new_adtradeid="substr(i,0,6)", m.confidence_level="substr(i,7,1)" WHERE \
                m.id IN ("map[i]") AND m.id = s.id  AND m.new_adtradeid=0 ;";
			}
		}
	}' ${shard_table_file} > ${shard_table_file}.sql
	
	local tempsqlfile=importtempfile.sql
	local count=0
	echo "use beidou;" > $tempsqlfile
	cat ${shard_table_file}.sql | while read line
	do
		if [ $((count % IMPORT_SQL_PAGE_SIZE)) -eq 0 ]
		then
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

if [ $# -eq 0 ]
then 
    incr_import
else
    full_import
fi

exit $?


