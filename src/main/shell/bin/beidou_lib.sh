#!/bin/bash

#@file: beidou_lib.sh
#@author: yanjie
#@date: 2009-04-10
#@version: 1.0.0.0
#@brief: based on dr_odp_lib.sh

#local misc function
function GetLogDate() {
	local time_now=$(date +%F" "%T)
	echo -n "[${time_now}]"
}

#无参数
function open_log()
{
	if [ -z $DEBUG_MOD ]
	then
		DEBUG_MOD=0
	fi

	if ! [ -d $LOG_PATH ]
	then
		LOG_PATH="."
	fi


	if [ -z $LOG_NAME ]
	then
		LOG_NAME=$$
	fi

	LOG_FILEPATH="$LOG_PATH"/"$LOG_NAME"".log"

	echo "$(GetLogDate):  $LOG_NAME * $$ Open process log by----$LOG_NAME" >> $LOG_FILEPATH && \
	echo "================================================" >> $LOG_FILEPATH && \
	echo "$(GetLogDate):  $LOG_NAME * $$ Open process log by----$LOG_NAME" >> $LOG_FILEPATH".wf" && \
	echo "================================================" >> $LOG_FILEPATH".wf" && \

	if [ $? -ne "0" ]
	then
		echo "WARNING!!! Fail to open log $LOG_FILEPATH!"
	fi

	return 0
}

#$1 退出状态
function close_log()
{
	LOG_FILEPATH="$LOG_PATH"/"$LOG_NAME"".log"
	if [ $1 -eq "0" ]
	then
		end_string="Normally end process"
	else
		end_string="Abnormally end process"
	fi
	echo "$(GetLogDate):  $LOG_NAME * $$ < - > $end_string" >> $LOG_FILEPATH
	echo "================================================" >> $LOG_FILEPATH

	echo "$(GetLogDate):  $LOG_NAME * $$ < - > $end_string" >> $LOG_FILEPATH".wf"
	echo "================================================" >> $LOG_FILEPATH".wf"

	return 0
}

#$1 日志级别: DEBUG, TRACE, NOTICE, WARNING, FATAL
#$2 日志信息
function log()
{
	if [ $DEBUG_MOD -eq 1 ]
	then
		echo "$@"
		return 0
	fi

	LOG_FILEPATH="$LOG_PATH"/"$LOG_NAME"".log"
	if [ -e $LOG_FILEPATH ]
	then
		log_size=`ls -l $LOG_FILEPATH | awk '{ print $5}'`
		if [[ $logsize -gt $LOG_SIZE ]]
		then
			old_log_path=$LOG_FILEPATH"."`date +%Y%m%d%H%m` && \
			mv -f $LOG_FILElPATH  $old_log_path
			if [ $? -ne "0" ]
			then
				echo "WARNING!!: $(GetLogDate):  $LOG_NAME * $$ Fail to cut log $LOG_FILEPATH, it is big than $LOG_SIZE!" >> $LOG_FILEPATH
				echo "WARNING!!: $(GetLogDate):  $LOG_NAME * $$ Fail to cut log $LOG_FILEPATH, it is big than $LOG_SIZE!" 1>&2
				return 1
			fi
		fi
	fi

	case $1 in
	"DEBUG"|"TRACE")
		if [ $LOG_LEVEL -le "4" ]
		then
			return 0;
		fi
	;;
	"FATAL"|"WARNING")
		LOG_FILEPATH="$LOG_FILEPATH"".wf"
	;;
	esac

#考虑$@
	echo "$1: $(GetLogDate):  $LOG_NAME * $$ $2" >> $LOG_FILEPATH

	return 0
}

#$1: mail message
#$2: email list
function SendMail() {
	local message=$1
	local mail_list=$2
	local title="${message}"
	local mail_message="$(GetLogDate) ${message}"
	echo "${mail_message}" | mail -s "${title}" "${mail_list}"
}

#$1: from
#$2: to
#$3: subject
#$4: mail body
function SendMailEx() {
	local from=${1}
	local to=${2}
	local subject=${3}
	local body=${4}
	local content_type="Content-type:text/plain;charset=gb2312"
	local mail_content="to:${to}\nfrom:${from}\nsubject:${subject}\n${content_type}\n${body}"
	echo -e ${mail_content} | /usr/sbin/sendmail -t
}

