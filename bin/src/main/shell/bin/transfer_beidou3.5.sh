#!/bin/bash

#@file: transfer_beidou3.5.sh
#@author: caichao


CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


CONF_SH="./alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


program=transfer.sh
curr_date=`date  "+%Y%m%d"`

WORK_PATH=${DATA_PATH}/transfer_data
ROLLBACK_WORK_PATH=${DATA_PATH}/transfer_data_rollback
LOG_PATH=${LOG_PATH}/transfer_data
LOG_NAME=transfer_data
LOG_FILE=${LOG_PATH}/${LOG_NAME}.${curr_date}.log 

WORK_PATH_KT_RT=${DATA_PATH}/transfer_data/kt_rt
WORK_PATH_IT_RT=${DATA_PATH}/transfer_data/it_rt
WORK_PATH_KT_IT=${DATA_PATH}/transfer_data/kt_it
WORK_PATH_IT=${DATA_PATH}/transfer_data/it
WORK_PATH_RT=${DATA_PATH}/transfer_data/rt

rm -rf ${WORK_PATH}
mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${WORK_PATH}

mkdir -p $ROLLBACK_WORK_PATH
mkdir -p $WORK_PATH_KT_RT
mkdir -p $WORK_PATH_IT_RT
mkdir -p $WORK_PATH_KT_IT
mkdir -p $WORK_PATH_IT
mkdir -p $WORK_PATH_RT

KT_RT_GROUP=${WORK_PATH_KT_RT}/kt_rt_group
GROUP_DELETE=${WORK_PATH_KT_RT}/group_delete
GROUP_DELETE_ROLLBACK=${ROLLBACK_WORK_PATH}/group_delete_rollback
GROUP_DELETE_BEFORE_STATUS=${WORK_PATH_KT_RT}/group_delete_before_status
GROUP_DELETE_DELINFO_INSERT=${WORK_PATH_KT_RT}/group_delete_delinfo_insert



RT_IT_GROUP=${WORK_PATH_IT_RT}/it_rt_group

IT_RELATION=${WORK_PATH_IT_RT}/it_relation
ITID=${WORK_PATH_IT_RT}/itid
IT_RELATION_INSERT=${WORK_PATH_IT_RT}/it_relation_insert
IT_RELATION_INSERT_ROLLBACK=${ROLLBACK_WORK_PATH}/it_relation_insert_rollback


RT_RELATION=${WORK_PATH_IT_RT}/rt_relation
PEOPLEID=${WORK_PATH_IT_RT}/peopleid
RT_RELATION_INSERT=${WORK_PATH_IT_RT}/people_relation_insert
RT_RELATION_INSERT_ROLLBACK=${ROLLBACK_WORK_PATH}/people_relation_insert_rollback

IT_RT_UPDATE_TARGETTYPE=${WORK_PATH_IT_RT}/it_rt_update_targettype
IT_RT_UPDATE_FINAL=${WORK_PATH_IT_RT}/it_rt_update_final
IT_RT_UPDATE_TARGETTYPE_ROLLBACK=${ROLLBACK_WORK_PATH}/it_rt_update_targettype_rollback


KT_IT_GROUP=${WORK_PATH_KT_IT}/kt_it_group
IT_RELATION_KT=${WORK_PATH_KT_IT}/it_relation_kt
IT_RELATION_INSERT_KT=${WORK_PATH_KT_IT}/it_relation_insert_kt
IT_RELATION_INSERT_KT_ROLLBACK=${ROLLBACK_WORK_PATH}/it_relation_insert_kt_rollback
ITID_KT=${WORK_PATH_KT_IT}/itid_kt
IT_KT_UPDATE_FINAL=${WORK_PATH_KT_IT}/it_kt_update_final
KT_IT_UPDATE_TARGETTYPE=${WORK_PATH_KT_IT}/kt_it_update_targettype
KT_IT_UPDATE_TARGETTYPE_ROLLBACK=${ROLLBACK_WORK_PATH}/kt_it_update_targettype_rollback

