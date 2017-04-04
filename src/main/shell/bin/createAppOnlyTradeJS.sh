#!/bin/sh

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=createAppOnlyTradeJS.sh
reader_list=wangchongjie

##################################################################################
#config options

APP_TRADE_JS="/bj/BEIDOU/online_appOnlyTrade.js"
CURR_DATE=`date -d "1 day ago" +"%Y%m%d"`
APP_TRADE_FILE=appTradeInfo.dat
APP_TRADE_BACKUP_FILE=appTradeInfo.txt.${CURR_DATE}
APP_TRADE_FILE_GET="wget http://10.42.7.105/dr-mgr/common/downloadpublishfile/trade_list/trade_list_data.txt?authtype=noah"
APP_TRADE_PATH=${DATA_PATH}/import/appTrade
LOG_FILE=${LOG_PATH}/createAppOnlyTradeJS.log

RESULT_FILE=appOnlyTrade.js
#result file example:
#var APP_ONLY_TRADE = [
#    "30",
#    "3001",
#];

# noah data push config
LZOP_CLIENT="/home/work/opbin/lzop";
FTP_PATH="ftp://tc-beidou-cron01.tc.baidu.com";
APP_ONLY_FILE_KEY="/data/0e4ebcbcec004c4283cd9abf4600d111";
##################################################################################

if [ ! -d $APP_TRADE_PATH ]; then
    mkdir -p $APP_TRADE_PATH
fi
cd ${APP_TRADE_PATH}


if [ -f "${APP_TRADE_FILE}" ]; then
	if [ ! -f "${APP_TRADE_BACKUP_FILE}" ]; then
		cp ${APP_TRADE_FILE} ${APP_TRADE_BACKUP_FILE}						
	fi
	rm ${APP_TRADE_FILE}
fi

msg="获取${APP_TRADE_FILE_GET}失败"
${APP_TRADE_FILE_GET} -O ${APP_TRADE_FILE}
alert $? ${msg}

>$RESULT_FILE
echo -e "var APP_ONLY_TRADE = [" >> $RESULT_FILE

#0的一二级行业，2的二级行业为只适用app的
awk '{if($1==0){print $2; print $3} if($1==2)print $3}' ${APP_TRADE_FILE}|sort -u > appOnlyTradeIds.dat

fileLine=`wc -l appOnlyTradeIds.dat|awk '{print $1}'`

#生成js文件
awk -v lineCnt=$fileLine '{if(NR<lineCnt)printf("    \"%s\"\,\n"   ,$1); if(NR==lineCnt)printf("    \"%s\"\n"   ,$1)}' appOnlyTradeIds.dat>> $RESULT_FILE

echo -e "];" >> $RESULT_FILE

md5sum $RESULT_FILE > $RESULT_FILE.md5

# use noah data push
#rm -f ${RESULT_FILE}.lzo
#${LZOP_CLIENT} ${RESULT_FILE} ${RESULT_FILE}.md5 -o ${RESULT_FILE}.lzo
#bscp --setinfo --file ${FTP_PATH}${APP_TRADE_PATH}/${RESULT_FILE}.lzo data:/${APP_ONLY_FILE_KEY}

md5code=`md5sum ${RESULT_FILE} | awk '{print $1}'`
#upload to noahdt
noahdt add ${APP_TRADE_JS} -m md5=${md5code} bscp://${APP_TRADE_PATH}/${RESULT_FILE}

exit 0

