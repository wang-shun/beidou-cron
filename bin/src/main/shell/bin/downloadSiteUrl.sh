#!/bin/sh
#抓取url展现数据
#$1 : 可传递具体的日期参数，格式为:yyyyMMdd 

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/siteurl.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "


program=downloadSiteUrl.sh
reader_list=yang_yun

LOG_FILE=${LOG_PATH}/downloadSiteUrl/downloadSiteUrl.log.`date +%Y%m%d`
TIME1=`date +%s`

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${LOG_PATH}/downloadSiteUrl
mkdir -p ${SITE_DATA_PATH}
mkdir -p ${SITE_DATA_CACHE}

msg="进入数据目录${SITE_DATA_PATH}失败"
cd ${SITE_DATA_PATH}
alert $? "${msg}"
echo "`date +"%Y-%m-%d %H:%M:%S"`,进入数据目录">>${LOG_FILE}

#抓取文件
[ -f ${WHITE_FILE} ] && mv ${WHITE_FILE} ${WHITE_FILE}.bak
[ -f ${WHITE_FILE}.md5 ] && mv ${WHITE_FILE}.md5 ${WHITE_FILE}.bak
msg="wget文件${WHITE_FILE_URL}${WHITE_FILE}.md5失败"
wget -q -t$MAX_RETRY  ${WHITE_FILE_URL}/${WHITE_FILE}.md5
alert $? "${msg}"

msg="wget文件${WHITE_FILE_URL}${WHITE_FILE}失败"
wget -q  -t$MAX_RETRY  ${WHITE_FILE_URL}/${WHITE_FILE}
alert $? "${msg}"

msg="${WHITE_FILE_URL}${WHITE_FILE}生成md5失败"
md5sum -c ${WHITE_FILE}.md5
alert $? "${msg}"

#抓取展现数据
[ -f ${SITE_URL_FILE} ] && rm ${SITE_URL_FILE}
msg="wget url统计数据${SITE_URL}失败"
wget -q -t$MAX_RETRY  --limit-rate=10m "${SITE_URL}normal${SITE_URL2}" -O ${SITE_URL_FILE}
alert $? "${msg}"

msg="wget url统计数据${SITE_URL} manifest失败"
wget -q -t$MAX_RETRY  --limit-rate=10m "${SITE_URL}midoutfile&file=@manifest${SITE_URL2}" -O ${SITE_URL_FILE}.manifest
alert $? "${msg}"

msg="wget url统计数据${SITE_URL} manifest.md5失败"
wget -q -t$MAX_RETRY  --limit-rate=10m "${SITE_URL}midoutfile&file=@manifest.md5${SITE_URL2}" -O ${SITE_URL_FILE}.manifest.md5
alert $? "${msg}"

offline_manifest_md5=`md5sum $SITE_URL_FILE".manifest" | awk '{print $1}'` #manifest md5 offline
online_manifest_md5=`awk '{print $1}' $SITE_URL_FILE".manifest.md5"`   #manifest md5 online
if [ "${offline_manifest_md5}" != "${online_manifest_md5}" ]
then
	alert 1 "${SITE_URL_FILE}-manifest文件md5校验失败"
fi

offline_file_size=`du -b $SITE_URL_FILE|awk '{print $1}'` #line count offline
online_file_size=`awk '{print $3}' $SITE_URL_FILE".manifest"`   #line count online
if [ "${offline_file_size}" != "${online_file_size}" ]
then
	alert 1 "${SITE_URL_FILE}-文件大小校验失败"
fi

#去除暂不使用的第一列数据
cut -f2- ${SITE_URL_FILE} > ${SITE_DATA_PATH}/tmp.txt
mv ${SITE_DATA_PATH}/tmp.txt ${SITE_URL_FILE}

msg="url统计数据至少应该有10行"
LINE=`head -n 11 ${SITE_URL_FILE} | wc -l | cut -d' ' -f1`
alert $? "${msg}"
if [ "${LINE}" -lt 10 ] ; then
    alert 1 "${msg}"
fi

TIME2=`date +%s`
echo "抓取URL展现数据耗时`expr ${TIME2} - ${TIME1}`" >>${LOG_FILE}


#按展现url进行排序
msg="按展现url进行排序失败"
sort -T${SITE_DATA_CACHE} -k3,3 -o"${SITE_URL_FILE}.url.sorted" ${SITE_URL_FILE}
alert $? "${msg}"
rm -f ${SITE_URL_FILE}
rm -f ${SITE_URL_FILE}.md5
TIME3=`date +%s`
echo "按展现url进行排序耗时`expr ${TIME3} - ${TIME2}`" >>${LOG_FILE}


#对show_url进行处理，对于第3列展现url一致的行，将其第二列累加，结果追加到每一行的结尾
msg="对show_url进行处理操作失败"
awk 'BEGIN{ 
        pre_url="";
        pre_count=0;
        i=0;
    }{
        if($3==pre_url){
            pre_count=$2+pre_count;
            pre[i++]=$0;
        }else{
                for(k=0;k<i;k++){
                    print pre[k]"\t"pre_count;
                }
                i=0;
                pre[i++]=$0;
                pre_url=$3;
                pre_count=$2
        }
    }
    END{
        for(k=0;k<i;k++){
            print pre[k]"\t"pre_count;
        }
    }'	${SITE_URL_FILE}.url.sorted >${SITE_URL_FILE}.added
alert $? "${msg}"
rm -f ${SITE_URL_FILE}.url.sorted
TIME4=`date +%s`
echo "对show_url进行处理操作耗时`expr ${TIME4} - ${TIME3}`" >>${LOG_FILE}


#对show_url.added进行排序处理
msg="对show_url.added进行排序处理失败"
sort -s -r -n -T${SITE_DATA_CACHE} -t"	" -k9,9 -o"${SITE_URL_FILE}.retrive.sorted" ${SITE_URL_FILE}.added
alert $? "${msg}"
rm -f ${SITE_URL_FILE}.added
TIME5=`date +%s`
echo "对show_url.added进行排序处理耗时`expr ${TIME5} - ${TIME4}`" >>${LOG_FILE}


#对show_url.added进行排序处理
msg="对show_url.added进行排序处理失败"
sort -s -f -T${SITE_DATA_CACHE} -k1,1 -o"${SITE_URL_FILE}.domain.sorted" "${SITE_URL_FILE}.retrive.sorted"
alert $? "${msg}"
rm -f ${SITE_URL_FILE}.retrive.sorted
TIME6=`date +%s`
echo "对show_url.added进行排序处理耗时`expr ${TIME6} - ${TIME5}`" >>${LOG_FILE}


#输出数据
msg="输出数据失败"
awk -F"\t" 'BEGIN{OFS="\t"}{print $3,$4,$9,$5,$6,$7,$8}' "${SITE_URL_FILE}.domain.sorted" > ${SITE_URL_FILE}.${STAT_DATE}
alert $? "${msg}"
rm -f ${SITE_URL_FILE}.domain.sorted
TIME7=`date +%s`
echo "输出数据耗时`expr ${TIME7} - ${TIME6}`" >>${LOG_FILE}


if ! [ -n "$1" ] ;then 
	find ${SITE_DATA_PATH} -type f -mtime +${DATA_STORE_DAYS} -exec  rm -rf {} \;
fi
TIME8=`date +%s`
#total time cost
echo "`date +"%Y-%m-%d %H:%M:%S"`,total time cost `expr ${TIME8} - ${TIME1}`" >>${LOG_FILE}
