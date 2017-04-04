#!/bin/sh

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=eunionsiteinfo.sh
reader_list=genglei

##########################
##
#config options
#
CURR_DATE=`date -d "1 day ago" +"%Y%m%d"`

#########################

LOG_FILE=${LOG_PATH}/eunionsiteinfo.log

#�Ƿ���Ҫ׷��PM���õ�TC top1000 ��������վ������ 0:disable, 1:enable
IS_APPEND_TC_SITE=1

#PM���ܿ��������õ�TC��������վtop1000
TC_SITE_FILE=pmTcSite.dat
TC_SITE_BACKUP_FILE=pm_tc_site_data.txt.${CURR_DATE}
TC_SITE_FILE_GET="wget http://10.42.7.105/dr-mgr/common/downloadpublishfile/trade_of_top1000/trade_of_top1000_data.txt?authtype=noah"
TC_SITE_PATH=${DATA_PATH}/import/tcsite

#�Ƿ���Ҫ׷��PM���õİ���������������0:disable, 1:enable
IS_APPEND_SITE_NEW_TRADE=1
TC_EXPORT_FILE=unionsiteinfo.tc.dat

#PM���ܿ��������õİ�������������վ��
SITE_NEW_TRADE_URL=ftp://tc-sf-ct00.tc//home/work/var/sf-ct/filesvr/data/762/site_new_trade_whitelist_data.txt
SITE_NEW_TRADE_PATH=${DATA_PATH}/import/sitenewtrade
SITE_NEW_TRADE_FILE=site_new_trade_whitelist_data.txt
SITE_NEW_TRADE_BACKUP_FILE=site_new_trade_whitelist_data.txt.${CURR_DATE}

#modify 1
EXPORT_PATH=${DATA_PATH}/export/unionsiteinfo
EXPORT_FILE=unionsiteinfo.dat
EXPORT_MD5_FILE=${EXPORT_FILE}.md5
EXPORT_BACKUP_FILE=${EXPORT_FILE}.${CURR_DATE}

EXPORT_SITE_NEW_TRADE_PATH=${DATA_PATH}/export/unionsiteinfo
EXPORT_SITE_NEW_TRADE_FILE=unionsiteinfo.dat.whitelistsiteurl
EXPORT_SITE_NEW_TRADE_MD5_FILE=${EXPORT_SITE_NEW_TRADE_FILE}.md5
EXPORT_SITE_NEW_TRADE_BACKUP_FILE=${EXPORT_SITE_NEW_TRADE_FILE}.${CURR_DATE}

EXPORT_ALL_SITE_PATH=${DATA_PATH}/export/unionsiteinfo
EXPORT_ALL_SITE_FILE=unionsiteinfo.dat.all
EXPORT_ALL_SITE_MD5_FILE=${EXPORT_ALL_SITE_FILE}.md5
EXPORT_ALL_SITE_BACKUP_FILE=${EXPORT_ALL_SITE_FILE}.${CURR_DATE}

FIRST_TRADE_OTHER=25
SECOND_TRADE_OTHER_LIST=",106,207,306,409,705,808,908,1006,1106,1206,1304,1403,1505,1605,1704,1804,1911,2006,2106,2204,2304,2406,"
########################

##################################################################################

#����siteid��unionsite��tc_site����ͬһ��sequence����
function generateSiteId()
{
	sql="select history.get_next_value ('unionsiteidtype');"
	rm -f db.out
	runsql_xdb_read "$sql" db.out 1
	echo `cat db.out`
	rm -f db.out
}

