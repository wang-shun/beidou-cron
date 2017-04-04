#!/bin/bash

#@filename: new_adtrade_export.sh
#@auther: xuxiaohu
#@date: 2013-06-14
#@version: 1.0.0.0
#@brief: export ad materials non-classified to other system to be classified

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="../bin/alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/db.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/new_adtrade_export.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

function check_path()
{
    if ! [ -w $EXPORT_OUT_PATH ]
    then
        if ! [ -e $EXPORT_OUT_PATH ]
        then
            mkdir -p $EXPORT_OUT_PATH
            if [ $? -ne 0 ]
            then
                PRINT_LOG "Fail to mkdir [$EXPORT_OUT_PATH]!"
                return 1
            fi
        else
            PRINT_LOG "Path[$EXPORT_OUT_PATH] is not writable!"
            return 1
        fi
    fi
    
    if ! [ -w $EXPORT_BACKUP_PATH ]
    then
        if ! [ -e $EXPORT_BACKUP_PATH ]
        then
            mkdir -p $EXPORT_BACKUP_PATH
            if [ $? -ne 0 ]
            then
                PRINT_LOG "Fail to mkdir [$EXPORT_BACKUP_PATH]!"
                return 1
            fi
        else
            PRINT_LOG "Path[$EXPORT_BACKUP_PATH] is not writable!"
            return 1
        fi
    fi

    if ! [ -w $EXPORT_FULL_OUT_PATH ]
    then
        if ! [ -e $EXPORT_FULL_OUT_PATH ]
        then
            mkdir -p $EXPORT_FULL_OUT_PATH
            if [ $? -ne 0 ]
            then
                PRINT_LOG "Fail to mkdir [$EXPORT_FULL_OUT_PATH]!"
                return 1
            fi
        else
            PRINT_LOG "Path[$EXPORT_FULL_OUT_PATH] is not writable!"
            return 1
        fi
    fi
    
    if ! [ -w $EXPORT_FULL_BACKUP_PATH ]
    then
        if ! [-e $EXPORT_FULL_BACKUP_PATH ]
        then
            mkdir -p $EXPORT_FULL_BACKUP_PATH
            if [ $? -ne 0 ]
            then
                PRINT_LOG "Fail to mkdir [$EXPORT_FULL_BACKUP_PATH]!"
                return 1
            fi
        else
            PRINT_LOG "Path[$EXPORT_FULL_BACKUP_PATH] is not writable!"
            return 1
        fi
    fi
    
    return 0
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
        PRINT_LOG "[error used] please input operation_sql for param 1"
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
            $db_info -e "$operation_sql" > $export_file  2>> ${EXPORT_LOG_PATH}
        else
            $db_info -e "$operation_sql"  2>> ${EXPORT_LOG_PATH}
        fi
        if [[ $? -eq 0 ]]; then
            return 0
        fi
        cnt=$(($cnt+1))
        sleep 5 
    done

    PRINT_LOG "$0:Execute Query Failed-$2"
    return 1
}

