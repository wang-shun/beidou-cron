#!/bin/bash

#@file:importTooManyKeywordsUser.sh
#@author:wangxiongjie
#@date:2012-09-19
#@version:1.0.0.0
#@brief:import user information who have more than 200W keywords to db


CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/importTooManyKeywordsUser.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importTooManyKeywordsUser.sh
reader_list=wangxiongjie

LOG_PATH=${LOG_PATH}/${DIR_NAME}
LOG_FILE=${LOG_PATH}/importTooManyKeywordsUser.log
DATA_PATH=${DATA_PATH}/${DIR_NAME}
TEMP_FILE=${DATA_PATH}/temp

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

[ ! -f ${LOG_FILE} ] && touch ${LOG_FILE}



function clean_data_file()
{
	if [ -f ${DATA_PATH}/${TMP_DB_RESULT} ]
	then
       rm ${DATA_PATH}/${TMP_DB_RESULT}
    fi
	
	if [ -f ${DATA_PATH}/${CPROKEYWORD_USER} ]
	then
       rm ${DATA_PATH}/${CPROKEYWORD_USER}
    fi
	
	if [ -f ${DATA_PATH}/${WORD_PACK_USER} ]
	then
       rm ${DATA_PATH}/${WORD_PACK_USER}
    fi
	
	if [ -f ${DATA_PATH}/${MERGED_USER} ]
	then
       rm ${DATA_PATH}/${MERGED_USER}
    fi
}

#导出cprokeyword${1}表中关键词个数大于${2}的用户id和关键词数量信息
# arguments 1: subtable's number 0 - 63
# arguments 2: keyword number limit 100W
# arguments 3: the file path ouputed
function export_onetable_cprokeyword_user()
{
	SELECT_SQL="SELECT userid, COUNT(keywordid) AS keywordnum FROM beidou.cprokeyword${1} where [userid] GROUP BY userid HAVING keywordnum>${2} ;"
	runsql_sharding_read "${SELECT_SQL}" "${DATA_PATH}/${TMP_DB_RESULT}"
	cat ${DATA_PATH}/${TMP_DB_RESULT} >> ${3}
	echo "finished cprokeyword${1}" >> ${LOG_FILE}
	return 0
}

#导出所有cprokeyword表中关键词个数大于${1}的用户id和关键词数量信息
# arguments 1: keyword number limit 100W
# arguments 2: the file path ouputed
function export_all_cprokeyword_user()
{
	TABLE_INDEX=0
	while(( ${TABLE_INDEX}<${CPROKEYWORD_TABLE_NUM} ))
	do
		export_onetable_cprokeyword_user ${TABLE_INDEX} ${1} ${2}
		if [ $? -ne 0 ]
		then
			return 1
		fi
		let TABLE_INDEX=TABLE_INDEX+1
	done
	return 0
}

#导出word_pack_keyword表中关键词个数大于${1}的用户id和关键词数量信息
# arguments 1: keyword number limit 100W
# arguments 2: the file path ouputed
function export_onetable_packkeyword_user()
{
	SELECT_SQL="SELECT userid, COUNT(keywordid) AS keywordnum FROM beidou.word_pack_keyword where [userid] GROUP BY userid HAVING keywordnum>${1} ;"
	runsql_sharding_read "${SELECT_SQL}" "${DATA_PATH}/${TMP_DB_RESULT}"
	cat ${DATA_PATH}/${TMP_DB_RESULT} >> ${2}
	echo "finished word_pack_keyword" >> ${LOG_FILE}
	return 0
}

#导出所有word_pack_keyword表中关键词个数大于${1}的用户id和关键词数量信息
# arguments 1: keyword number limit 100W
# arguments 2: the file path ouputed
function export_all_packkeyword_user()
{
	export_onetable_packkeyword_user ${1} ${2}
	if [ $? -ne 0 ]
	then
		return 1
	fi
	return 0
}

#根据userid查询此用户在word_pack表中的词量,并计算出总词量写入结果文件
# arguments 1: userid
# arguments 2: keyword number from cprokeyword belongs to this user
# arguments 3: the file path ouputed
function query_keyword_num_from_word_pack()
{
	SELECT_SQL="SELECT COUNT(keywordid) AS keywordnum FROM beidou.word_pack_keyword WHERE [userid] and userid=${1} ;"
	slice=`getUserSlice ${1}`
	>${TEMP_FILE};
	runsql_single_read "${SELECT_SQL}" "${TEMP_FILE}" ${slice}
	keywordNum=$(head -1 ${TEMP_FILE})
	keywordNum=$((${keywordNum}+${2}))
	if [ ${keywordNum} -gt ${KEYWORD_NUM_LIMIT} ]
	then
		echo "${1} ${keywordNum}">>${3}
	fi
	echo "finished user ${1} from word_pack_keyword" >> ${LOG_FILE}
	return 0
	
}

