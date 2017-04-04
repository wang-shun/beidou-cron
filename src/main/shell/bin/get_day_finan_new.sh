#!/bin/sh
#get cost data of each day, if there is no params, then query cost data yesterday
#input format:yyyyMMdd

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/outputcrmstat_new.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=get_day_finan.sh
reader_list=zengyunfeng

cd ${ROOT_PATH}
if [ $? -ne 0 ] ; then
	exit 1
fi
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

get_day_user(){
	echo "`date +%F\ %T` get_day_user:" $* >> ${LOG_FILE}
	#get userid(cost>0) of one day, input params,1: one certain day; 2: output filename
	msg="get $1 userid(cost>0) failed"
	runsql_clk_read "SELECT distinct userid FROM beidoufinan.cost_$1 WHERE price>0 GROUP BY userid ORDER BY userid"  $2
	alert $? "${msg}"
}

merge_and_sort(){
	#merge and sort two files, input params,1: one filename and result filename; 2: another filename
	echo "`date +%F\ %T` merge_and_sort:" $* >> ${LOG_FILE}
	msg="cat $1 to $2"
	cat $2 >> $1
	alert $? "${msg}"
	msg="sort -nu $1 to tmpfile"
	sort -nu $1 >> $1.tmp
	alert $? "${msg}"
	msg="mv tmpfile $1"
	mv $1.tmp $1
	alert $? "${msg}"
}

STAT_DATE=`date -d 'yesterday' +%Y%m%d`
isInManualOperation=0
if [ -n "$1" ] ;then
	STAT_DATE=`date -d"$1" +%Y%m%d`
	isInManualOperation=1
fi

echo "$0 stat date:$STAT_DATE" >> ${LOG_FILE}

msg="cd ${ROOT_PATH} failed"
cd ${ROOT_PATH}
alert $? "${msg}"

#output beidoufinan data
msg="get ${STAT_DATE} business data failed"
runsql_clk_read "SELECT userid, SUM(price*rrate), SUM(price), COUNT(*) FROM beidoufinan.cost_${STAT_DATE} GROUP BY userid ORDER BY userid"  ${FINAN_FILE}
alert $? "${msg}"

#output userid(cost>0) for each day
msg="get file of userid(cost>0) for each day failed"
awk '{if($3>0) print $1}' ${FINAN_FILE}> ${DAY_USERID}
alert $? "${msg}"

#output userid(cost>0) for each week
DAY_OF_WEEK=`date -d"${STAT_DATE}" +%u`
echo "DAY_OF_WEEK: ${DAY_OF_WEEK}">> ${LOG_FILE}
if [ 1 -eq "${DAY_OF_WEEK}" ] 
then
	#Monday
	msg="output file of userid(cost>0) for each week [${WEEK_USERID}] failed"
	cp ${DAY_USERID} ${WEEK_USERID}
	alert $? "${msg}"
	
else
	#not Monday 
	if ! [ -f ${WEEK_USERID} ] ; then
		#not exsit week-file, needed to generate
		echo 'not exit week userid'>> ${LOG_FILE}
		let DAY_BEFORE=DAY_OF_WEEK-1
		for DAY_BEFORE in `seq ${DAY_BEFORE}`
		do
			WEEK_DAY=`date -d"-${DAY_BEFORE}day ${STAT_DATE}" +%Y%m%d`
			get_day_user ${WEEK_DAY} ${WEEK_USERID}.tmp
			merge_and_sort ${WEEK_USERID} ${WEEK_USERID}.tmp
		done
	fi
	merge_and_sort ${WEEK_USERID} ${DAY_USERID}
fi

#output userid(cost>0) for each month
DAY_OF_MONTH=`date -d"${STAT_DATE}" +%e`
echo "DAY_OF_MONTH: ${DAY_OF_MONTH}">> ${LOG_FILE}
if [ 1 -eq "${DAY_OF_MONTH}" ] 
then
	msg="output file of userid(cost>0) for each month [${MONTH_USERID}] failed"
	cp ${DAY_USERID} ${MONTH_USERID}
	alert $? "${msg}"
	
