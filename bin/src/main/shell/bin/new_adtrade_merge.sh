#!/bin/bash

#filename: new_adtrade_export.sh
#@auther: xuxiaohu
#@date: 2013-06-14
#@version: 1.0.0.0
#@brief: merge target, flash and other text data 

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="../bin/alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/new_adtrade_merge.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

function check_path()
{
    if ! [ -d $MERGE_INPUT_INCR_PATH ]
    then  
        mkdir -p $MERGE_INPUT_INCR_PATH
    fi  
    
    if ! [ -d $MERGE_INCR_OUTPUT_PATH ]
    then
        mkdir -p $MERGE_INCR_OUTPUT_PATH
    fi                                                               
    
	if ! [ -d $MERGE_INPUT_FULL_PATH ]
    then
        mkdir -p $MERGE_INPUT_FULL_PATH
    fi  
    
    if ! [ -d $MERGE_FULL_OUTPUT_PATH ]
    then
        mkdir -p $MERGE_FULL_OUTPUT_PATH
    fi                                                               
    return 0
}

#backup file, delete files generate in the day before 7 days
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

      return 0
}

function get_remote_file()
{
    hostAndPath=$1
    remoteFileName=$2
    storeFileName=$3

	local DOWNLOAD_RETRY_TIMES=10
	cnt=0   
	while [[ $cnt -lt $DOWNLOAD_RETRY_TIMES ]]; do
		wget ftp://${hostAndPath}${remoteFileName} -nd -nH  --limit-rate=30M
		if [ $? -eq 0 ];then
			mv ${remoteFileName} ${storeFileName}
		    if ! [ -s ${storeFileName} ] 
		    then
		        PRINT_LOG "File[${storeFileName}] is empty!"
		    fi
    		return 0
		else
			sleep 60 
		fi
		cnt=$(($cnt+1))
	done
	
   alert 1 "Fetch[${hostAndPath}${remoteFileName}]  ERROR!"
}

function deal_target_msg()
{
    local inputFile=$1
    local outputFile=$2
    if ! [ -f $outputFile ]
    then
        #outputfile must be exsited
        touch $outputFile
    fi
    touch ${outputFile}.tmp && awk -F"\t" '{ alen=split($1, a ,","); for(i=1; i<= alen; i++){print a[i]"\t"$2} }' \
        ${inputFile} | awk -F"\t" '{print $1"\t"$2}' > ${outputFile}.tmp

    #remove empty line
    awk 'NF>0' ${outputFile}.tmp > ${outputFile} && rm ${outputFile}.tmp
    return 0
}

function base_target_merge()
{
    local base=$1
    local target=$2
    local output=$3
    #join -t$'\t' -a1 -o '1.1 1.8 1.9 1.10 1.7 2.2' "$base" "$target" > base.tttmp
    #awk -F"\t" '{a=$6; if( a == "" ){ a="-";} print $1"\t"$2","$3","$4"\t"a"\t"$5}' base.tttmp > $output
    #rm base.tttmp
    #base :id,targetUrl,wuliaoType,mcId,mcVersionId,fileSrc,uid,t1,t2,t3,gid,pid
    awk -F"\t" 'ARGIND==1{target[$1]=$2}ARGIND==2{tv="-"; if($1 in target ){tv=target[$1]} print $1"\t"$8","$9","$10"\t"tv"\t"$7"\t"$11"\t"$12}' $target $base > $output
    return 0
}

function base_flash_merge()
{
    local base=$1
    local flash=$2
    local output=$3
    
    #join -t$'\t' -a1 -o '1.1 1.2 1.3 1.4 2.2' "$base" "$flash" > base.tttmp2
    #awk -F"\t" '{a=$5; if( a == "" ){ a="-";} print $1"\t"$2"\t"$3"\t"a"\t"$4}' base.tttmp2 > $output
    #rm base.tttmp2
    #base:id text targeturlText flashText uid gid pid
    awk -F"\t" 'ARGIND==1{flash[$1]=$2}ARGIND==2{fv="-"; if($1 in flash ){fv=flash[$1]} print $1"\t"$2"\t"$3"\t"fv"\t"$4"\t"$5"\t"$6}' $flash $base > $output
    return 0
}
function incr_merge()
{
    cd $MERGE_INPUT_INCR_PATH
    backup_file  $MERGE_INPUT_DATA_FILE
    backup_file $MERGE_INPUT_TAGET_MSG_FILE
    backup_file $MERGE_INPUT_FLASH_MSG_FILE
    
    #fetch base, flash, taegeturl
    get_remote_file ${REMOTE_INCRDATA_SERVER_URL}${REMOTE_INCRDATA_PATH} ${REMOTE_INCRDATA_FILE} ${MERGE_INPUT_DATA_FILE}
    get_remote_file ${REMOTE_INCRTARGET_MSG_SERVER_URL}${REMOTE_INCRTARGET_MSG_PATH} ${REMOTE_INCRTARGET_MSG_FILE} \
        ${MERGE_INPUT_TAGET_MSG_FILE}
    get_remote_file ${REMOTE_FLASH_MSG_SERVER_URL}${REMOTE_FLASH_MSG_PATH} ${REMOTE_FLASH_MSG_FILE} \
        ${MERGE_INPUT_FLASH_MSG_FILE}
    
    local targetmsgFile=targetmsg.data
    deal_target_msg $MERGE_INPUT_TAGET_MSG_FILE $targetmsgFile
    baseLen=`wc -l $MERGE_INPUT_DATA_FILE | awk  '{ print $1 }'`
    targetLen=`wc -l $targetmsgFile | awk  '{ print $1 }'`
    if [ $baseLen -ne $targetLen ]
    then
        #alert 1 "$0-Base data length is not equal to target msg length! "
        PRINT_LOG "$0-Base data length is not equal to target msg length! "
    fi

    base_target_merge $MERGE_INPUT_DATA_FILE $targetmsgFile newbase.tmp
    rm $targetmsgFile

    local outputFile=${MERGE_INCR_OUTPUT_PATH}${MERGE_OUTPUT_FILE}
    base_flash_merge newbase.tmp $MERGE_INPUT_FLASH_MSG_FILE $outputFile

    if [ -s $outputFile ]
    then
        #write donelist
        local nowTime=`date  +'%Y-%m-%d %H:%M:%S'`
        echo -e "${outputFile}\t${nowTime}" >> ${MERGE_INCR_OUTPUT_PATH}${MERGE_OUTPUT_DONELIST}
    else
        alert 1 "$0-incr data merge file is empty! "
    fi

    rm newbase.tmp
    return 0
}