SINGLE_IT_GROUP=${WORK_PATH_IT}/single_it_group
SINGLE_RELATION_IT=${WORK_PATH_IT}/single_relation_it
SINGLE_IT_RELATION_INSERT=${WORK_PATH_IT}/single_it_relation_insert
SINGLE_IT_RELATION_INSERT_ROLLBACK=${ROLLBACK_WORK_PATH}/single_it_relation_insert_rollback
ITID_SINGLE=${WORK_PATH_IT}/itid_single
SINGLE_IT_UPDATE_TARGETTYPE=${WORK_PATH_IT}/single_it_update_targettype
SINGLE_IT_UPDATE_FINAL=${WORK_PATH_IT}/single_it_update_final
SINGLE_IT_UPDATE_TARGETTYPE_ROLLBACK=${ROLLBACK_WORK_PATH}/single_it_update_targettype_rollback

SINGLE_RT_GROUP=${WORK_PATH_RT}/single_rt_group
SINGLE_RELATION_RT=${WORK_PATH_RT}/single_relation_rt
SINGLE_RT_RELATION_INSERT=${WORK_PATH_RT}/single_rt_relation_insert
SINGLE_RT_RELATION_INSERT_ROLLBACK=${ROLLBACK_WORK_PATH}/single_rt_relation_insert_rollback
PEOPLEID_SINGLE=${WORK_PATH_RT}/peopleid_single
SINGLE_RT_UPDATE_TARGETTYPE=${WORK_PATH_RT}/single_rt_update_targettype
SINGLE_RT_UPDATE_FINAL=${WORK_PATH_RT}/single_rt_update_final
SINGLE_RT_UPDATE_TARGETTYPE_ROLLBACK=${ROLLBACK_WORK_PATH}/single_rt_update_targettype_rollback

SINGLE_INTEREST_PRICE=${WORK_PATH_IT}/price_it_single
SINGLE_INTEREST_PRICE_INSERT=${WORK_PATH_IT}/price_it_single_insert
SINGLE_INTEREST_PRICE_INSERT_ROLLBACK=${ROLLBACK_WORK_PATH}/price_it_single_insert_rollback


INTEREST_PRICE_KT=${WORK_PATH_KT_IT}/price_it_kt
INTEREST_PRICE_INSERT_KT=${WORK_PATH_KT_IT}/price_it_kt_insert
INTEREST_PRICE_INSERT_KT_ROLLBACK=${ROLLBACK_WORK_PATH}/price_it_kt_insert_rollback

INTEREST_PRICE_RT=${WORK_PATH_IT_RT}/price_it_rt
INTEREST_PRICE_INSERT_RT=${WORK_PATH_IT_RT}/price_it_rt_insert
INTEREST_PRICE_INSERT_RT_ROLLBACK=${ROLLBACK_WORK_PATH}/price_it_rt_insert_rollback

DATABASE_FINAL_UPDATE=${WORK_PATH}/database_final_update


