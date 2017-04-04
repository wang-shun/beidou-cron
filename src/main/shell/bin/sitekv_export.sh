#!/bin/bash

#@file: sitekv_export.sh
#@author: zhangpingan
#@date: 2011-12-06
#@version: 1.0.0.0
#@brief: 为数据引擎导出基准及增量数据


CONF_SH=../conf/db.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/sitekv_export.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

program=sitekv_export.sh
reader_list=zhangpingan
LOG_FILE=${LOG_PATH}/sitekv_export.log

function check_conf()
{
	if ! [[ $DB_URL ]]
	then
		echo "Conf[DB_URL] is empty or its value is invalid"
		return 1
	fi
	
	if ! [[ $DB_PORT ]]
	then
		echo "Conf[DB_PORT] is empty or its value is invalid"
		return 1
	fi
	
	if ! [[ $DB_NAME ]]
	then
		echo "Conf[DB_NAME] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $DB_USER ]]
	then
		echo "Conf[DB_USER] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $DB_PWD ]]
	then
		echo "Conf[DB_PWD] is empty or its value is invalid"
		return 1
	fi
	
	if ! [[ $DB_CLIENT ]]
	then
		echo "Conf[DB_CLIENT] is empty or its value is invalid"
		return 1
	fi
	
	if ! [[ $MAX_RETRY ]]
	then
		echo "Conf[MAX_RETRY] is empty or its value is invalid"
		return 1
	fi
	

	if ! [ $DE_CACHE_BASENAME ]
	then
		echo "Conf[DE_CACHE_BASENAME] is empty or its value is invalid"
		return 1
	fi

	if ! [ $DE_CACHE_IMPLNAME ]
	then
		echo "Conf[DE_CACHE_IMPLNAME] is empty or its value is invalid"
		return 1
	fi
	
	return 0
}

function check_path()
{
	if ! [ -e $EXPORT_PATH ]
		then
			mkdir -p $EXPORT_PATH
			if [ $? -ne 0 ]
			then
				echo "Fail to mkdir [$EXPORT_PATH]!">> ${LOG_FILE}
				return 1
	        fi
	 fi


	if ! [ -e $BACKUP_PATH ]
		then
			mkdir -p $BACKUP_PATH
			if [ $? -ne 0 ]
			then
				echo "Fail to mkdir [$BACKUP_PATH]!" >> ${LOG_FILE}
				return 1
			fi
	fi
	
	return 0	
}

check_conf
alert $? "Configuration Check Error"

check_path
alert $? "Data Path Check Error"

	
timeNow=`date +%Y%m%d-%H:%M:%S`
echo "Export DE Data at:${timeNow}" >> ${LOG_FILE}
	
#备份上周期增量文件及md5
timestamp=`date -d "1 hours ago" +%Y%m%d%H`

cd ${EXPORT_PATH}
if [ ! -e ${DE_CACHE_IMPLNAME} ]
  then touch ${DE_CACHE_IMPLNAME}
fi

mv ${DE_CACHE_IMPLNAME} ${DE_CACHE_IMPLNAME}.${timestamp}
md5sum ${DE_CACHE_IMPLNAME}.${timestamp} > ${DE_CACHE_IMPLNAME}.${timestamp}.md5
mv ${EXPORT_PATH}/${DE_CACHE_IMPLNAME}.${timestamp}  ${BACKUP_PATH}/${DE_CACHE_IMPLNAME}.${timestamp}
mv ${EXPORT_PATH}/${DE_CACHE_IMPLNAME}.${timestamp}.md5  ${BACKUP_PATH}/${DE_CACHE_IMPLNAME}.${timestamp}.md5


#导出基准数据
retryCount=0
sucFlag=0
while [[ $retryCount -lt $MAX_RETRY ]] && [[ $sucFlag -eq 0 ]]
do
	retryCount=$(($retryCount+1))
	runsql_xdb_read "select sign, literal from beidouurl.sitekv" $EXPORT_PATH/${DE_CACHE_BASENAME}.tmp
	if [ $? -eq 0 ]
	then
		sucFlag=1
	else
		sleep 0.5
	fi
done
	
if [ $sucFlag -eq 0 ]
 then
       echo "Export DE Base Data Error" >> ${LOG_FILE}
       alert 1 "Export DE Base Data Error"
 else
       echo "Export DE Base Data Success" >> ${LOG_FILE}
fi

cd $EXPORT_PATH 

#重命名上周期增量文件及md5


if [ ! -e "${DE_CACHE_BASENAME}" ]
   then touch ${DE_CACHE_BASENAME}
   md5sum ${DE_CACHE_BASENAME} > ${DE_CACHE_BASENAME}.md5
fi

mv ${DE_CACHE_BASENAME} ${DE_CACHE_BASENAME}.${timestamp}
md5sum ${DE_CACHE_BASENAME}.${timestamp} > ${DE_CACHE_BASENAME}.${timestamp}.md5

#diff文件得到增量数据
awk -F'\t' '
ARGIND==1{
 sitekv[$1]=$2;
}
ARGIND==2{
 if($1 in sitekv)
 {}
 else
 {
   printf("%s\n",$0);
 }
}' ${DE_CACHE_BASENAME}.${timestamp} ${DE_CACHE_BASENAME}.tmp > ${DE_CACHE_IMPLNAME}

alert $? "Diff DE Implement Data Error"

md5sum  ${DE_CACHE_IMPLNAME} > ${DE_CACHE_IMPLNAME}.md5
alert $? "Generate MD5 For Implement Data Error"


#备份上周期基准文件及md5
if [ -e "${EXPORT_PATH}/${DE_CACHE_BASENAME}.${timestamp}" ];then
mv ${EXPORT_PATH}/${DE_CACHE_BASENAME}.${timestamp} ${BACKUP_PATH}/${DE_CACHE_BASENAME}.${timestamp}
fi
if [ -e "${EXPORT_PATH}/${DE_CACHE_BASENAME}.${timestamp}.md5" ];then
mv ${EXPORT_PATH}/${DE_CACHE_BASENAME}.${timestamp}.md5 ${BACKUP_PATH}/${DE_CACHE_BASENAME}.${timestamp}.md5
fi

#生成基准文件及md5
mv ${DE_CACHE_BASENAME}.tmp ${DE_CACHE_BASENAME}
md5sum ${DE_CACHE_BASENAME} > ${DE_CACHE_BASENAME}.md5
alert $? "Generate MD5 For Base Data Error"

#Clear历史DE数据文件
timeDel=`date -d "1 day ago" +%Y%m%d%H`
cd ${BACKUP_PATH}
rm -f ${DE_CACHE_BASENAME}.${timeDel}*
rm -f ${DE_CACHE_IMPLNAME}.${timeDel}*

timeNow=`date +%Y%m%d-%H:%M:%S`
echo "Export DE Sitekv Data End:${timeNow}" >> ${LOG_FILE}
