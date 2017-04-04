#!/bin/bash

#@file: beidou_blacklist
#@author: zhangpingan
#@date: 2011-11-30
#@version: 1.0.0.0
#@brief: CT AND QT BlackList
#@modify: wangchongjie since 2012.12.10 for cpweb525

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/beidou_blacklist.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

program=beidou_blacklist.sh
reader_list=zhangpingan
LOG_FILE=${LOG_PATH}/beidou_blacklist.log

function check_conf()
{
	if ! [[ $MAX_RETRY ]]
	then
		echo "Conf[MAX_RETRY] is empty or its value is invalid"
		return 1
	fi

	if ! [ ${BLACK_PATH} ]
	then
		echo "Conf[BLACK_PATH] is empty or its value is invalid"
		return 1
	fi

	if ! [ ${REMOTE_SERVER_NAME} ]
	then
		echo "Conf[REMOTE_SERVER_NAME] is empty or its value is invalid"
		return 1
	fi

	if ! [ ${REMOTE_SERVAER_PATH} ]
	then
		echo "Conf[REMOTE_SERVAER_PATH] is empty or its value is invalid"
		return 1
	fi

	if ! [ ${BLACKLIST_FILE} ]
	then
		echo "Conf[BLACKLIST_FILE] is empty or its value is invalid"
		return 1
	fi
        if ! [ ${DBLIST_FILE} ]
        then
                echo "Conf[DBLIST_FILE] is empty or its value is invalid"
                return 1
        fi
	
	return 0
}

function check_path()
{
        if ! [ -e $BLACK_PATH ]
	then
             mkdir -p $BLACK_PATH
	     if [ $? -ne 0 ]
	       then
	       echo "Fail to mkdir [$BLACK_PATH]!" >> $LOG_FILE
	       return 1
	     fi
        fi
	return 0	
}

function check_file()
{
        if ! [ -e $BLACKLIST_FILE ]
        then
              echo "Empty File $BLACKLIST_FILE" >> $LOG_FILE
              return 1
        fi

        #Check File
        currentlines=`wc -l $BLACKLIST_FILE | cut -d' ' -f 1`
        checklines=`awk '/^[0-9]+$/{print NR}' $BLACKLIST_FILE | wc -l | cut -d' ' -f 1`
        if  [ $currentlines -ne $checklines ]
        then
              echo "Error Lines Existed In $BLACKLIST_FILE" >> $LOG_FILE
              return 1
        fi
        return 0
}



check_conf
alert $? "Error Configuration"

check_path
alert $? "Error File Path"

	
timeNow=`date +%Y%m%d%H%M`
echo "Update Black List At:${timeNow}" >> ${LOG_FILE}
	

timestamp=`date +%Y%m%d%H`

cd ${BLACK_PATH}
if [ ! -e ${BLACKLIST_FILE} ]
  then touch ${BLACKLIST_FILE}
fi
if [ ! -e ${DBLIST_FILE} ]
  then touch ${DBLIST_FILE}
fi

mv ${BLACKLIST_FILE} ${BLACKLIST_FILE}.${timestamp}
rm -f ${BLACKLIST_FILE}.md5
md5sum ${BLACKLIST_FILE}.${timestamp} > ${BLACKLIST_FILE}.${timestamp}.md5
mv ${DBLIST_FILE} ${DBLIST_FILE}.${timestamp}
md5sum ${DBLIST_FILE}.${timestamp} > ${DBLIST_FILE}.${timestamp}.md5


#Export Black List in DB
runsql_cap_read "select userid from beidoucap.useraccount where ustate=1"  ${BLACK_PATH}/${DBLIST_FILE}
alert $? "Export Black List User Failed"

#Get Black List in Darwin
msg="wget ${BLACKLIST_FILE} Failed"
wget -t 3 -q ftp://${REMOTE_SERVER_NAME}:/${REMOTE_SERVAER_PATH}/${BLACKLIST_FILE}
alert $? "${msg}"

