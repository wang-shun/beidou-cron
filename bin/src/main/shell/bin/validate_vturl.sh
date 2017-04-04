#!/bin/sh
#增量处理主域变更

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

CONF_SH=../conf/validate_vturl.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

DATE_NOW=`date +"%Y%m%d"`
VALIDATE_VTURL=${DATA_PATH}${VALIDATE_VTURL}
LOG_PATH=${LOG_PATH}${VALIDATE_VTURL}
LOG_FILE=${LOG_PATH}"/"${VALIDATE_VTURL_LOG_NAME}${DATE_NOW}



#查询出所有的domain
USERDOMAIN_URL_FILE_INFO=${VALIDATE_VTURL}"/userdomain_url_from_user_info"
USERDOMAIN_URL_FILE=${VALIDATE_VTURL}"/"${USEDOMAIN_URL_FILE_NAME}
USERDOMAIN_URL_FILE_BAK=${VALIDATE_VTURL}"/userdomain_url_file_bak1"
USERDOMAIN_FILE_DB=${VALIDATE_VTURL}"/userdomian_in_sql"
USERDOMAIN_FILE=${VALIDATE_VTURL}"/"${USERDOMAIN_FILE_NAME}
INCREMENT_DATA=${VALIDATE_VTURL}"/"${INCREMENT_DATA_FILE_NAME}
TMP_PID_FILE=${VALIDATE_VTURL}"/"${TMP_PID_FILE_NAME}
TMP_URLID_FILE=${VALIDATE_VTURL}"/"${TMP_URLID_FILE_NAME}
ALIVE_USERID=${VALIDATE_VTURL}"/alive_userid"

INSERT_RESULT=${VALIDATE_VTURL}"/insert_result"

mkdir -p "${VALIDATE_VTURL}"
mkdir -p "${LOG_PATH}"



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
	if [ -z $1 ]; then
		PRINT_LOG "[error userid] userid can not null"
		return 1
	fi
	>${USERDOMAIN_FILE}
	>${USERDOMAIN_FILE_DB}
	local getdomainsql="select RTRIM(website) from SF_User.userinfo where userid=$1"
	
	runsql_cap_read "$getdomainsql" "$USERDOMAIN_FILE_DB"

																			
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
	' ${USERDOMAIN_FILE_DB} > ${USERDOMAIN_FILE}

	sed -i 's/\\/\\\\\\\\/g' ${USERDOMAIN_FILE}
																										
}
stat_time=`date +"%F %T"`
echo "[$stat_time]==================start========================="

sql="select userid,website from SF_User.userinfo"

runsql_cap_read "$sql" "$USERDOMAIN_URL_FILE_INFO"

        LANG=c awk  '
		BEGIN{FS=OFS="\t"}
		{
			url=$2;
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
						print $1,url"/*";
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
				print $1,retUrl
			}
		}
	' ${USERDOMAIN_URL_FILE_INFO} > ${USERDOMAIN_URL_FILE}

	sed -i 's/\\/\\\\\\\\/g' ${USERDOMAIN_URL_FILE}


sql="select u.userid,u.url from beidou.vturl u ,beidou.vtpeople p where u.pid=p.pid and u.userid=p.userid and p.type=5"

runsql_sharding_read "$sql" "$USERDOMAIN_URL_FILE_BAK"


if [ $? -ne 0 ];then
	PRINT_LOG "[error] query SF_User.userinfo fail [sql] $sql"
	exit
fi

#if [ ! -e "$USERDOMAIN_URL_FILE_BAK" ];then
#	mv $USERDOMAIN_URL_FILE $USERDOMAIN_URL_FILE_BAK
#	exit
#fi
runsql_cap_read "select userid from beidoucap.useraccount where ustate=0 and sfstattransfer =0  and ushifenstatid in (2,3,6)" "${ALIVE_USERID}"
awk '
	ARGIND==1{
		index1=$1$2
		old[index1]
	}
	ARGIND==2{
		userid[$1]
	}
	ARGIND==3{
		index2=$1$2
		if((!(index2 in old)) && ($1 in userid)){print $1}
	}

' $USERDOMAIN_URL_FILE_BAK $ALIVE_USERID $USERDOMAIN_URL_FILE | sort | uniq  > ${INCREMENT_DATA}

while read line
do
	userid=`echo $line | awk '{print $1}'`
	selectPidSql="select pid from beidou.vtpeople where userid=$userid and type=5"
	slice=`getUserSlice "$userid"`

	runsql_single_read "$selectPidSql"  "${TMP_PID_FILE}" "$slice"
	#runsql_single_file "$selectPidSql"  "$slice" "${TMP_PID_FILE}"
	
	if [ $? -ne 0 ];then
		PRINT_LOG "[error] query beidou.vtpeople fail [sql] $selectPidSql"
		exit
	fi

	pid=`cat ${TMP_PID_FILE} | head -n1 | awk '{print $1}'`
	if [ ! -z "$pid"  ];then
		deleteurlsql="select id from beidou.vturl where userid=$userid and pid=$pid;"
		
		runsql_single_read "$deleteurlsql" "$TMP_URLID_FILE" "$slice"
		#runsql_single_file "$deleteurlsql"  "$slice" "$TMP_URLID_FILE"
		
		if [ $? -ne 0 ];then
			PRINT_LOG "[error] select old beidou.vturl fail [sql] $deleteurlsql"
			exit
		fi

	
		getDomainByUserId "$userid"

		if [ $? -eq 0 ];then

			while read urlLine
			do
				if [ ! -z "$urlLine" ];then

					inserturlsql="insert into beidou.vturl(pid,userid,url) values($pid,$userid,'$urlLine')"
					runsql_single_file "$inserturlsql" "$slice" "$INSERT_RESULT"

					if [ $? -ne 0 ];then
						PRINT_LOG "[error] insert beidou.vturl fail [sql] $inserturlsql"
						exit
					fi
				fi
			done < $USERDOMAIN_FILE

			while read deleteLine
			do
				id=`echo $deleteLine | awk '{print $1}'`
				delSql="delete from beidou.vturl where userid=$userid and id=$id"

				runsql_single "$delSql" "$slice"

			done < $TMP_URLID_FILE 
		fi
	fi

done < ${INCREMENT_DATA}

#if [ $? -eq 0 ];then
#	mv $USERDOMAIN_URL_FILE $USERDOMAIN_URL_FILE_BAK
#fi

end_time=`date +"%F %T"`
echo "[$end_time]===========================end========================"
