#!/bin/bash

#@file:updateUserKeywordsLimit.sh
#@author:wangxiongjie
#@date:2012-09-26
#@version:1.0.0.0
#@brief:update user's keywords up limit


CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/updateUserKeywordsLimit.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=updateUserKeywordsLimit.sh
reader_list=wangxiongjie

LOG_PATH=${LOG_PATH}/${DIR_NAME}
LOG_FILE=${LOG_PATH}/updateUserKeywordsLimit.log


function usage()
{
    echo "You should use this shell like this: sh updateUserKeywordsLimit.sh updateFileName"
    echo "The updateFileName is the full path fileName"
}
if [ $# -ne 1 ] || [ ! -f ${1} ]
then
    usage
    exit 1
fi

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

[ ! -f ${LOG_FILE} ] && touch ${LOG_FILE}


#�?查输入文件格式的合法�?
# arguments 1: input data file full path
function check_data_file()
{
	while read line
	do
		line_arr=(${line})
		if [ ${#line_arr[@]} -ne 2 ]
		then
            echo "file format is wrong, you should prepare the file content like this: 10000 20000" >> ${LOG_FILE}
            echo "10000 is userid of the user,20000 is the keyword limit number of this user" >> ${LOG_FILE}
            return 1
        fi
        if ! [ -n "${line_arr[0]}" -a -z "${line_arr[0]//[0-9]/}" ]
        then
	        echo "file format is wrong, you should prepare the file content like this: 10000 20000" >> ${LOG_FILE}
            echo "you get a no number userid:${line_arr[0]}" >> ${LOG_FILE}
            return 1
	    fi

        if ! [ -n "${line_arr[1]}" -a -z "${line_arr[1]//[0-9]/}" ]
        then
	        echo "file format is wrong, you should prepare the file content like this: 10000 20000" >> ${LOG_FILE}
            echo "you get a no number keyword limit:${line_arr[1]}" >> ${LOG_FILE}
            return 1
	    fi
    done < ${1}
			
}

#更新用户的关键词个数
# arguments 1: userid
# arguments 2: keyword limit number belongs to this user
function update_user_keyword_num()
{
	retryCount=0
	#modtime=$(date "+%Y-%m-%d %H:%M:%S")
	UPDATE_SQL="update beidoureport.user_keyword_limit set num_limit=if(${2}=0,NULL,${2}) where userid=${1};"
	while [[ $retryCount -lt $MAX_RETRY ]]
	do
		retryCount=$((${retryCount}+1))
		runsql_xdb "${UPDATE_SQL}"
		if [ $? -eq 0 ]
		then
			return 0
		else
			sleep 1
		fi
	done
	log "FATAL" "Fail to update user:${1}!"
	return 1
}

# arguments 1: input data file full path
function mainFunction()
{
	#从文件中逐行读取userid和keywordNumLimit,更新user_keyword_limit表中的用户关键词上限
	while read line
	do
		line_arr=(${line})
		if [ ${#line_arr[@]} -eq 2 ]
		then
			update_user_keyword_num ${line_arr[0]} ${line_arr[1]}
			echo "finished data ${line}" >> ${LOG_FILE}
		fi
			
	done < ${1}
	
}


check_data_file ${1}
alert $? "Input Data File Check Error"

mainFunction ${1}
alert $? "update task failed"

echo "updateUserKeywordsLimit task finished successful."
