#!/bin/sh
#给有效用户创建全站人群，被build_people_relation_main.sh调用

#CONF_SH="/home/work/.bash_profile"
#[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=./alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/db.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/bulidUserRelation.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "


DATE_NOW=`date +"%Y%m%d"`
BULID_USER_RELATION_OUTPUT=${DATA_PATH}${BULID_USER_RELATION_OUTPUT}

LOG_PATH=${LOG_PATH}${BULID_USER_RELATION_LOG_PATH}
LOG_FILE=${LOG_PATH}"/"${BULID_USER_RELATION_LOG_NAME}${DATE_NOW}
function initPath()
{
	#主域列表
	USERDOMAIN_DOMAIN_DB=${BULID_USER_RELATION_OUTPUT}"/"$1$1"/domain_file_sql"
	USERDOMAIN_FILE=${BULID_USER_RELATION_OUTPUT}"/"$1$1"/"${USERDOMAIN_FILE}

	#临时值存贮
	TMPPIDFILE=${BULID_USER_RELATION_OUTPUT}"/"$1$1"/"${TMP_PID_FILE_NAME}
	TMPURLFILE=${BULID_USER_RELATION_OUTPUT}"/"$1$1"/"${TMP_URL_FILE_NAME}
	TMP_IS_BULIDPEOPLE_FILE=${BULID_USER_RELATION_OUTPUT}"/"$1$1"/"${TMP_IS_BULID_FILE_NAME}
	
	#处理失败数据
	FAIL_USERID_DATA=${BULID_USER_RELATION_OUTPUT}"/"${FAIL_USERID_FILE_NAME}
	
	#回滚sql
	ROLLBACK_PEOPLE_DATA=${BULID_USER_RELATION_OUTPUT}"/"$1$1"/"${ROLLBACK_PEOPLE_FILE_NAME}
	ROLLBACK_VTURL_DATA=${BULID_USER_RELATION_OUTPUT}"/"$1$1"/"${ROLLBACK_VTURL_FILE_NAME}
	INSERT_RESULT=${BULID_USER_RELATION_OUTPUT}"/"$1$1"/tmp_insert_result"

	mkdir -p ${BULID_USER_RELATION_OUTPUT}"/"$1$1
	mkdir -p "${LOG_PATH}"
}


function PRINT_LOG()
{
	timeNow=`date +%Y%m%d-%H:%M:%S`
	echo "[${timeNow}]${1}" >> ${LOG_FILE}
}