#����tc_site_trede����TC��վ��Ϣ׷�ӵĵ�����nova���ļ���
function appendTcSite()
{
	cd ${EXPORT_PATH}

	touch shouldBeInValidSiteIds.out
	touch shouldInsertTcSite.out
	touch shouldUpDateTcSite.out
	
	#tc_site_trede���еļ�¼����pm���õ��ļ��У�����Ҫ��tc_site_trede���־λ��Ϊ��Ч
	awk -v file1=$TC_SITE_FILE 'FILENAME==file1{SITEURLPM[$1]=$1}\
	FILENAME=="tcSiteInDb.dat"{if(""==SITEURLPM[$2] && $5==1)print $1}'\
	$TC_SITE_FILE\
	tcSiteInDb.dat > shouldBeInValidSiteIds.out
	
	#pm���õ�վ��url����unionsite���Ҳ���tc_site_trede���У�������뵽tc_site_trede��
	awk -v file2=$EXPORT_FILE -v file3=$TC_SITE_FILE 'FILENAME=="tcSiteInDb.dat"{TCSITEURLDB[$2]=$1}\
	FILENAME==file2{SITEURLDB[$2]=$1}\
	FILENAME==file3{if(""==TCSITEURLDB[$1] && ""==SITEURLDB[$1] )print $0}'\
	tcSiteInDb.dat\
	$EXPORT_FILE\
	$TC_SITE_FILE > shouldInsertTcSite.out
	
	#pm���õ�վ��url��tc_site_trede���У��������pm���õ���Ϣ��tc_site_trede����
	#״̬ΪʧЧ����£���Ч��Ҳ���£���Ϊ����ҵ���ܱ仯
	awk -v file2=$TC_SITE_FILE 'FILENAME=="tcSiteInDb.dat"{TCSITEURLDB[$2]=$1}\
	FILENAME==file2{if(""!=TCSITEURLDB[$1])print TCSITEURLDB[$1]"\t"$0"\t1"}'\
	tcSiteInDb.dat\
	$TC_SITE_FILE > shouldUpDateTcSite.out
	
	insertTcSiteToDb
	updateTcSiteToDb
	updateTcSiteInvalidToDb
	
	sleep 3
	
	runsql_xdb_read "select t.siteid, t.siteurl, t.firsttradeid, t.secondtradeid from beidouext.tc_site_trade t where t.valid != 0  and t.siteurl not in (select siteurl from beidouext.unionsite) order by t.firsttradeid asc, t.secondtradeid asc, t.siteid asc" ./${TC_EXPORT_FILE}
	if [ "$?" -ne "0" ]; then
			hit "Failed: export tc site info"
	fi
	 
	cat $TC_EXPORT_FILE >> $EXPORT_FILE
	md5sum ${EXPORT_FILE} > ${EXPORT_MD5_FILE}
	cat $TC_EXPORT_FILE >> $EXPORT_ALL_SITE_FILE
	md5sum ${EXPORT_ALL_SITE_FILE} > ${EXPORT_ALL_SITE_MD5_FILE}
}

#�����ݲ��뵽tc_site_trede����
function insertTcSiteToDb()
{
	cd ${EXPORT_PATH}
	
	if ! [[ -f shouldInsertTcSite.out ]]; then
		alert 1 "File Not Found-shouldInsertTcSite.out"
	fi
	
	mv shouldInsertTcSite.out shouldInsertTcSite.out.tmp
	
	while read -r record
	do
		siteId=`generateSiteId`
		echo -e $siteId"\t""$record""\t1" >> shouldInsertTcSite.out
	done < shouldInsertTcSite.out.tmp
	rm -f shouldInsertTcSite.out.tmp
	
	# ensure file exist
	touch shouldInsertTcSite.out
	
	sql="load data local infile 'shouldInsertTcSite.out' ignore into table beidouext.tc_site_trade (siteid,siteurl,firsttradeid,secondtradeid,valid)"  
	runsql_xdb "$sql"
	if [ "$?" -ne "0" ]; then
        hit "Failed: insertTcSiteToDb"
	fi
}

