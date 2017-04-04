#!/bin/sh
#����չ��url����

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/siteurl.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=importSiteUrl.sh
reader_list=yang_yun

TABLE_NAME_URL_SIGN="siteurl"
TABLE_NAME_URL_STAT="siteurlstat"
TABLE_NAME_SITE="mainsitesize"

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_DATA_PATH}
mkdir -p ${SITE_DATA_CACHE}

msg="���빤��Ŀ¼${BIN_PATH}ʧ��"
cd ${BIN_PATH}
alert $? "${msg}"

START_TIME=`date +%s`
msg="����չ��url����ʧ��"
java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.UrlStatImporter -w ${SITE_DATA_PATH}/${WHITE_FILE} -u ${SITE_DATA_PATH}/${SITE_URL_FILE}.${STAT_DATE} -o ${SITE_DATA_CACHE} -p ${URL_TABLE_COUNT} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf

# if the relt of "java" is wrong then send error message
alert $? "${msg}"
END_TIME=`date +%s`
echo "Database file generating time(seconds):`expr ${END_TIME} - ${START_TIME}`" >>${LOG_FILE}

TODAY=`date -d"-0 days" +%Y%m%d`;
FIVEDAYAGO=`date -d"-5 days" +%Y%m%d`;

cd ${SITE_DATA_CACHE}
[ -f "${SITE_DATA_CACHE}/${TABLE_NAME_SITE}" ] || alert 1 "File ${SITE_DATA_CACHE}/${TABLE_NAME_SITE} does not exist"
#Create temp table
MAX_TABLE_INDEX=`expr $URL_TABLE_COUNT - 1`
for index in `seq 0 ${MAX_TABLE_INDEX}`
do
	START_TIME=`date +%s`
	[ -f "${SITE_DATA_CACHE}/${TABLE_NAME_URL_SIGN}${index}" ] || alert 1 "�ļ�${SITE_DATA_CACHE}/${TABLE_NAME_URL_SIGN}${index}������"
	[ -f "${SITE_DATA_CACHE}/${TABLE_NAME_URL_STAT}${index}" ] || alert 1 "�ļ�${SITE_DATA_CACHE}/${TABLE_NAME_URL_STAT}${index}������"
	msg="�����ļ�${TABLE_NAME_URL_STAT}${index}ʧ��"
	runsql_xdb "
		use beidouurl;
		set charset utf8;
		drop table if exists ${TABLE_NAME_URL_SIGN}${index}_bak;
		drop table if exists ${TABLE_NAME_URL_STAT}${index}_bak;
		create table if not exists ${TABLE_NAME_URL_SIGN}${index} like ${TABLE_NAME_URL_SIGN};
		create table if not exists ${TABLE_NAME_URL_STAT}${index} like ${TABLE_NAME_URL_STAT};
		create table ${TABLE_NAME_URL_SIGN}${index}_bak like ${TABLE_NAME_URL_SIGN};
		create table ${TABLE_NAME_URL_STAT}${index}_bak like ${TABLE_NAME_URL_STAT};
		load data local infile '${SITE_DATA_CACHE}/${TABLE_NAME_URL_SIGN}${index}' into table ${TABLE_NAME_URL_SIGN}${index}_bak (urlsign1,urlsign2,url);
		load data local infile '${SITE_DATA_CACHE}/${TABLE_NAME_URL_STAT}${index}' into table ${TABLE_NAME_URL_STAT}${index}_bak (siteid,urlsign1,urlsign2,displaytype,supporttype,srchs,size);
		"  1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
	alert $? "${msg}"
	END_TIME=`date +%s`
	echo "load���ݿ��ļ�${index}ʱ��(��λΪ��)Ϊ��`expr ${END_TIME} - ${START_TIME}`" >>${LOG_FILE}
	#for ODS/EDW
	cp ${SITE_DATA_CACHE}/${TABLE_NAME_URL_SIGN}${index} ${SITE_DATA_CACHE}/${TABLE_NAME_URL_SIGN}${index}.${TODAY}
	cp ${SITE_DATA_CACHE}/${TABLE_NAME_URL_STAT}${index} ${SITE_DATA_CACHE}/${TABLE_NAME_URL_STAT}${index}.${TODAY}
	cd ${SITE_DATA_CACHE}
	md5sum ${TABLE_NAME_URL_SIGN}${index}.${TODAY} > ${TABLE_NAME_URL_SIGN}${index}.${TODAY}.md5
	md5sum ${TABLE_NAME_URL_STAT}${index}.${TODAY} > ${TABLE_NAME_URL_STAT}${index}.${TODAY}.md5
	rm -rf ${TABLE_NAME_URL_SIGN}${index}.${FIVEDAYAGO}*
	rm -rf ${TABLE_NAME_URL_STAT}${index}.${FIVEDAYAGO}*