function INF()
{
 echo $1
 echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` "$1 >> $LOG_FILE
}
function ERR()
{
 echo $1
 echo "[ERROR] `date +"%Y-%m-%d %H:%M:%S"` "$1 >> $LOG_FILE
}

function calAllId()
{
	local start=$1;
	local idFile=$2;
	local count=$3

	> ${idFile}
	for((j=0;j<$count;j++))
	do
		local nextId=$(($start+$j*8));
		echo -e "$nextId" >> $idFile
	done

}

function getSql()
{
	local p1=$1
	local p2=$2

	sql="select g.groupid from beidou.cprogroup g inner join (select gid from (select distinct groupid as gid from beidou.group_pack where pack_type=${p1} union all select distinct groupid from beidou.group_pack where pack_type=${p2}) a group by a.gid having count(*) > 1) d on g.groupid=d.gid;"
}


function get2TSql()
{
	local p1=$1
	local p2=$2
	local p3=$3

	sql="select g.groupid from beidou.cprogroup g inner join (select gid from (select distinct groupid as gid from beidou.group_pack where pack_type=$p1 union all select distinct groupid from beidou.group_pack where pack_type=$p2) a group by a.gid having count(*) > 1) d on g.groupid=d.gid and g.groupid not in (select groupid from beidou.group_pack where pack_type=$p3)"
}


function getRelationSql() {
	local p1=$1
	local p2=$2

	relateSql="select groupid,planid,userid,pack_id from beidou.group_pack where pack_type=${p1} and groupid in (${p2});"
}

function getSingelTSql()
{
	local p1=$1
	local p2=$2

	sql="select distinct groupid from beidou.group_pack where  pack_type=$p1 and groupid not in (select groupid from beidou.group_pack where  pack_type in ($p2));"
}

function getInterestPrice()
{
	local p1=$1
	local p2=$2

	interestPriceSql="select groupid,planid,userid,pack_id,price from beidou.group_pack where pack_type=$p1 and groupid in($p2) and price > 0";
}

function handleKTRTGroup()
{
	#delete rt | kt
	
	getSql "2" "3"
	runsql_single_read "$sql" "${KT_RT_GROUP}.$i" "${i}"

	cat ${KT_RT_GROUP}.$i | awk 'BEGIN{groupids=""}{groupids=groupids$1","}END{print "update beidou.cprogroup set groupstate=2 where groupid in ("groupids"0);"}' > "${GROUP_DELETE}.$i"

	groupIds=`cat ${KT_RT_GROUP}.${i} | awk 'BEGIN{grouplist=""}{grouplist=grouplist$1","}END{print grouplist"0"}'`

	runsql_single_read "select groupid,groupstate from beidou.cprogroup where groupid in($groupIds);" "$GROUP_DELETE_BEFORE_STATUS.$i" $i

	cat "$GROUP_DELETE_BEFORE_STATUS.$i" | awk '{print "update beidou.cprogroup set groupstate="$2" where groupid="$1";"}' > ${GROUP_DELETE_ROLLBACK}.$i

	#insert into groupdelinfo
	cat "${KT_RT_GROUP}.$i" | awk '{print "replace into beidou.groupdelinfo(groupid,deltime) values("$1",\"2014-09-15 00:00:00\");"}' > "$GROUP_DELETE_DELINFO_INSERT.$i"
	echo "delete from beidou.groupdelinfo where groupid=0;" >> $GROUP_DELETE_DELINFO_INSERT.$i
	#delinfo end

	#delete rt|kt end
}

function handleITRTGroup()
{
	#it|rt start
	#transfer it|rt data & update targettype
	get2TSql "1" "2" "3"

	runsql_single_read "$sql" "${RT_IT_GROUP}.${i}" "$i"
	
	groupIds=`cat ${RT_IT_GROUP}.${i} | awk 'BEGIN{grouplist=""}{grouplist=grouplist$1","}END{print grouplist"0"}'` 
	
	getRelationSql "1" "$groupIds"
	runsql_single_read "${relateSql}" "${IT_RELATION}.$i" "$i"
	itrelationcount=`cat "${IT_RELATION}.$i" | wc -l | awk '{print $1}'`
	runsql_single_file "select beidou.get_next_values('cprogroupit',${itrelationcount});" "$i" "${ITID}.$i.tmp"

	startId=`cat "${ITID}.$i.tmp" | awk '{print $1}'` 
	calAllId "$startId"  "${ITID}.$i" "$itrelationcount"

    awk 'BEGIN{key=1}ARGIND==1{
				itid[NR]=$1
			}
		ARGIND==2{
				print("insert into beidou.cprogroupit(itid,planid,groupid,userid,iid,type,addtime,adduser) values("itid[key]","$2","$1","$3","$4",1,now(),"$3");");
				key++
			}
	
	' "${ITID}.$i" "${IT_RELATION}.$i" > "${IT_RELATION_INSERT}.$i"

	#rollback
    awk '{print "delete from beidou.cprogroupit where itid="$1";"}' ${ITID}.$i > ${IT_RELATION_INSERT_ROLLBACK}.$i
	#rollback


	getRelationSql "2" "$groupIds"
	runsql_single_read "${relateSql}" "${RT_RELATION}.$i" "$i"
	rtrelationcount=`cat "${RT_RELATION}.$i" | wc -l | awk '{print $1}'`
	runsql_single_file "select beidou.get_next_values('cprogroupvt',$rtrelationcount);" "$i" "${PEOPLEID}.$i.tmp"
	
	startId=`cat "${PEOPLEID}.$i.tmp" | awk '{print $1}'`
	calAllId "${startId}" "${PEOPLEID}.$i" "$rtrelationcount"

	awk 'BEGIN{key=1}ARGIND==1{
			peopleid[NR]=$1
		}
		ARGIND==2{
			print("insert into beidou.cprogroupvt(vtid,groupid,planid,userid,targettype,pid,relatetype,addtime,modtime,adduser,moduser) values("peopleid[key]","$1","$2","$3",16,"$4",0,now(),now(),"$3","$3");");
			key++
		}
	' "${PEOPLEID}.$i" "${RT_RELATION}.$i" > ${RT_RELATION_INSERT}.$i

	awk '{print "delete from beidou.cprogroupvt where vtid="$1";"}' "${PEOPLEID}.$i" > ${RT_RELATION_INSERT_ROLLBACK}.$i

	# tranfer interest price start
	getInterestPrice "1" "$groupIds"

	runsql_single_read "$interestPriceSql" "$INTEREST_PRICE_RT.$i" "$i"

	awk '{print "insert into beidou.group_interest_price(groupid,planid,userid,iid,type,price,addtime,adduser,modtime,moduser) values("$1","$2","$3","$4",1,"$5",now(),"$3",now(),"$3");"}' $INTEREST_PRICE_RT.$i > $INTEREST_PRICE_INSERT_RT.$i

	# transfer interest price end

	awk '{print "delete from beidou.group_interest_price where groupid="$1" and planid="$2" and userid="$3" and iid="$4" and price="$5";"}' $INTEREST_PRICE_RT.$i > $INTEREST_PRICE_INSERT_RT_ROLLBACK.$i

	echo -e "update beidou.cprogroup set targettype=48 where groupid in($groupIds);" > "${IT_RT_UPDATE_TARGETTYPE}.${i}"

	echo -e "update beidou.cprogroup set targettype=64 where groupid in($groupIds);" > "${IT_RT_UPDATE_TARGETTYPE_ROLLBACK}.${i}"

	cat "${IT_RELATION_INSERT}.$i" ${RT_RELATION_INSERT}.$i $INTEREST_PRICE_INSERT_RT.$i "${IT_RT_UPDATE_TARGETTYPE}.${i}" > ${IT_RT_UPDATE_FINAL}.$i

	#IT | RT end
}

function handleKTITGroup()
{
	#IT | KT start

	get2TSql "1" "3" "2"

	runsql_single_read "$sql" "${KT_IT_GROUP}.${i}" "$i"
	
	groupIds=`cat ${KT_IT_GROUP}.${i} | awk 'BEGIN{grouplist=""}{grouplist=grouplist$1","}END{print grouplist"0"}'` 
	
	getRelationSql "1" "$groupIds"
	runsql_single_read "${relateSql}" "${IT_RELATION_KT}.$i" "$i"
	itrelationcount=`cat "${IT_RELATION_KT}.$i" | wc -l | awk '{print $1}'`
	runsql_single_file "select beidou.get_next_values('cprogroupit',${itrelationcount});" "$i" "${ITID_KT}.$i.tmp"

	startId=`cat "${ITID_KT}.$i.tmp" | awk '{print $1}'` 
	calAllId "$startId"  "${ITID_KT}.$i" "$itrelationcount"

    awk 'BEGIN{key=1}ARGIND==1{
				itid[NR]=$1
			}
		 ARGIND==2{
				print("insert into beidou.cprogroupit(itid,planid,groupid,userid,iid,type,addtime,adduser) values("itid[key]","$2","$1","$3","$4",1,now(),"$3");");
				key++
			}
	
	' "${ITID_KT}.$i" "${IT_RELATION_KT}.$i" > "${IT_RELATION_INSERT_KT}.$i"

	
	#rollback
    awk '{print "delete from beidou.cprogroupit where itid="$1";"}' ${ITID_KT}.$i > ${IT_RELATION_INSERT_KT_ROLLBACK}.$i
	#rollback
	
	# tranfer interest price start
	getInterestPrice "1" "$groupIds"

	runsql_single_read "$interestPriceSql" "$INTEREST_PRICE_KT.$i" "$i"

	awk '{print "insert into beidou.group_interest_price(groupid,planid,userid,iid,type,price,addtime,adduser,modtime,moduser) values("$1","$2","$3","$4",1,"$5",now(),"$3",now(),"$3");"}' $INTEREST_PRICE_KT.$i > $INTEREST_PRICE_INSERT_KT.$i

	# transfer interest price end

	awk '{print "delete from beidou.group_interest_price where groupid="$1" and planid="$2" and userid="$3" and iid="$4" and price="$5";"}' $INTEREST_PRICE_KT.$i > $INTEREST_PRICE_INSERT_KT_ROLLBACK.$i

	echo -e "update beidou.cprogroup set targettype=96 where groupid in($groupIds);" > "${KT_IT_UPDATE_TARGETTYPE}.${i}"

	echo -e "update beidou.cprogroup set targettype=64 where groupid in($groupIds);" > "${KT_IT_UPDATE_TARGETTYPE_ROLLBACK}.${i}"


	cat "${IT_RELATION_INSERT_KT}.$i" $INTEREST_PRICE_INSERT_KT.$i "${KT_IT_UPDATE_TARGETTYPE}.${i}" > ${IT_KT_UPDATE_FINAL}.${i}

	#IT | KT end
}

function handleItGroup()
{
	#IT start

	getSingelTSql "1" "0,2,3,4"

	runsql_single_read "$sql" "${SINGLE_IT_GROUP}.${i}" "$i"
	
	groupIds=`cat ${SINGLE_IT_GROUP}.${i} | awk 'BEGIN{grouplist=""}{grouplist=grouplist$1","}END{print grouplist"0"}'` 
	
	getRelationSql "1" "$groupIds"
	runsql_single_read "${relateSql}" "${SINGLE_RELATION_IT}.$i" "$i"
	itrelationcount=`cat "${SINGLE_RELATION_IT}.$i" | wc -l | awk '{print $1}'`
	runsql_single_file "select beidou.get_next_values('cprogroupit',${itrelationcount});" "$i" "${ITID_SINGLE}.$i.tmp"

	startId=`cat "${ITID_SINGLE}.$i.tmp" | awk '{print $1}'` 
	calAllId "$startId"  "${ITID_SINGLE}.$i" "$itrelationcount"

    awk 'BEGIN{key=1}ARGIND==1{
				itid[NR]=$1
			}
		 ARGIND==2{
				print("insert into beidou.cprogroupit(itid,planid,groupid,userid,iid,type,addtime,adduser) values("itid[key]","$2","$1","$3","$4",1,now(),"$3");");
				key++
			}
	
	' "${ITID_SINGLE}.$i" "${SINGLE_RELATION_IT}.$i" > "${SINGLE_IT_RELATION_INSERT}.$i"

    awk '{print "delete from beidou.cprogroupit where itid="$1";"}' "${ITID_SINGLE}.$i" > ${SINGLE_IT_RELATION_INSERT_ROLLBACK}.$i

	# tranfer interest price start
	getInterestPrice "1" "$groupIds"

	runsql_single_read "$interestPriceSql" "$SINGLE_INTEREST_PRICE.$i" "$i"

	awk '{print "insert into beidou.group_interest_price(groupid,planid,userid,iid,type,price,addtime,adduser,modtime,moduser) values("$1","$2","$3","$4",1,"$5",now(),"$3",now(),"$3");"}' $SINGLE_INTEREST_PRICE.$i > $SINGLE_INTEREST_PRICE_INSERT.$i

	# transfer interest price end


	awk '{print "delete from beidou.group_interest_price where groupid="$1" and planid="$2" and userid="$3" and iid="$4" and price="$5";"}' $SINGLE_INTEREST_PRICE.$i > $SINGLE_INTEREST_PRICE_INSERT_ROLLBACK.$i

	echo -e "update beidou.cprogroup set targettype = 32 where groupid in (${groupIds});" > ${SINGLE_IT_UPDATE_TARGETTYPE}.${i}
	echo -e "update beidou.cprogroup set targettype = 64 where groupid in (${groupIds});" > ${SINGLE_IT_UPDATE_TARGETTYPE_ROLLBACK}.${i}

	cat "${SINGLE_IT_RELATION_INSERT}.$i" $SINGLE_INTEREST_PRICE_INSERT.$i ${SINGLE_IT_UPDATE_TARGETTYPE}.${i} > ${SINGLE_IT_UPDATE_FINAL}.${i}

	# IT end
}

function handleRtGroup()
{
	#RT start

	getSingelTSql "2" "0,1,3,4"

	runsql_single_read "$sql" "${SINGLE_RT_GROUP}.${i}" "$i"
	
	groupIds=`cat ${SINGLE_RT_GROUP}.${i} | awk 'BEGIN{grouplist=""}{grouplist=grouplist$1","}END{print grouplist"0"}'` 
	getRelationSql "2" "$groupIds"
	runsql_single_read_beidou "${relateSql}"  "${SINGLE_RELATION_RT}.$i" "$i"
	rtrelationcount=`cat "${SINGLE_RELATION_RT}.$i" | wc -l | awk '{print $1}'`
	echo $rtrelationcount
	runsql_single_file "select beidou.get_next_values('cprogroupvt',$rtrelationcount);" "$i" "${PEOPLEID_SINGLE}.$i.tmp"
	

	startId=`cat "${PEOPLEID_SINGLE}.$i.tmp" | awk '{print $1}'`
	calAllId "${startId}" "${PEOPLEID_SINGLE}.$i" "$rtrelationcount"

	awk 'BEGIN{key=1}ARGIND==1{
			peopleid[NR]=$1
		}
		ARGIND==2{
			print("insert into beidou.cprogroupvt(vtid,groupid,planid,userid,targettype,pid,relatetype,addtime,modtime,adduser,moduser) values("peopleid[key]","$1","$2","$3",16,"$4",0,now(),now(),"$3","$3");");
			key++
		}
	' "${PEOPLEID_SINGLE}.$i" "${SINGLE_RELATION_RT}.$i" > ${SINGLE_RT_RELATION_INSERT}.$i


	awk '{print "delete from beidou.cprogroupvt where vtid="$1";"}' "${PEOPLEID_SINGLE}.$i"  > ${SINGLE_RT_RELATION_INSERT_ROLLBACK}.$i

	echo -e "update beidou.cprogroup set targettype=16 where groupid in($groupIds);" > "${SINGLE_RT_UPDATE_TARGETTYPE}.${i}"
	echo -e "update beidou.cprogroup set targettype=64 where groupid in($groupIds);" > "${SINGLE_RT_UPDATE_TARGETTYPE_ROLLBACK}.${i}"

	cat ${SINGLE_RT_RELATION_INSERT}.$i "${SINGLE_RT_UPDATE_TARGETTYPE}.${i}" > "${SINGLE_RT_UPDATE_FINAL}.${i}"

	#RT end
}

function runsql_file_with_transaction()
{
   local sharding_idx=$1
   local file=$2


   sed -i '1 i\begin;' $file 
   echo "commit;" >> $file


   
   if [ -z $sharding_idx ];then
      PRINT_DB_LOG "$0:The 2rd Parameter For Function runsql_single can not be null"
      return 1
   fi
   
   local machine_id=${DB_HOST_BD_MAID[${sharding_idx}]}
   local mysql_conn="${MYSQL_CLIENT} -B -N -h${machine_id} -P${DB_PORT_BD_MAID[$sharding_idx]} -u${DB_USER_BD_MAID} -p${DB_PASSWORD_BD_MAID} -Dbeidoucap --default-character-set=gbk --skip-column-names"
   $mysql_conn < $file 2>> ${LOG_FILE}
}

function finalUpdateData()
{
	cat "${SINGLE_RT_UPDATE_FINAL}.$i" "${SINGLE_IT_UPDATE_FINAL}.$i" ${IT_KT_UPDATE_FINAL}.$i ${IT_RT_UPDATE_FINAL}.$i ${GROUP_DELETE}.$i "$GROUP_DELETE_DELINFO_INSERT.$i" > "$DATABASE_FINAL_UPDATE.$i"

	runsql_file_with_transaction $i "$DATABASE_FINAL_UPDATE.$i"

	if [ $? -eq 0 ] ;then
		echo -e "database $i finish" >> $LOG_FILE
	fi
}

function runsql_single_read_beidou()
{
	   local sql=$1
	   local dumpfile=$2
		     
				      
		local sharding_idx=$3
					     
		if [ -z $sharding_idx ];then
		sharding_idx=`expr $RANDOM % ${SHARDING_SLICE}`
		fi
									     
		 tmp_sql=${sql}
										    
											      
	  local machine_id=${DB_HOST_BD_MAID_READ[${sharding_idx}]}
	  local mysql_conn="${MYSQL_CLIENT} -B -N -h${machine_id} -P${DB_PORT_BD_MAID_READ[$sharding_idx]} -u${DB_USER_BD_MAID_READ} -p${DB_PASSWORD_BD_MAID_READ} -Dbeidoucap --default-character-set=gbk --skip-column-names"
	  db_execute "${mysql_conn}" "${tmp_sql}" "${dumpfile}"
}

INF "begin task..."
for ((i=0;i<8;i++))
do
	
	sql="";

	relateSql="";

	interestPriceSql="";

	handleKTRTGroup

	handleITRTGroup

	handleKTITGroup

	handleItGroup

	handleRtGroup

	finalUpdateData
	
	echo $i
	
	sleep 120s
done
INF "finished"