#��tc_site_trede���и�����ʧЧ��վ��Ϊ��Ч
function updateTcSiteToDb()
{
	cd ${EXPORT_PATH}
	
	if ! [[ -f shouldUpDateTcSite.out ]]; then
		alert 1 "File Not Found-shouldUpDateTcSite.out"
	fi
	
	# ensure file exist
	touch shouldUpDateTcSite.out
	
	sql="load data local infile 'shouldUpDateTcSite.out' replace into table beidouext.tc_site_trade (siteid,siteurl,firsttradeid,secondtradeid,valid)"  
	runsql_xdb "$sql"
	if [ "$?" -ne "0" ]; then
        hit "Failed: updateTcSiteToDb"
	fi
}

#��tc_site_trede���и�����Ч��վ��ΪʧЧ
function updateTcSiteInvalidToDb()
{
	cd ${EXPORT_PATH}
	
	if ! [[ -f shouldBeInValidSiteIds.out ]]; then
		alert 1 "File Not Found-shouldBeInValidSiteIds.out"
	fi
	
	# ensure file exist
	touch shouldBeInValidSiteIds.out
	
	SITEIDS=`awk '{printf(",%s ",$0)}' shouldBeInValidSiteIds.out`
	SITEIDS=${SITEIDS#,}
	SITEIDS=${SITEIDS%,}
	
	if [[ "" == $SITEIDS ]];then
		return 0
	fi
	
	sql="update beidouext.tc_site_trade set valid=0 where siteid in ($SITEIDS);"  
	runsql_xdb "$sql"
	if [ "$?" -ne "0" ]; then
        hit "Failed: updateTcSiteInvalidToDb"
	fi
}

##################################################################################







if [ ! -d $SITE_NEW_TRADE_PATH ]; then
    mkdir -p $SITE_NEW_TRADE_PATH
fi
cd ${SITE_NEW_TRADE_PATH}

if [ $IS_APPEND_SITE_NEW_TRADE -eq 1 ]; then
    if [ -f "${SITE_NEW_TRADE_FILE}" ]; then
		if [ ! -f "${SITE_NEW_TRADE_BACKUP_FILE}" ]; then
			cp ${SITE_NEW_TRADE_FILE} ${SITE_NEW_TRADE_BACKUP_FILE}						
		fi
		rm ${SITE_NEW_TRADE_FILE}
    fi
    msg="��ȡ${SITE_NEW_TRADE_URL}ʧ��"
    wget -t 3 -q ${SITE_NEW_TRADE_URL} -O ${SITE_NEW_TRADE_FILE}
    alert $? ${msg}
fi

if [ ! -d $TC_SITE_PATH ]; then
    mkdir -p $TC_SITE_PATH
fi
cd ${TC_SITE_PATH}

if [ $IS_APPEND_TC_SITE -eq 1 ]; then
    if [ -f "${TC_SITE_FILE}" ]; then
		if [ ! -f "${TC_SITE_BACKUP_FILE}" ]; then
			cp ${TC_SITE_FILE} ${TC_SITE_BACKUP_FILE}						
		fi
		rm ${TC_SITE_FILE}
    fi
    msg="��ȡ${TC_SITE_FILE_GET}ʧ��"
    ${TC_SITE_FILE_GET} -O ${TC_SITE_FILE}
    alert $? ${msg}
	cp ${TC_SITE_PATH}/${TC_SITE_FILE} ${EXPORT_PATH}/${TC_SITE_FILE}
fi

cd ${EXPORT_PATH}

# backup
if [ -f "${EXPORT_FILE}" ]; then
		if [ ! -f "${EXPORT_BACKUP_FILE}" ]; then
			cp ${EXPORT_FILE} ${EXPORT_BACKUP_FILE}						
		fi
		rm ${EXPORT_FILE}
fi

if [ -f "${EXPORT_SITE_NEW_TRADE_FILE}" ]; then
		if [ ! -f "${EXPORT_SITE_NEW_TRADE_BACKUP_FILE}" ]; then
			cp ${EXPORT_SITE_NEW_TRADE_FILE} ${EXPORT_SITE_NEW_TRADE_BACKUP_FILE}						
		fi
		rm ${EXPORT_SITE_NEW_TRADE_FILE}
fi

if [ -f "${EXPORT_ALL_SITE_FILE}" ]; then
		if [ ! -f "${EXPORT_ALL_SITE_BACKUP_FILE}" ]; then
			cp ${EXPORT_ALL_SITE_FILE} ${EXPORT_ALL_SITE_BACKUP_FILE}						
		fi
		rm ${EXPORT_ALL_SITE_FILE}
fi

runsql_xdb_read "select t.siteid, t.siteurl, t.firsttradeid, t.secondtradeid, t.valid from beidouext.tc_site_trade t  order by t.firsttradeid asc, t.secondtradeid asc, t.siteid asc" ./tcSiteInDb.dat
if [ "$?" -ne "0" ]; then
        hit "Failed: export all tc union site info"
fi

runsql_xdb_read "select t.siteid, t.siteurl, t.firsttradeid, t.secondtradeid from beidouext.unionsite t where t.valid != 0 order by t.firsttradeid asc, t.secondtradeid asc, t.siteid asc" ./${EXPORT_FILE}
if [ "$?" -ne "0" ]; then
        hit "Failed: export all union site info"
fi

#check and md5
if [ ! -f "${EXPORT_FILE}" ]; then
	hit "${EXPORT_FILE} file not found"
fi

#add \t in last postion
mv ${EXPORT_FILE} ${EXPORT_FILE}.tmp

awk '{mytemp=(","$4",");if($3=="'$FIRST_TRADE_OTHER'" || index("'$SECOND_TRADE_OTHER_LIST'",mytemp)>0){printf("%s\t%s\t0\t0\t\n", $1, $2) }else{printf("%s\t%s\t%s\t%s\t\n", $1, $2, $3, $4)}}' ${EXPORT_FILE}.tmp > ${EXPORT_FILE}

rm ${EXPORT_FILE}.tmp

if [ ! -f "${EXPORT_FILE}" ]; then
	hit "${EXPORT_FILE} file not found"
fi

md5sum ${EXPORT_FILE} > ${EXPORT_MD5_FILE}


#add site new trade whiteurl and all file
if [ $IS_APPEND_SITE_NEW_TRADE -eq 1 ]; then
if [ -f "${EXPORT_SITE_NEW_TRADE_BACKUP_FILE}" ] ;then
    awk -v site_new_trade_file=${EXPORT_SITE_NEW_TRADE_BACKUP_FILE} 'BEGIN{while (getline l < site_new_trade_file > 0) {split(l,ls);if(ls[1]>max)max=ls[1];a[ls[2]]=ls[1];b[ls[2]]=l} max+=1;} {if(a[$1]<=0) {print max"\t"$0;max+=1} else {print b[$1]}}' ${SITE_NEW_TRADE_PATH}/${SITE_NEW_TRADE_FILE} > ${EXPORT_SITE_NEW_TRADE_FILE}
    #cat ${EXPORT_SITE_NEW_TRADE_BACKUP_FILE} >> ${EXPORT_SITE_NEW_TRADE_FILE}
else
    awk -v start=1000000 '{print start"\t"$0;start+=1}' ${SITE_NEW_TRADE_PATH}/${SITE_NEW_TRADE_FILE} > ${EXPORT_SITE_NEW_TRADE_FILE}
fi
if [ -f "${EXPORT_SITE_NEW_TRADE_FILE}" ] ;then
    md5sum ${EXPORT_SITE_NEW_TRADE_FILE} > ${EXPORT_SITE_NEW_TRADE_MD5_FILE}
    cp ${EXPORT_FILE} ${EXPORT_ALL_SITE_FILE}
    cat ${EXPORT_SITE_NEW_TRADE_FILE} >> ${EXPORT_ALL_SITE_FILE} 
    if [ -f "${EXPORT_ALL_SITE_FILE}" ] ;then
        md5sum ${EXPORT_ALL_SITE_FILE} > ${EXPORT_ALL_SITE_MD5_FILE}
    fi
fi
fi

if [ $IS_APPEND_TC_SITE -eq 1 ]; then
	appendTcSite
fi

exit 0