#$1:full_export_sql_suffix
function generate_full_sql(){
	local db_count=8
    local tb_count=8
    local full_export_sql_suffix=$1
    
    #delete filter ad id file
	if [ -f ${FILTER_ADID_FILE} ]
    then
        rm ${FILTER_ADID_FILE}
    fi
    	
    #get filter ad id file
    wget ftp://${FILTER_ADID_HOST_PATH}/${FILTER_ADID_FILE} -nd -nH  --limit-rate=30M
	if [ $? -eq 0 ];then
		if ! [ -s ${FILTER_ADID_FILE} ] 
		then
			PRINT_LOG "File[${FILTER_ADID_FILE}] is empty!"
		    alert 1 "Fetch[${FILTER_ADID_HOST_PATH}/${FILTER_ADID_FILE}]  ERROR!"
		fi
	else
		alert 1 "Fetch[${FILTER_ADID_HOST_PATH}/${FILTER_ADID_FILE}]  ERROR!"
	fi
	
	local full_export_data_suffix="_tempsql_for_full_export.data"
	if [ -f ${full_export_data_suffix}.fail ]
    then
    	rm ${full_export_data_suffix}.fail
   	fi
   	 
   	#sub-database, sub-table
	awk -F"\t" '{
    	if((NF==2) && ($0~/^([0-9\t])+$/)){
	    	shard=int($2/2^6)%8;
	        table=$2%8;
			print $1 >> "'${full_export_data_suffix}.'"shard"."table;
        }else{
            print $0 >> "'${full_export_data_suffix}.fail'";
        }
     }' ${FILTER_ADID_FILE}
     	
    #if has error data, alert and exit
	if [ -s ${full_export_data_suffix}.fail ]
    then
        #PRINT_LOG "$0-Check $FILTER_ADID_FILE data ERROR!"
        alert 1 "$0-[$2]Check $FILTER_ADID_FILE data ERROR!"
    fi
    
    tempsql="SELECT s.id,s.targetUrl,s.wuliaoType, s.mcId, s.mcVersionId, s.fileSrc, t.uid,\
        IF(s.title= '' || s.title is NULL, '-', s.title),IF(s.description1=''\
        || s.description1 is NULL, '-', s.description1),IF(s.description2= ''||s.description2 is NULL,'-',s.description2),\
        t.gid,t.pid FROM beidou.cprounitstate? t,beidou.cprounitmater? s WHERE t.id = s.id AND t.state<>2 AND s.id in "
    
    #generates sql file for every table in each shard   
	for ((idx=0; idx < ${db_count}; idx++))
	do
		for ((itx=0; itx < ${tb_count}; itx++))
		do
			local tmp_file=${full_export_data_suffix}.${idx}.${itx}
			if ! [ -s $tmp_file ]
			then
		    	continue
		   	fi
		   	
		   	#replace ? to table index
		   	local sql_suffix=${tempsql//\?/${itx}}
		   	#get id size
			local tmp_file_len=`wc -l $tmp_file | awk  '{ print $1 }'`
			#limit every sql size
			awk -F'\t' -v m=2000 -v ss="$sql_suffix" 'BEGIN{cn=0; tmp}
			{
				cn=cn+1;
				if(cn > 0 && int(cn%m)==0){
					print ss"("tmp$1");";
					tmp="";
				}else if(cn != '$tmp_file_len'){
					tmp=tmp$1","
				}else{
					tmp=tmp$1
				}
			}
			END{
				if(int(cn%m) != 0){
					print ss"("tmp");";
				}
			}' $tmp_file > ${full_export_sql_suffix}.${idx}.${itx}
			
			rm $tmp_file
		done
	done
	
}