msg="wget ${BLACKLIST_FILE}.md5 Failed"
wget -t 3 -q ftp://${REMOTE_SERVER_NAME}:/${REMOTE_SERVAER_PATH}/${BLACKLIST_FILE}.md5
alert $? "${msg}"

msg="Check Black User List Failed"
md5sum -c ${BLACKLIST_FILE}.md5
alert $? "${msg}"


#diff File

rm -f ${BLACKLIST_FILE}.input
mv ${BLACKLIST_FILE} ${BLACKLIST_FILE}.input
cat ${BLACKLIST_FILE}.input | sort -u |  sed '/^$/d' > ${BLACKLIST_FILE}

check_file
alert $? "Error File Format"

cat ${DBLIST_FILE} ${BLACKLIST_FILE} | sort | uniq -d > tmp.out
cat ${DBLIST_FILE} tmp.out | sort | uniq -c | awk '{if($1 == 1){print $2}}' > update_to_0.tmp
cat ${BLACKLIST_FILE} tmp.out | sort | uniq -c | awk '{if($1 == 1) {print $2}}' > update_to_1.tmp


#Generate User List To Be Updated
up0_list=`awk '{printf(",%s",$1)}' update_to_0.tmp`;
up1_list=`awk '{printf(",%s",$1)}' update_to_1.tmp`;

up0_sql="update beidoucap.useraccount set ustate=0 where userid in(-1"${up0_list}") and ustate=1";
up1_sql="update beidoucap.useraccount set ustate=1 where userid in(-1"${up1_list}") and ustate=0";

echo $up0_sql >> ${LOG_FILE}
echo $up1_sql >> ${LOG_FILE}

#Set Ustate to 0态
#runsql_cap "${up0_sql}"
#alert $? "Set Ustate To 0 Failed"


#runsql_cap "${up1_sql}"
#alert $? "Set Ustate To 1 Failed"

#add by kanghongwei since cpweb455(2012--5-21)
#将QT/CT的恶意提词的用户黑名单导入到beidou历史库表beidoureport.commitkeywordblacklist
function importCommitKeywordBlackList()
{
	msg="drop临时表commitkeywordblacklist_tmp失败"
	runsql_xdb "drop table if exists beidoureport.commitkeywordblacklist_tmp"
	alert $? "${msg}"
	
	msg="建立临时表commitkeywordblacklist_tmp失败"
	runsql_xdb "CREATE TABLE beidoureport.commitkeywordblacklist_tmp( userid int(10) NOT NULL default '0' , PRIMARY KEY (userid)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;"
	alert $? "${msg}"
	
	msg="加载恶意提词黑名单用户列表失败"
	runsql_xdb "load data local infile '${BLACKLIST_FILE}' into table beidoureport.commitkeywordblacklist_tmp"
	alert $? "${msg}"
	
	msg="重命名恶意提词黑名单用户列表的临时表失败"
	runsql_xdb "drop table if exists beidoureport.commitkeywordblacklist; rename table beidoureport.commitkeywordblacklist_tmp to beidoureport.commitkeywordblacklist"
	alert $? "${msg}"
}

echo "import commitKeywordBlackList start." >> ${LOG_FILE}
importCommitKeywordBlackList
echo "import commitKeywordBlackList success." >> ${LOG_FILE}

#Clear History Data
timeDel=`date -d "${HIS_KEEP} day ago" +%Y%m%d%H`
cd ${BLACK_PATH}
rm -f ${BLACKLIST_FILE}.${timeDel}
rm -f ${BLACKLIST_FILE}.${timeDel}.md5
rm -f ${DBLIST_FILE}.${timeDel}
rm -f ${DBLIST_FILE}.${timeDel}.md5

timeNow=`date +%Y%m%d-%H:%M:%m`
echo "Update Black User End:${timeNow}" >> ${LOG_FILE}
