#!/bin/bash
#@author: kanghongwei
#@date: 2013-04-21
#@purpose: refresh beidou.cproplan's wireless_bid_ratio to specified value when users in whitelist.

CONF_SH="./refresh_cproplan_bid_ratio.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

function printLog()
{
    timeNow=`date +%Y%m%d-%H:%M:%S`
    echo "[${timeNow}]:  ${1}"
}

function getWhiteListFile(){
	if [ -f ${WORK_PATH}/${WHITELIST_FILE} ]
	then
		rm -f ${WORK_PATH}/${WHITELIST_FILE}
	fi
	
	if [ -f ${WORK_PATH}/${WHITELIST_FILE_IN_LINE} ]
	then
		rm -f ${WORK_PATH}/${WHITELIST_FILE_IN_LINE}
	fi
	
	if [ -f ${WORK_PATH}/${CPROPLAN_UPDATE_SQL_FILE} ]
	then
		rm -f ${WORK_PATH}/${CPROPLAN_UPDATE_SQL_FILE}
	fi
		
	wget  ${WHITELIST_PATH}/${WHITELIST_FILE} -O ${WORK_PATH}/${WHITELIST_FILE}
	if [ $? -ne 0 ]
	then
		printLog "get ${WHITELIST_FILE} failed."
		exit 1
	else
		printLog "get ${WHITELIST_FILE} success."
	fi
	
	# generate whitelist batch ids file
	sumLineNum=`cat "${WORK_PATH}/${WHITELIST_FILE}" | wc -l`
	
	if [ ${sumLineNum} -lt 1 ]
	then
		printLog "${sumLineNum} is less than 1."
		exit 1
	fi
	
	awk -v outFile="${WORK_PATH}/${WHITELIST_FILE_IN_LINE}" -v maxNum=${sumLineNum}  -v maxNumPerSql="${MAX_UPDATE_NUM_PER_TIME}" '
    {
        if(NR % maxNumPerSql !=  0) 
        {
            if(NR != maxNum){
                printf("%s,",$2) >> outFile;
            }else{
                print $2 >> outFile;
            }
        }else
        {
            printf("%s,",$2) >> outFile;
            print "0" >> outFile;
        }
     }' < "${WORK_PATH}/${WHITELIST_FILE}"
     
     printLog "generate ${WHITELIST_FILE_IN_LINE} success."
     
     # generate sql file
     awk -v outFile="${WORK_PATH}/${CPROPLAN_UPDATE_SQL_FILE}" '
     {
        print "update beidou.cproplan set wireless_bid_ratio = 40 where userid in ( "$0" );"  >> outFile;

     }' < "${WORK_PATH}/${WHITELIST_FILE_IN_LINE}"
     
     printLog "generate ${CPROPLAN_UPDATE_SQL_FILE} success."
	
}

function doDBInsert(){
    if [ -s ${DEL_OBJ_SQL_FILE} ]	
    then
        sql=`cat ${DEL_OBJ_SQL_FILE}`
        mysql -h${addb_mysql_host} -P${addb_mysql_port} -u${addb_mysql_user} -p${addb_mysql_password} -Dbeidou  -e "${sql}"
    fi
}

function refreshDB(){

	if [ ! -s ${WORK_PATH}/${CPROPLAN_UPDATE_SQL_FILE} ]
	then
		printLog "${WORK_PATH}/${CPROPLAN_UPDATE_SQL_FILE} is empty."
		exit 1
	fi
	
	for index in `seq 0 7`
    do
	    printLog "${index} shard start..."
	    ${MYSQL_CLIENT} -h${DB_HOST_BD_MAID_WRITE[$index]} -P${DB_PORT_BD_MAID_WRITE[$index]} -u${DB_USER_BD_MAID_WRITE} -p${DB_PASSWORD_BD_MAID_WRITE} -Dbeidou --default-character-set=gbk -e "source ${WORK_PATH}/${CPROPLAN_UPDATE_SQL_FILE}" &
		printLog "${index} shard end..."
    done

    wait
}

function main(){
		
	getWhiteListFile
	
	refreshDB
	
}

main

