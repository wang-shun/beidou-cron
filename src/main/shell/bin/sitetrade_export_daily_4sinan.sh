#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/sitetrade_export_daily_4sinan.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SINAN_EXPORT_FILEPATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

program=sitetrade_export_daily_4sinan.sh
reader_list=liangshimu

#get trade_ids file
msg="${program}:export sitetrade for sinan error"
TRADE_ID_FILE="${SINAN_EXPORT_FILEPATH}/TRADE_ID_FILE.txt"
SQL="select a.siteurl,a.firsttradeid,a.secondtradeid,b.sitename, concat(d.scale,'ÐÇ'), c.srchs, b.sitedesc from beidouext.unionsite a left join beidouext.unionsiteinfos b on a.siteid=b.siteid left join beidouext.unionsitestat c on a.siteid=c.siteid left join beidouext.unionsitebdstat d on a.siteid=d.siteid where a.valid=1;"
runsql_xdb_read "${SQL}" "${TRADE_ID_FILE}"
alert $? "${msg}"

#get tradeName
TRADE_NAME_SQL=${SINAN_EXPORT_FILEPATH}/"TRADE_NAME_SQL.sql"
if [ -s ${TRADE_ID_FILE} ]
then
	awk -v maxNum=${MAX_SELECT_NUM_PER_TIME} -v sqlFile=${TRADE_NAME_SQL} 'BEGIN{ ORS=""; count=0; firstExeTime=0;}; 
	{	
		if(firstExeTime == 0){
			print "select t.tradeid, t.tradename from beidoucode.sitetrade as t where t.tradeid in (" >> sqlFile
		}
		if(count < maxNum) {
	         print $2", "$3", " >> sqlFile
	         count += 1;
	    }else{
	    	print "0);\n" >> sqlFile
	    	print "select t.tradeid, t.tradename from beidoucode.sitetrade as t where t.tradeid in (" >> sqlFile
			print $2", "$3", " >> sqlFile
			count = 1;    
	    }
	    
	    firstExeTime +=1;
	} END{print "0);\n" >> sqlFile }' "${TRADE_ID_FILE}"
fi

#query db
TMP_QUERY_FILE="${SINAN_EXPORT_FILEPATH}/TMP_QUERY_FILE.txt"
TRADE_NAME_FILE="${SINAN_EXPORT_FILEPATH}/TRADE_NAME_FILE.txt"
if [ -s ${TRADE_NAME_SQL} ]
then
	while read line
	do	
		if [ -f ${TMP_QUERY_FILE} ]
		then
			rm -f ${TMP_QUERY_FILE}
		fi
		touch ${TMP_QUERY_FILE}
		runsql_cap_read "${line}" "${TMP_QUERY_FILE}"
		if [ -s ${TMP_QUERY_FILE} ]
		then
			cat ${TMP_QUERY_FILE} >> ${TRADE_NAME_FILE}
		fi
		sleep 1
	done < ${TRADE_NAME_SQL}
fi

#generate result file
RESULT_FILE=${SINAN_EXPORT_FILEPATH}/${SINAN_EXPORT_FILENAME}
if [ -s ${RESULT_FILE} ]
then
	rm -f ${RESULT_FILE}
fi
if [ -s ${TRADE_NAME_FILE} ] && [ -s ${TRADE_ID_FILE} ]
then
	awk  -v resultFile=${RESULT_FILE} 'ARGIND==1{
			trade_id_name_map[$1]=$2;
		}
		ARGIND==2{
			firstTradeName="NULL";
			secondTradeName="NULL";
			if($2 in trade_id_name_map){
				firstTradeName=trade_id_name_map[$2];
			}
			if($3 in trade_id_name_map){
				secondTradeName=trade_id_name_map[$3];
			}
			print $1"\t"firstTradeName"\t"secondTradeName"\t"$4"\t"$5"\t"$6"\t"$7 >> resultFile
						
		}' ${TRADE_NAME_FILE} ${TRADE_ID_FILE}
fi

#clear tmp file
rm -f ${TRADE_ID_FILE}
rm -f ${TRADE_NAME_SQL}
rm -f ${TMP_QUERY_FILE}
rm -f ${TRADE_NAME_FILE}

# generate md5
cd ${SINAN_EXPORT_FILEPATH}
md5sum ${SINAN_EXPORT_FILENAME} > ${SINAN_EXPORT_FILENAME}.md5