else
	if ! [ -f ${MONTH_USERID} ] ; then
		#not exsit month-file, needed to generate
		echo 'not exit month userid'>> ${LOG_FILE}
		let DAY_BEFORE=DAY_OF_MONTH-1
		for DAY_BEFORE in `seq ${DAY_BEFORE}`
		do
			WEEK_DAY=`date -d"-${DAY_BEFORE}day ${STAT_DATE}" +%Y%m%d`
			get_day_user ${WEEK_DAY} ${MONTH_USERID}.tmp
			merge_and_sort ${MONTH_USERID} ${MONTH_USERID}.tmp
		done
	fi
	#merge and sort data yesterday into month-file
	merge_and_sort ${MONTH_USERID} ${DAY_USERID}	
fi

#output userid(cost>0) for each season
MONTH_OF_YEAR=`date -d"${STAT_DATE}" +%m`
echo "MONTH_OF_YEAR: ${MONTH_OF_YEAR}">> ${LOG_FILE}
if [ 1 -eq "${DAY_OF_MONTH}" ] && [ 1 -eq "$MONTH_OF_YEAR" -o 4 -eq "$MONTH_OF_YEAR" -o 7 -eq "$MONTH_OF_YEAR" -o 10 -eq "$MONTH_OF_YEAR" ]
then
	msg="output file of userid(cost>0) for each season [${SEASON_USERID}] failed"
	cp ${DAY_USERID} ${SEASON_USERID}
	alert $? "${msg}"
else
	if ! [ -f ${SEASON_USERID} ] ; then
		#not exsit season-file, needed to generate
		echo 'not exit season userid' >> ${LOG_FILE}
		let DAY_BEFORE=DAY_OF_MONTH-1
		MONTH_OF_YEAR=`echo $MONTH_OF_YEAR |awk '{print strtonum($1)}' `
		let END_MONTH=(MONTH_OF_YEAR-1)/3*3+1
		CUR_MONTH=${MONTH_OF_YEAR}
		CUR_DAY=${DAY_OF_MONTH}
		DAY_BEFORE=0
		while ! [ 1 -eq "${CUR_DAY}" -a "${END_MONTH}" -eq "${CUR_MONTH}" ] 
		do
			let DAY_BEFORE=DAY_BEFORE+1
			CUR_MONTH=`date -d"-${DAY_BEFORE}day ${STAT_DATE}" +%m`
			CUR_DAY=`date -d"-${DAY_BEFORE}day ${STAT_DATE}" +%e`
			SEASON_DAY=`date -d"-${DAY_BEFORE}day ${STAT_DATE}" +%Y%m%d`
			get_day_user ${SEASON_DAY} ${SEASON_USERID}.tmp
			merge_and_sort ${SEASON_USERID} ${SEASON_USERID}.tmp
		done
	fi
	#merge and sort data yesterday into season-file
	merge_and_sort ${SEASON_USERID} ${DAY_USERID}	
fi

#query business data and data in storage, get mainsite(cost>0), get shifen userinfo file instead of getting ulevelid
if [ $isInManualOperation -eq 0 ]; then
	msg="get all userlist file failed"
	runsql_cap_read "select userid, 0 FROM beidoucap.useraccount a where a.ustate !=2 order by a.userid"  ${USER_FILE}
	alert $? "${msg}"
	cp ${USER_FILE} ${USER_FILE}.${STAT_DATE}
else
	msg="copy all userlist file[${USER_FILE}.${STAT_DATE}]failed"
	cp ${USER_FILE}.${STAT_DATE} ${USER_FILE}
	alert $? "${msg}"
fi

#split CRM_TASK_CONCURRENT_NUMBER pieces for all file(six files), according to userid%CRM_TASK_CONCURRENT_NUMBER, filename format: original-file.number
msg="split user file failed"
awk -vsplit_number=${CRM_TASK_CONCURRENT_NUMBER} '{
	partId = ($1 % split_number) + 1;
	print $0 > FILENAME"."partId;
}' ${FINAN_FILE} ${DAY_USERID} ${WEEK_USERID} ${MONTH_USERID} ${SEASON_USERID} ${USER_FILE}
alert $? "${msg}"