#mail with text attachment
#$1: mail_from
#$2: mail_to
#$3: subject
#$4: content mimetype, such as "text/plain"
#$5: content
#$6: attach mimetype, such as "text/csv"
#$7: attach display name
#$8: attach file path
function SendMailAttach(){
	local MSG_FILE="/tmp/mail.tmp"

	echo "From: $1" > $MSG_FILE
	echo "To: $2" >> $MSG_FILE
	echo "Subject: $3" >> $MSG_FILE
	echo "Mime-Version: 1.0" >> $MSG_FILE
	echo 'Content-Type: multipart/mixed; boundary="GvXjxJ+pjyke8COw"' >> $MSG_FILE
	echo "Content-Disposition: inline" >> $MSG_FILE
	echo "" >> $MSG_FILE
	echo "--GvXjxJ+pjyke8COw" >> $MSG_FILE
	echo "Content-Type: $4" >> $MSG_FILE
	echo "Content-Disposition: inline" >> $MSG_FILE
	echo "" >> $MSG_FILE
	echo "$5" >> $MSG_FILE
	echo "" >> $MSG_FILE
	echo "" >> $MSG_FILE
	echo "--GvXjxJ+pjyke8COw" >> $MSG_FILE
	echo "Content-Type: $6" >> $MSG_FILE
	echo "Content-Disposition: attachement; filename=$7" >> $MSG_FILE
	echo "" >> $MSG_FILE
	echo "" >> $MSG_FILE
	cat $8 >> $MSG_FILE

	cat $MSG_FILE | /usr/lib/sendmail -t
}

#$1: message
#$2: mobile list
function SendMessage() {
	local local_host_name=$(hostname | sed 's/.baidu.com//')
	local message=$1
	local mobile_list=$2
	local mobile_message="${local_host_name} ${message} $(GetLogDate)"
	for mobile in ${mobile_list}
	do
		gsmsend -s emp01.baidu.com:15003 -s emp02.baidu.com:15003 ${mobile}@"${mobile_message}"
	done
}

##############################################################################################################
#author                dongying
#
#$1:getfile_operation  if use wget to get file, must use -O option o avoid aotu rename for download local file
#$2:clear_operation    if you have no clear operation but want to set retry_count, please set this param=""
#$3:retry_count        retry times, default is 3
##############################################################################################################
function getfile(){
#init parms
if [[ -z $1 ]]; then
    echo "[error used] please input getfile_operation param like :wget -q -c ftp://****/****/filename -O localfilename"
    getfile_help
    return 1
else
    if [[ $1 == "help" ]] || [[ $1 == "?" ]]; then
        getfile_help
        return 1
    fi
    getfile_operation=$1
    echo $1|egrep '^wget.+' > /dev/null
    if [[ $? -eq 0 ]]; then
        echo $1|egrep '^wget.+-O.+' > /dev/null
        if [[ $? -ne 0 ]];then
            echo "[error used] when you use wget to get file ,please make sure you have use -O option to avoid aotu rename for download local file!!!"
            getfile_help
            return 1
        fi
    fi
fi

if [[ ! -z $2 ]]; then
    clear_operation=$2
fi
if [[ -z $3 ]]; then
    retry=3
else
    retry=$3
fi

#try to exec clear_operation and exec getfile_operation for retry_count times
cnt=0
while [[ $cnt -lt $retry ]]; do
    clear_flag=0

    #if has clear command, do it
    if [[ ! -z $clear_operation ]]; then
        eval $clear_operation
        if [[ $? -eq 0 ]]; then
            clear_flag=1
        fi      
    else    
        clear_flag=1
    fi      

    #if clear ok, do getfile_operation next
    if [[ $clear_flag -eq 1 ]]; then
        echo "[TRACE] clear ok, go to try $cnt time to do $getfile_operation" 
        $getfile_operation

        if [[ $? -eq 0 ]]; then
            return 0
        fi      
    else    
        echo "[TRACE] clear failed and have tried $cnt times" 
    fi      
    cnt=$(($cnt+1)) 
    sleep 5 
done    

return 1
}