#获取主域信息
function getDomainByUserId()
{
	if [ $# -ne 1 ]; then
		PRINT_LOG "[error userid] please input userid for param 1"
		return 1
	fi
	if [ -z "$1" ]; then
		PRINT_LOG "[error userid] userid can not null"
		return 1
	fi
	>$USERDOMAIN_FILE
	local sql="select RTRIM(website) from SF_User.userinfo where userid=$1"
	runsql_cap_read "$sql" "${USERDOMAIN_DOMAIN_DB}"
	
	if [ $? -ne 0 ]; then
		PRINT_LOG "[error query sql] query SF_User.userinfo table fail [sq] $sql"
		return 1
	fi
	
	LANG=c awk  '
		BEGIN{FS=OFS="\t"}
		{
			url=$1;
			if(length(url) != 0){
				if(substr(url,1,7)=="http://")
				{
					url=substr(url,8);
				}
				if(substr(url,length(url),1)=="/"){
					tempUrl=substr(url,1,length(url)-1);
					url=tempUrl;
				}
				else{
					tempUrl=url;
				}
				if(tempUrl ~ /[/]/){
					retUrl=url;
				}
				else
				{
					if(match(url,/[^.]+\.(com\.cn|com|net\.cn|net|org\.cn|org|gov\.cn|gov|gov\.cn|gov|cn|mobi|me|info|name|biz|cc|tv|asia|hk|tw|com\.tw|edu\.cn|com\.hk|ccoo\.cn)$/)){
						domain=substr(url,RSTART,RLENGTH);
					}
					else{
						#domain=url;
						print url"/*";
						next;
					}
					if(domain==tempUrl||"www."domain==tempUrl){
						retUrl="*."domain"/*";
					}
					else
					{
						retUrl=url"/*";
					}
				
				}
				print retUrl
			}
		}
	' ${USERDOMAIN_DOMAIN_DB} > ${USERDOMAIN_FILE}

	sed -i 's/\\/\\\\\\\\/g' ${USERDOMAIN_FILE}
	
}

#插入用户信息到vtpeople表和对应的url表
function insert2PeopleAndUrl()
{
	>$TMPPIDFILE
	while read userIdLine
	do
		if [ -n "$userIdLine" ];then
			local userid=`echo $userIdLine | awk '{print $1}'`
			local shardingSlice=`getUserSlice "$userid"`
			isBulidPeople "$userid" "$shardingSlice"
			if [ $? -ne 0 ];then
				echo -e "$userid" >> $FAIL_USERID_DATA
				PRINT_LOG "[error] query table beidou.vtpeople fail"
			fi
			
			
			#生成数据库的主键ID
			local getPidFromSeq="select beidou.get_next_value('userpack')"
			runsql_single_file "$getPidFromSeq" "$shardingSlice" "$TMPPIDFILE" #只能读主库
				if [ $? -ne 0 ]; then
					echo -e "$userid" >> $FAIL_USERID_DATA
					PRINT_LOG "[error] query beidou.get_next_value seq fail sql : $getPidFromSeq"
				fi
				
			local primary_id=`cat $TMPPIDFILE | head -n1 | awk '{print $1}'`
			#########################
		
			local pid=`cat $TMP_IS_BULIDPEOPLE_FILE | head -n1 | awk '{print $1}'`
			
			if [ -z "$pid" ];then
			    ############这里调用DMP的人群ID服务，获取分配的人群ID
				local idResponsJson=`curl -s "http://id.beidou.baidu.com/sequence/query?type=dmpGroupId&num=1"`
				local status=`echo $idResponsJson | awk -F'"status":' '{print $2}' | awk -F',' '{print $1}'`
				if [ $status -ne 0 ]; then
					PRINT_LOG "[error] query sequence fail,return status=$status"
					return 1
				else
					local pid=`echo $idResponsJson | awk -F'\\[' '{print $2}' | awk -F'\\]' '{print $1}'`
				fi	
				
				######################################################
				
				local sql="insert into beidou.vtpeople(id,pid,name,alivedays,cookienum,userid,type,activetime,addtime,modtime,adduser,moduser) values($primary_id,$pid,'全站人群',30,0,$userid,5,now(),now(),now(),$userid,$userid)"
				runsql_single "$sql" "$shardingSlice" 
			
				if [ $? -ne 0 ]; then
					echo -e "$userid" >> $FAIL_USERID_DATA
					PRINT_LOG "[error] query beidou.vtpeople table fail sql : $sql"
				else

					#echo "delete from beidou.vtpeople where pid = ${pid};" >> ${ROLLBACK_PEOPLE_DATA}

					operate_domain "$userid" "$pid" "$shardingSlice"
				fi
			else
				operate_domain "$userid" "$pid" "$shardingSlice"
			fi
		fi
#限速
		sleep 0
	done < $1
	
}

function operate_domain()
{
	local userid=$1
	local pid=$2
	local shardingSlice=$3
	getDomainByUserId "$userid" 
	if [ $? -ne 0 ]; then
		#deletePeople "$pid" "$userid"
		echo -e "$userid" >> $FAIL_USERID_DATA
    	PRINT_LOG "[error] query domain table fail"
	fi


	while read line
	do
		
    	if  [ ! -z "$line" ];then
			
			isBuildPidUrl "$pid" "${line}" "$userid" "$shardingSlice"
			if [ $? -ne 0 ];then
				#deletePeople "$pid" "$userid"
				echo -e "$userid" >> $FAIL_USERID_DATA
				PRINT_LOG "[error] query vturl fail"
			fi
			local pidurl=`cat $TMPURLFILE | head -n1 | awk '{print $1}'`
			if [ -z "$pidurl" ];then
				local insertVturlSql="insert into beidou.vturl(pid,url,userid) values($pid,'${line}',$userid)"
				runsql_single_file "$insertVturlSql" "$shardingSlice" "$INSERT_RESULT"
				#如果插入url失败，则必须回滚vtpeople表对应记录
				if [ $? -ne 0 ]; then
					#deletePeople "$pid" "$userid"
					#deleteUrlByPidAndUsreid "$pid" "$userid"
					echo -e "$userid" >> $FAIL_USERID_DATA
					PRINT_LOG "[error] insert beidou.vturl table fail [sql] $insertVturlSql"
				#else
					#echo "delete from beidou.vturl where pid=$pid and url='${line}' and userid=${userid};" >> ${ROLLBACK_VTURL_DATA}
				fi
			fi
		fi
	done < $USERDOMAIN_FILE
}

#判断用户是否已经关联上默认人群,传入userid
function isBulidPeople()
{
	> $TMP_IS_BULIDPEOPLE_FILE
	if [ $# -ne  2 ];then
		PRINT_LOG "the param userid can not null"
		return 1
	fi
    local userid=$1
	local shardingSlice=$2
	local sqlPeople="select pid from beidou.vtpeople where userid=$userid and type=5"
	runsql_single_read "$sqlPeople" "$TMP_IS_BULIDPEOPLE_FILE" "$shardingSlice"

	if [ $? -ne 0 ];then
		PRINT_LOG "[error] query beidou.vtpeople fail [sql] $sqlPeople"
		return 1
	fi
}


#判断pid对应的url是否已经插入 
function isBuildPidUrl()
{
	>$TMPURLFILE
	if [ $# -ne 4 ];then
		PRINT_LOG "[error] the function need three params"
	fi
	
	if [ -z "$1" ];then
		PRINT_LOG "[error] the param pid is null"
	fi
	local pid=$1
	
	if [ -z "$2" ];then
		PRINT_LOG "[error] the param url is null"
	fi
	local url=$2
	
	if [ -z $3 ];then
		PRINT_LOG "[error] the param userid is null"
	fi
	local userid=$3
	local shardingSlice=$4
	local urlsql="select pid from beidou.vturl where pid=$pid and url='${url}' and userid=$userid"
	 
	runsql_single_read "$urlsql"  "$TMPURLFILE" "$shardingSlice"
	if [ $? -ne 0 ];then
		PRINT_LOG "[error] query beidou.vturl fail [sql] $urlsql"
		return 1
	fi
}

#回滚删除vtpeople表对应记录
function deletePeople()
{
	if [ $# -ne 2 ];then
		PRINT_LOG "[error] the function need two params"
		return 1
	fi
	
	local pid=$1
	local userid=$2
	local shardingSlice=`getUserSlice "$userid"`
	local deleteSql="delete from beidou.vtpeople where pid=$pid and userid=$userid and type=5" 
    runsql_single "$deleteSql" "$shardingSlice"
    
    if [ $? -ne 0 ]; then
    	PRINT_LOG "[error] delete people fail [sql] $deleteSql"
		return 1
    fi
}
#当有一个url插入失败时，回滚所有已经插入的url数据
function deleteUrlByPidAndUsreid()
{
	if [ $# -ne 2 ];then
		PRINT_LOG "[error] the function need two params"
		return 1
	fi

	local pid=$1
	local userid=$2
	local shardingSlice=`getUserSlice "$userid"`
	local deleteSql="delete from beidou.vturl where pid=$pid and userid=$userid"

	runsql_single "$deleteSql" "$shardingSlice"
	if [ $? -ne 0 ]; then
	    PRINT_LOG "[error] delete vturl fail [sql] $deleteSql"
		return 1
	fi
}

start_time=`date +"%F %T"`
echo "[${start_time}]=====================================start=============="
initPath "$1"
insert2PeopleAndUrl "$BULID_USER_RELATION_OUTPUT"/"$1"
end_time=`date +"%F %T"`
echo "[${end_time}]========================================end==============="
