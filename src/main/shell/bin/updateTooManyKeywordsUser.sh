#!/bin/bash

#@file:updateTooManyKeywordsUser.sh
#@author:wangxiongjie
#@date:2012-09-19
#@version:1.0.0.0
#@brief:update user keywords number information who has more than 200W/450W keywords


CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/updateTooManyKeywordsUser.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=updateTooManyKeywordsUser.sh
reader_list=wangxiongjie

cprokeyword_num=0
pack_keyword_num=0

LOG_PATH=${LOG_PATH}/${DIR_NAME}
LOG_FILE=${LOG_PATH}/updateTooManyKeywordsUser.log
DATA_PATH=${DATA_PATH}/${DIR_NAME}-${1}
TEMP_FILE=${DATA_PATH}/temp

function usage()
{
    echo "You should use this shell like this: sh updateTooManyKeywordsUser.sh 2000000"
    echo "The number 2000000 is the keyword number that need concerned"
}
if [ $# -ne 1 ]
then
    usage
    exit 1
fi

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

[ ! -f ${LOG_FILE} ] && touch ${LOG_FILE}


function clean_data_file()
{
     >${DATA_PATH}/${USERID_LIST}

     >${DATA_PATH}/${USERID_KEYWOED_NUM_LIST}
}

# 查输入的要更新的用户的关键词个数下线,这个地方可以�?200W�?1天运行一次的任务；也可以�?450W，半小时任务的时候的参数
function check_input_params()
{
	if [ $# -ne 1 ]
	then
		echo "need one input for user keywords limit, like 2000000 or 4500000" >> ${LOG_FILE}
		return 1
	elif [ -n "${1}" -a -z "${1//[0-9]/}" ]  #这个地方判断输入的如果全是数�?
    then
        KEYWORD_NUM_LIMIT=${1}
		return 0
	else
		echo "need one input a NUMBER for user keywords limit, like 2000000 or 4500000" >> ${LOG_FILE}
		return 1
	fi
}


#导出user_keyword_limit表中�?有关键词个数超过特定限度的用�?
# arguments 1: the limit number,like 200W
# arguments 2: the file path ouputed
function export_need_count_user()
{
	SELECT_SQL="SELECT userid, keyword_num FROM beidoureport.user_keyword_limit WHERE keyword_num>=${1} ;"
	runsql_xdb_read "${SELECT_SQL}"  "${2}"
	return 0
}

#计算用户的cprokeyword的个�?,结果存在全局变量cprokeyword_num�?
# arguments 1: userid
function count_user_cprokeyword()
{
	tableNum=$((${1}%${CPROKEYWORD_TABLE_NUM}))
	SELECT_SQL="SELECT COUNT(keywordid) AS keywordnum FROM beidou.cprokeyword${tableNum} WHERE [userid] and userid=${1} ;"
	#keywordNum=$(${DB_CLIENT} -u${DB_USER_READ_BEIDOU} -h${DB_URL_READ_BEIDOU} -P${DB_PORT_READ_BEIDOU}  -p${DB_PWD_READ_BEIDOU} ${DB_NAME_READ_BEIDOU} --default-character-set=GBK --skip-column-names -e "${SELECT_SQL}" )
	slice=`getUserSlice ${1}`
	>${TEMP_FILE};
	runsql_single_read "${SELECT_SQL}" "${TEMP_FILE}" ${slice}
	keywordNum=$(head -1 ${TEMP_FILE})
	cprokeyword_num=${keywordNum}
	return 0
}

#计算用户的packkeyword的个�?,结果存在全局变量pack_keyword_num�?
# arguments 1: userid
function count_user_pack_keyword()
{
	SELECT_SQL="SELECT COUNT(keywordid) AS keywordnum FROM beidou.word_pack_keyword WHERE [userid] and userid=${1} ;"
	slice=`getUserSlice ${1}`
	>${TEMP_FILE};
	runsql_single_read "${SELECT_SQL}" "${TEMP_FILE}" ${slice}
	keywordNum=$(head -1 ${TEMP_FILE})
	pack_keyword_num=${keywordNum}
	return 0
}

#查询并计算出${1}文件中所有userid对应的用户的关键词数�?
# arguments 1: the file path inputed
# arguments 2: the file path outputed
function count_user_total_keyword()
{
	while read line
	do
		line_arr=(${line})
		if [ ${#line_arr[@]} -eq 2 ]
		then
			count_user_cprokeyword ${line_arr[0]}
			if [ $? -ne 0 ]
			then
				continue
			fi
			count_user_pack_keyword ${line_arr[0]}
			if [ $? -ne 0 ]
			then
				continue
			fi
			keywordNum=$((cprokeyword_num+pack_keyword_num))
			echo "${line_arr[0]} ${keywordNum}">>${2}
		fi
			
	done < ${1}
}

#更新用户的关键词个数
# arguments 1: userid
# arguments 2: keyword number belongs to this user
function update_user_keyword_num()
{
	modtime=$(date "+%Y-%m-%d %H:%M:%S")
	UPDATE_SQL="update beidoureport.user_keyword_limit set user_state=if(keyword_num<${2},1,0), keyword_num=${2}, modtime='${modtime}' where userid=${1};"
	runsql_xdb "${UPDATE_SQL}"
	return 0
}


function mainFunction()
{
	#找出user_keyword_limit表中已有关键词个数超�?200W的用�?,存入到文件中
	echo "find keyword num bigger than limit in user_keyword_limit start..." >> ${LOG_FILE}
	export_need_count_user ${KEYWORD_NUM_LIMIT} ${DATA_PATH}/${USERID_LIST}
	if [ $? -ne 0 ]
	then
		echo "find keyword num bigger than limit in user_keyword_limit failed..." >> ${LOG_FILE}
		return 1
	fi
	echo "find keyword num bigger than limit in user_keyword_limit end..." >> ${LOG_FILE}
	
	#实际计算导出用户列表中的用户的当前关键词数量，并导入到新文件�?
	echo "find keyword num start..." >> ${LOG_FILE}
	count_user_total_keyword ${DATA_PATH}/${USERID_LIST} ${DATA_PATH}/${USERID_KEYWOED_NUM_LIST}
	if [ $? -ne 0 ]
	then
		echo "find keyword num failed..." >> ${LOG_FILE}
		return 1
	fi
	echo "find keyword num end..." >> ${LOG_FILE}
	
	#从文件中逐行读取userid和keywordNum,更新user_keyword_limit表中的用户关键词数量，并同时更新modtime和user_stat
	echo "update keyword num to db start..." >> ${LOG_FILE}
	while read line
	do
		line_arr=(${line})
		if [ ${#line_arr[@]} -eq 2 ]
		then
			update_user_keyword_num ${line_arr[0]} ${line_arr[1]}
			echo "update keyword num to db done ${line}..." >> ${LOG_FILE}
		fi
			
	done < ${DATA_PATH}/${USERID_KEYWOED_NUM_LIST}
	echo "update keyword num to db end..." >> ${LOG_FILE}
}


check_input_params ${1}
alert $? "Input paramter Check Error"

clean_data_file

mainFunction
alert $? "update task failed"

echo "updateTooManyKeywordsUser task finished successful."