function full_merge()
{
    cd ${MERGE_INPUT_FULL_PATH}
    backup_file  $MERGE_INPUT_DATA_FILE
    backup_file $MERGE_INPUT_TAGET_MSG_FILE
    backup_file $MERGE_INPUT_FLASH_MSG_FILE
    
    #fetch base, flash, taegeturl
    get_remote_file ${REMOTE_FULLDATA_SERVER_URL}${REMOTE_FULLDATA_PATH} ${REMOTE_FULLDATA_FILE} ${MERGE_INPUT_DATA_FILE}
    get_remote_file ${REMOTE_FULLTARGET_MSG_SERVER_URL}${REMOTE_FULLTARGET_MSG_PATH} ${REMOTE_FULLTARGET_MSG_FILE} \
        ${MERGE_INPUT_TAGET_MSG_FILE}
    get_remote_file ${REMOTE_FLASH_MSG_SERVER_URL}${REMOTE_FLASH_MSG_PATH} ${REMOTE_FLASH_MSG_FILE}.full \
    	${MERGE_INPUT_FLASH_MSG_FILE}
    
    local targetmsgFile=targetmsg.data
    deal_target_msg $MERGE_INPUT_TAGET_MSG_FILE $targetmsgFile
    baseLen=`wc -l $MERGE_INPUT_DATA_FILE | awk  '{ print $1 }'`
    targetLen=`wc -l $targetmsgFile | awk  '{ print $1 }'`
    if [ $baseLen -ne $targetLen ]
    then
        #alert 1 "$0-Base data length is not equal to target msg length! "
        PRINT_LOG "$0-full export: Base data length is not equal to target msg length! "
    fi

    #base_target_merge ${MERGE_INPUT_DATA_FILE} ${targetmsgFile} newbase.tmp
    #awk -F"\t" 'ARGIND==1{target[$1]=$2}ARGIND==2{tv="-"; if($1 in target ){tv=target[$1]} print $1"\t"$4","$5","$6"\t"tv"\t"$3}' $targetmsgFile ${MERGE_INPUT_DATA_FILE} > newbase.tmp
    #rm $targetmsgFile

    #local outputFile=${MERGE_FULL_OUTPUT_PATH}${MERGE_OUTPUT_FILE}
    #no flash, use "-"
    #awk -F"\t" '{print $1"\t"$2"\t"$3"\t-\t"$4}' newbase.tmp > $outputFile

	base_target_merge $MERGE_INPUT_DATA_FILE $targetmsgFile newbase.tmp
    rm $targetmsgFile

    local outputFile=${MERGE_FULL_OUTPUT_PATH}${MERGE_OUTPUT_FILE}
    base_flash_merge newbase.tmp $MERGE_INPUT_FLASH_MSG_FILE $outputFile
    
    if [ -s $outputFile ]
    then
        #write donelist
        local nowTime=`date  +'%Y-%m-%d %H:%M:%S'`
        echo -e "${outputFile}\t${nowTime}" >> ${MERGE_FULL_OUTPUT_PATH}${MERGE_OUTPUT_DONELIST}
    else
        alert 1 "$0-full data merge file is empty! "
    fi

    rm newbase.tmp
    return 0
}

function PRINT_LOG()
{
    local logTime=`date +%Y%m%d-%H:%M:%S`
    echo "[${logTime}]$0-$1" >> $MERGE_LOG_PATH
}
        
check_path
if [ $? -ne 0 ]
then
    PRINT_LOG "Fail to check path!"
    return 1
fi

if [ $# -eq 0 ]
then
    incr_merge
else
    full_merge
fi

exit $?