#############################################################################################################################
##authoer          dongying
#
#$1:db_info        please input db_info like : 'mysql -h hostname -P portnum -u username -p password'
#
#$2:operation_sql  operation_sql should be simple sentense 
#                  if you have serveral sql operation, split them into several singel sql
#
#$3:clear_sql      if has no clear sql but want to set param 4/5/6, please set this param=""
#
#$4:export_file    if this is a export db operation, you must set this param to save the dbdata, recommand use all path way 
#
#$5:export_way     used with param 4, 0:new, 1:append to the file
#
#$6:retry_count    could be empty, default is 3 times
#############################################################################################################################
function db_retry_operation(){
#init parms
if [[ -z $1 ]]; then
    echo "[error used] please input db_info like : 'mysql -h hostname -P portnum -u username -p password'"
    db_retry_operation_help
    return 1
else    
    if [[ $1 == "help" ]] || [[ $1 == "?" ]]; then
        db_retry_operation_help
        return 1
    fi
    db_info=$1
fi
if [[ -z $2 ]]; then
    echo "[error used] please input operation_sql for param 1"
    db_retry_operation_help
    return 1
else    
    operation_sql=$2
fi

if [[ ! -z $3 ]]; then
    clear_sql=$3
fi 

if [[ ! -z $4 ]]; then
    export_file=$4
fi
#check weather operation_sql is a export opearation
echo $operation_sql|egrep '^select.+from.+' > /dev/null
if [[ $? -eq 0 ]] && [[ -z $export_file ]]; then
    echo "[error used] you want to do a exprot operation, please input export_file for the 4th params, recommand use all path way"
    db_retry_operation_help
    return 1    
fi

if [[ ! -z $5 ]]; then
    export_way=$5
fi
#check export_way and export_file are both set
if [[ ! -z $export_file ]] && [[ -z $export_way ]]; then
    echo "[error used] please input 0(new) or 1(appeand) for the 5th param to declare how to add the data to the export file!!"
    db_retry_operation_help
    return 1
fi


if [[ -z $6 ]]; then
    retry=3 
else    
    retry=$6
fi


#try to exec clear_sql and operation_sql for times
cnt=0   
while [[ $cnt -lt $retry ]]; do
    clear_flag=0

    #if has clear clear_sql, do it
    if [[ ! -z $clear_sql ]]; then
        $db_info -e"$clear_sql"
        if [[ $? -eq 0 ]]; then
            clear_flag=1
        fi      
    else    
        clear_flag=1
    fi      

    #if clear ok, do operation_sql next
    if [[ $clear_flag -eq 1 ]]; then
        echo "[TRACE] clear ok, go to try $cnt time to do $operation_sql"

        if [[ -z $export_file ]]; then
            echo "[TRACE] this is not an export db operation!!"
            $db_info -e "$operation_sql"
        else
            if [[ $export_way -eq 0 ]]; then
                echo "[TRACE] this is an export db operation with > way!!"
                $db_info -e "$operation_sql" > $export_file
            else
                echo "[TRACE] this is an export db operation with >> way!!"
                $db_info -e "$operation_sql" >> $export_file
            fi
        fi

        if [[ $? -eq 0 ]]; then
            return 0
        fi      
    else    
        echo "[TRACE] clear failed and have tried $cnt times"
    fi      
    cnt=$(($cnt+1)) 
    sleep 5 
done    

return 1
}

function getfile_help(){
    echo "=========================================================================================================="
    echo "getfile is used for getfile with auto retry mechanism"
    echo "it has 3 params, please read the following description clearly"
    echo "=========================================================================================================="
    echo "param 1 : getfile_operation   if use wget to get file, must use -O option o avoid aotu rename for download local file"
    echo "                              param demo: wget -q -c ftp://****/****/filename -O localfilename"
    echo "param 2 : clear_operation     if you have no clear operation but want to set retry_count, please set this param="""
    echo "                              param demo: cd /home/work/tmp&&rm test.txt"
    echo "param 3 : retry_count         retry times, could be empty, default is 3 times"
    echo "=========================================================================================================="
    echo 'getfile demo : getfile "getfile_operation" "clear_operation" "retry_count"'
    echo "=========================================================================================================="
}

function db_retry_operation_help(){
    echo "=========================================================================================================="
    echo "db_retry_operation is used for db operation with auto retry mechanism"
    echo "it has 6 params, please read the following description clearly"
    echo "=========================================================================================================="
    echo "param 1 : dbinfo         please input db_info like : 'mysql -h hostname -P portnum -u username -p password'"
    echo "param 2 : operation_sql  operation_sql should be simple sentense"
    echo "                         if you have serveral sql operation, split them into several singel sql"
    echo "param 3 : clear_sql      if has no clear sql but want to set param 4/5/6, please set this param="""
    echo "param 4 : export_file    if this is a export db operation, you must set this param to save the dbdata"
    echo "                         recommand use all path way"
    echo "param 5 : export_way     how to save the export data to file, 0 is new and 1 is append"
    echo "param 6 : retry_count    retry times, could be empty, default is 3 times"
    echo "=========================================================================================================="
    echo 'import demo :  db_retry_operation "dbinfo" "db_sql" "clear_sql" "" "" "retry_times"'
    echo 'export demo :  db_retry_operation "dbinfo" "db_sql" "clear_sql" "export_file" "exprot_way" "retry_times"'
    echo "=========================================================================================================="
}