#根据userid查询此用户在cprokeyword表中的词量,并计算总词量写入结果文件
# arguments 1: userid
# arguments 2: keyword number from word_pack_keyword belongs to this user
# arguments 3: the file path ouputed
function query_keyword_num_from_cprokeyword()
{
	tableNum=$((${1}%${CPROKEYWORD_TABLE_NUM}))
	SELECT_SQL="SELECT COUNT(keywordid) AS keywordnum FROM beidou.cprokeyword${tableNum} WHERE [userid] and userid=${1} ;"
	slice=`getUserSlice ${1}`
	>${TEMP_FILE};
	runsql_single_read "${SELECT_SQL}" "${TEMP_FILE}" ${slice}
	keywordNum=$(head -1 ${TEMP_FILE})
	keywordNum=$((${keywordNum}+${2}))
	if [ ${keywordNum} -gt ${KEYWORD_NUM_LIMIT} ]
	then
		echo "${1} ${keywordNum}">>${3}
	fi
	echo "finished user ${1} from cprokeyword${tableNum}" >> ${LOG_FILE}
	return 0
}

#根据userid和keyword number 的信息写入数据库user_keyword_limit表中
# arguments 1: userid
# arguments 2: keyword number belongs to this user
function insert_user_keyword_num()
{
	modtime=$(date "+%Y-%m-%d %H:%M:%S")
	UPDATE_SQL="insert into beidoureport.user_keyword_limit(userid,keyword_num,num_limit,modtime) values(${1},${2},NULL,'${modtime}') on duplicate key update user_state=if(keyword_num<${2},1,0), keyword_num=${2}, modtime='${modtime}';"
	runsql_xdb "${UPDATE_SQL}"
	return 0;
}


function mainFunction()
{
	#找出cprokeyword 表中词量大于100W的用户,存入到文件中
	echo "find data from table cprokeyword start..." >> ${LOG_FILE}
	export_all_cprokeyword_user $((${KEYWORD_NUM_LIMIT}/2)) ${DATA_PATH}/${CPROKEYWORD_USER}
	if [ $? -ne 0 ]
	then
		echo "find data from table cprokeyword failed" >> ${LOG_FILE}
		return 1
	fi
	echo "find data from table cprokeyword end..." >> ${LOG_FILE}
	
	#找出word_pack_keyword 表中词量大于100W的用户,存入到文件中
	echo "find data from table word_pack_keyword start..." >> ${LOG_FILE}
	export_all_packkeyword_user $((${KEYWORD_NUM_LIMIT}/2)) ${DATA_PATH}/${WORD_PACK_USER}
	if [ $? -ne 0 ]
	then
		echo "find data from table word_pack_keyword failed" >> ${LOG_FILE}
		return 1
	fi
	echo "find data from table word_pack_keyword end..." >> ${LOG_FILE}
	
	#查出word_pack_keyword表中数据量大于100W的用户的全部词量,并存入最终文件中
	echo "deal with user in table word_pack_keyword start..." >> ${LOG_FILE}
	while read line
	do
		line_arr=(${line})
		if [ ${#line_arr[@]} -eq 2 ]
		then
			query_keyword_num_from_cprokeyword ${line_arr[0]} ${line_arr[1]} ${DATA_PATH}/${MERGED_USER}
		fi
			
	done < ${DATA_PATH}/${WORD_PACK_USER}
	echo "deal with user in table word_pack_keyword end..." >> ${LOG_FILE}
	
	#查出cprokeyword表中数据量大于100W的用户的全部词量,并存入最终文件中
	echo "deal with user in table cprokeyword start..." >> ${LOG_FILE}
	while read line
	do
		line_arr=(${line})
		if [ ${#line_arr[@]} -eq 2 ]
		then
			query_keyword_num_from_word_pack ${line_arr[0]} ${line_arr[1]} ${DATA_PATH}/${MERGED_USER}
		fi
			
	done < ${DATA_PATH}/${CPROKEYWORD_USER}
	echo "deal with user in table cprokeyword end..." >> ${LOG_FILE}
	
	#从最终文件中找出词量大于200W的用户，导入到数据库中
	echo "insert  final user info into table user_keyword_limit start..." >> ${LOG_FILE}
	while read line
	do
		line_arr=(${line})
		if [ ${#line_arr[@]} -eq 2 ] && [ ${line_arr[1]} -gt ${KEYWORD_NUM_LIMIT} ]
		then
			insert_user_keyword_num ${line_arr[0]} ${line_arr[1]}
		fi
			
	done < ${DATA_PATH}/${MERGED_USER}
	echo "insert  final user info into table user_keyword_limit end..." >> ${LOG_FILE}
}

clean_data_file
alert $? "clean data file failed"

mainFunction
alert $? "import task failed"
echo "importTooManyKeywordsUser task finished successful."