#0: full 1: incremental
function do_export()
{
    local db_count=8
    local tb_count=8
    local tempsql
    isincr=$1
    
    local full_export_sql_suffix="full_export_sql_suffix.sql"
	local outFile
	#for full
    if [ $isincr -eq 0 ]
    then
    	cd ${EXPORT_FULL_OUT_PATH}
    	outFile=${EXPORT_FULL_OUT_PATH}/${EXPORT_FULL_FILE}
		generate_full_sql "${full_export_sql_suffix}"      
	#for incr 
    else
        cd ${EXPORT_OUT_PATH}
        outFile=${EXPORT_OUT_PATH}/${EXPORT_FILE}
        tempsql="SELECT s.id,s.targetUrl,s.wuliaoType, s.mcId, s.mcVersionId, s.fileSrc, t.uid,\
        IF(s.title= '' || s.title is NULL, '-', s.title),IF(s.description1=''\
        || s.description1 is NULL, '-', s.description1),IF(s.description2= ''||s.description2 is NULL,'-',s.description2),\
        t.gid,t.pid FROM beidou.cprounitstate? t,beidou.cprounitmater? s WHERE t.id = s.id AND s.new_adtradeid=0 AND t.state<>2 "
    fi
    
    local tempdatasuffix="tempdata_for_new_adtrade_export"
    #Query db
    for ((idx=0; idx<db_count; idx++)) 
    do
        local mysql_conn="${MYSQL_CLIENT} -B -N -h${DB_HOST_BD_MAID_READ[$idx]} -P${DB_PORT_BD_MAID_READ[$idx]} \
        -u${DB_USER_BD_MAID_READ} -p${DB_PASSWORD_BD_MAID_READ} -Dbeidou --default-character-set=utf8 \
        --skip-column-names"
        for ((itx=0; itx<tb_count; itx++))
        do
            if [ $isincr -eq 0 ]
            then
            	#full export
            	if [ -s ${full_export_sql_suffix}.${idx}.${itx} ]
            	then
		            cat ${full_export_sql_suffix}.${idx}.${itx} | while read line
				    do
				    	local tmp_full_part_data="_tmp_full_part_data.data"
				        db_execute "${mysql_conn}" "${line}" "${tmp_full_part_data}" || alert $? "$0-Error Read Data\
	                    From Sharding No $idx: ${DB_HOST_BD_MAID_READ[$idx]}; table index: ${itx}; sql: ${line}" 
	                    cat $tmp_full_part_data >> ${tempdatasuffix}.${idx}${itx}
	                    rm $tmp_full_part_data
				    done 
				    
				    rm ${full_export_sql_suffix}.${idx}.${itx}
        		fi
            else
            	#incr export
            	#replace ? to table index
            	local sql=${tempsql//\?/${itx}}
                db_execute "${mysql_conn}" "${sql}" ${tempdatasuffix}.${idx}${itx} || alert $? "$0-Error Read Data\
                From Sharding No $idx: ${DB_HOST_BD_MAID_READ[$idx]}; table index: ${itx}"  
            fi
        done
    done

    wait
    
    #deal result: (1)base data, (2)pre data for landing page analysis, (3)pre data for flash analysis
    for ((idx=0; idx<db_count; idx++))
    do
        for ((itx=0; itx<tb_count; itx++))
        do
           #deal_targeturl
           deal_targeturl ${tempdatasuffix}.${idx}${itx} ${outFile}.targeturl.temp
           #deal flash url
           deal_flashurl ${tempdatasuffix}.${idx}${itx} ${outFile}.flashurl
           cat ${tempdatasuffix}.${idx}${itx} >> ${outFile}
           rm ${tempdatasuffix}.${idx}${itx}
        done
    done

    #remove targeturl data file empty line
    awk 'NF>0' ${outFile}.targeturl.temp > ${outFile}.targeturl
    rm ${outFile}.targeturl.temp
    return 0
}

function deal_flashurl(){
    local deal_input=$1
    local deal_output=$2
    #id wuliaoType mcid mcVersionId fileSrc
    #genglei:wuliaoType==2 || wuliaoType==3 for flash analysis
    awk -F"\t" '{if($3 == 2 || $3 == 3){print $1","$3","$4","$5","$6}}' $deal_input >> $deal_output
    return 0
}

function deal_targeturl()
{
    local deal_input=$1
    local deal_output=$2
    #$1:aid, $2:target url
    awk -F'\t' -v v=',' '{if(map[$2]==""){map[$2]=$1}else{map[$2]=map[$2]v$1}}END{for(i in map)\
        {printf("%s\t%s\n",map[i],i)}}' $deal_input >> $deal_output
}

function deal_targeturl_old()
{
    local deal_input=$1
    local deal_output=$2
    #id targeturl ...
    sort -t\t +2 $deal_input > sortesttemp
    local last_id
    local last_url
    i=1

    while read line
    do
        id=`echo "$line" | awk -F "\t" '{print $1}'`
        url=`echo "$line" | awk -F "\t" '{print $2}'`
        if [ $i -eq 1 ]
        then
            last_id=$id
            last_url=$url
        else
            if [ "$url" = "$last_url" ]
            then
                last_id="${last_id},$id"
            else
                echo -e "${last_id}\t${last_url}" >> $deal_output
                last_id=$id
                last_url=$url
            fi
        fi
        i=$((i+1))
    done < sortesttemp
    echo -e "${last_id}\t${last_url}" >> $deal_output
    rm sortesttemp
    return 0
}

function export_full()
{
    check_path
    if [ $? -ne 0 ]
    then
        return 1
    fi

    local timeNow=`date +%Y%m%d%H%M`
    outFile=${EXPORT_FULL_OUT_PATH}/${EXPORT_FULL_FILE}
    
    if [ -f $outFile ]
    then
        cp $outFile ${EXPORT_FULL_BACKUP_PATH}/${EXPORT_FULL_FILE}.$timeNow
        rm $outFile
        PRINT_LOG "Suc to backup $outFile"

        cd $EXPORT_FULL_BACKUP_PATH
        md5sum ${EXPORT_FULL_FILE}.$timeNow > ${EXPORT_FULL_FILE}.$timeNow.md5
        
     	#delete files generate in the day before 7 days
      	oneWeekAgoPrefix=`date --date "-1 week " +%Y%m%d`
      	rm ${EXPORT_FULL_FILE}.${oneWeekAgoPrefix}*
      	
      	oneMonthAgoPrefix=`date --date "-1 month " +%Y%m`
      	rm ${EXPORT_FULL_FILE}.${oneMonthAgoPrefix}*
    fi

    if [ -f ${outFile}.md5 ]
    then
        rm ${outFile}.md5
    fi

    #delete targeturl file
    if [ -f ${outFile}.targeturl ]
    then
        rm ${outFile}.targeturl
    fi

	#delete flash url
    if [ -f ${outFile}.flashurl ]
    then
        rm ${outFile}.flashurl
    fi
    
    #excute sql to file
    do_export 0 
    if [ $? -ne 0 ]
    then
        return 1
    fi
}

function export_incr()
{
    check_path
    if [ $? -ne 0 ]
    then
        return 1
    fi

    local timeNow=`date +%Y%m%d%H%M`
    outFile=${EXPORT_OUT_PATH}/${EXPORT_FILE}
    
    if [ -f $outFile ]
    then
        cp $outFile ${EXPORT_BACKUP_PATH}/${EXPORT_FILE}.$timeNow
        rm $outFile
        PRINT_LOG "Suc to backup $outFile"

        cd $EXPORT_BACKUP_PATH
        md5sum ${EXPORT_FILE}.$timeNow > ${EXPORT_FILE}.$timeNow.md5
    	
    	#delete files generate in the day before 7 days
      	oneWeekAgoPrefix=`date --date "-1 week " +%Y%m%d`
      	rm ${EXPORT_FILE}.${oneWeekAgoPrefix}*
      	
      	oneMonthAgoPrefix=`date --date "-1 month " +%Y%m`
      	rm ${EXPORT_FILE}.${oneMonthAgoPrefix}*
    fi

    if [ -f ${outFile}.md5 ]
    then
        rm ${outFile}.md5
    fi
    
    #delete targeturl file
    if [ -f ${outFile}.targeturl ]
    then
        rm ${outFile}.targeturl
    fi

    #delete flash url
    if [ -f ${outFile}.flashurl ]
    then
        rm ${outFile}.flashurl
    fi

    #excute sql to file
    do_export 1
   
    if [ $? -ne 0 ]
    then
        return 1
    fi
}

function PRINT_LOG()
{
    local logTime=`date +%Y%m%d-%H:%M:%S`
    echo "[${logTime}]$0-$1" >> $EXPORT_LOG_PATH
}

if [ $# -eq 0 ]
then
    export_incr
else
    export_full
fi

exit $?
