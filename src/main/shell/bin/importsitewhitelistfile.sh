#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/unionsite.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importsitewhitelistfile.sh
reader_list=zhuqian

LOG_FILE=${LOG_PATH}/importsitewhitefile.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录${SITE_DATA_PATH}失败"
cd ${SITE_DATA_PATH}
alert $? "${msg}"

#备份百度自有流量网站配置
CONF_SH=${WHITE_SITE_FILE}
[ -f "${CONF_SH}" ] && mv $CONF_SH ${WHITE_SITE_FILE_BAK}
CONF_SH=${WHITE_SITE_FILE_MD5}
[ -f "${CONF_SH}" ] && rm $CONF_SH 

#抓取文件并验证MD5
msg="wget文件${WHITE_SITE_FILE}失败"
wget -q  ${WHITE_SITE_URL}/${WHITE_SITE_FILE}
alert $? "${msg}"

msg="wget文件${WHITE_SITE_FILE_MD5}失败"
wget  -q ${WHITE_SITE_URL}/${WHITE_SITE_FILE_MD5}
alert $? "${msg}"

msg="${WHITE_SITE_FILE}文件的md5校验失败"
md5sum -c ${WHITE_SITE_FILE_MD5}
alert $? "${msg}"

msg="${WHITE_SITE_FILE}文件为空，请PM检查"
size=`cat ${WHITE_SITE_FILE} | wc -w`
if [ ${size} -eq 0 ]; then
    mv ${WHITE_SITE_FILE} ${WHITE_SITE_FILE}.error
    alert 1 "${msg}"
fi

msg="${WHITE_SITE_FILE}文件列数不为15，请PM检查"
for nf in `awk -F'\t' '{print NF}' ${WHITE_SITE_FILE}`; do
    if [ $nf -ne 15 ]; then
        mv ${WHITE_SITE_FILE} ${WHITE_SITE_FILE}.error    
        alert 1 "${msg}" 
    fi
done

#msg="${WHITE_SITE_FILE}文件中存在行业分类不为260或270的记录，请PM检查"
#awk -F'\t' '{print $6}' ${WHITE_SITE_FILE} > tmpfile.$$
#while read trade
#do
#    if [ -z "$trade" ] || ( [ $trade -ne 260 ] && [ $trade -ne 270 ] ); then 
#        mv ${WHITE_SITE_FILE} ${WHITE_SITE_FILE}.error
#        alert 1 "${msg}"
#    fi
#done <tmpfile.$$
#rm -rf tmpfile.$$