done

START_TIME=`date +%s`
msg="�����ļ�${TABLE_NAME_SITE}ʧ��"
	runsql_xdb "
		use beidouurl;
		set charset utf8;
		drop table if exists ${TABLE_NAME_SITE}_bak;
		create table ${TABLE_NAME_SITE}_bak like ${TABLE_NAME_SITE};
		load data local infile '${SITE_DATA_CACHE}/${TABLE_NAME_SITE}' into table ${TABLE_NAME_SITE}_bak (siteid,displaytype,supporttype,size);
		"  1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
alert $? "${msg}"
END_TIME=`date +%s`
echo "load���ݿ��ļ�${TABLE_NAME_SITE}ʱ��(��λΪ��)Ϊ��`expr ${END_TIME} - ${START_TIME}`" >>${LOG_FILE}

#for ODS/EDW
cp ${SITE_DATA_CACHE}/${TABLE_NAME_SITE} ${SITE_DATA_CACHE}/${TABLE_NAME_SITE}.${TODAY}
cd ${SITE_DATA_CACHE}
md5sum ${TABLE_NAME_SITE}.${TODAY} > ${TABLE_NAME_SITE}.${TODAY}.md5
rm -rf ${TABLE_NAME_SITE}.${FIVEDAYAGO}*

#switch temp table and formal table
for index in `seq 0 ${MAX_TABLE_INDEX}`
do
	START_TIME=`date +%s`
	msg="��������${TABLE_NAME_URL_STAT}${index}ʧ��"
	runsql_xdb "
		use beidouurl;
		set charset utf8;
		rename table ${TABLE_NAME_URL_SIGN}${index} to ${TABLE_NAME_URL_SIGN}${index}_tmp,
				${TABLE_NAME_URL_SIGN}${index}_bak to ${TABLE_NAME_URL_SIGN}${index},
				${TABLE_NAME_URL_SIGN}${index}_tmp to ${TABLE_NAME_URL_SIGN}${index}_bak,
				${TABLE_NAME_URL_STAT}${index} to ${TABLE_NAME_URL_STAT}${index}_tmp,
				${TABLE_NAME_URL_STAT}${index}_bak to ${TABLE_NAME_URL_STAT}${index},
				${TABLE_NAME_URL_STAT}${index}_tmp to ${TABLE_NAME_URL_STAT}${index}_bak;
		"  1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
	alert $? "${msg}"
	END_TIME=`date +%s`
	echo "rename���ݿ��ļ�${index}ʱ��(��λΪ��)Ϊ��`expr ${END_TIME} - ${START_TIME}`" >>${LOG_FILE}
done

START_TIME=`date +%s`
msg="��������${TABLE_NAME_SITE}ʧ��"
	runsql_xdb "
		use beidouurl;
		set charset utf8;
		rename table ${TABLE_NAME_SITE} to ${TABLE_NAME_SITE}_tmp,
				${TABLE_NAME_SITE}_bak to ${TABLE_NAME_SITE},
				${TABLE_NAME_SITE}_tmp to ${TABLE_NAME_SITE}_bak;
		"  1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
alert $? "${msg}"
END_TIME=`date +%s`
echo "rename���ݿ��ļ�${TABLE_NAME_SITE}ʱ��(��λΪ��)Ϊ��`expr ${END_TIME} - ${START_TIME}`" >>${LOG_FILE}
