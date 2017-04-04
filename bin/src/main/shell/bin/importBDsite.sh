#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/unionsite.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=stat_sitelink.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importBDsite.sh
reader_list=zengyunfeng,liangshimu
TODAY=`date +"%Y-%m-%d"`
LOG_FILE=${LOG_PATH}/importbdsite.log.${TODAY}
Q_CACHE_FILE_NAME=${BEIDOU_DATA_PATH}/qcachefilename

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_DATA_PATH}
mkdir -p ${BEIDOU_DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

#execute extractclickurl
msg="执行extractclickurl失败，请查看错误日志[stat_sitelink.log],该异常不影响线上服务"
extractClickUrl >> ${LOG_PATH}/stat_sitelink.log 2>&1
alert_return $? "${msg}"

msg="进入数据目录${SITE_DATA_PATH}失败"
cd ${SITE_DATA_PATH}
alert $? "${msg}"

#抓取文件
msg="wget文件${STATFILE_PRI}${FILE_SUF}失败"
wget -q -t$MAX_RETRY  ${STATFILE_URL_DATA}${sitestat_embeded} -O ${STATFILE_PRI}${FILE_SUF}
alert $? "${msg}"

msg="wget文件${STATFILE_PRI}${FILE_SUF}.md5失败"
wget  -q -t$MAX_RETRY  ${STATFILE_URL_MD5}${sitestat_embeded} -O $sitestat_embeded_md5
alert $? "${msg}"
awk -vfname="${STATFILE_PRI}${FILE_SUF}" '{print $2 "  " fname}' $sitestat_embeded_md5 > ${STATFILE_PRI}${FILE_SUF}.md5
alert $? "${msg}"

msg="${STATFILE_PRI}${FILE_SUF}文件的md5校验失败"
md5sum -c ${STATFILE_PRI}${FILE_SUF}.md5
alert $? "${msg}"

msg="wget文件${STATFILE_PRI}${FILE_SUF}${FLOW_SUF}失败"
wget -q  -t$MAX_RETRY ${STATFILE_URL_DATA}${sitestat_xuanfu} -O ${STATFILE_PRI}${FILE_SUF}${FLOW_SUF}
alert $? "${msg}"

msg="wget文件${STATFILE_PRI}${FILE_SUF}${FLOW_SUF}.md5失败"
wget  -q -t$MAX_RETRY ${STATFILE_URL_MD5}${sitestat_xuanfu} -O $sitestat_xuanfu_md5
alert $? "${msg}"
awk -vfname="${STATFILE_PRI}${FILE_SUF}${FLOW_SUF}" '{print $2 "  " fname}' $sitestat_xuanfu_md5 > ${STATFILE_PRI}${FILE_SUF}${FLOW_SUF}.md5
alert $? "${msg}"

msg="${STATFILE_PRI}${FILE_SUF}${FLOW_SUF}文件的md5校验失败"
md5sum -c ${STATFILE_PRI}${FILE_SUF}${FLOW_SUF}.md5
alert $? "${msg}"

msg="wget文件${STATFILE_PRI}${FILE_SUF}${FILM_SUF}失败"
wget -q  -t$MAX_RETRY ${STATFILE_URL_DATA}${sitestat_tiepian} -O ${STATFILE_PRI}${FILE_SUF}${FILM_SUF}
alert $? "${msg}"

msg="wget文件${STATFILE_PRI}${FILE_SUF}${FILM_SUF}.md5失败"
wget  -q -t$MAX_RETRY ${STATFILE_URL_MD5}${sitestat_tiepian} -O $sitestat_tiepian_md5
alert $? "${msg}"
awk -vfname="${STATFILE_PRI}${FILE_SUF}${FILM_SUF}" '{print $2 "  " fname}' $sitestat_tiepian_md5 > ${STATFILE_PRI}${FILE_SUF}${FILM_SUF}.md5
alert $? "${msg}"
 
msg="${STATFILE_PRI}${FILE_SUF}${FILM_SUF}文件的md5校验失败"
md5sum -c ${STATFILE_PRI}${FILE_SUF}${FILM_SUF}.md5
alert $? "${msg}"

msg="wget文件${IPSTATFILE_PRI}${FILE_SUF}失败"
wget -q  -t$MAX_RETRY ${STATFILE_URL_DATA}${siteinfo} -O ${IPSTATFILE_PRI}${FILE_SUF}
alert $? "${msg}"

msg="wget文件${IPSTATFILE_PRI}${FILE_SUF}.md5失败"
wget  -q -t$MAX_RETRY ${STATFILE_URL_MD5}${siteinfo} -O ${siteinfo_md5}
awk -vfname="${IPSTATFILE_PRI}${FILE_SUF}" '{print $2 "  " fname}' $siteinfo_md5 > ${IPSTATFILE_PRI}${FILE_SUF}.md5
alert $? "${msg}"

msg="${IPSTATFILE_PRI}${FILE_SUF}文件的md5校验失败"
md5sum -c ${IPSTATFILE_PRI}${FILE_SUF}.md5
alert $? "${msg}"

mv ${IPSTATFILE_PRI}${FILE_SUF} ${STATFILE_PRI}${FILE_SUF}.ipcookie


msg="进入工作目录${BIN_PATH}失败"
cd ${BIN_PATH}
alert $? "${msg}"

Xms_Para=8192m
Xmx_Para=10240m

msg="导入beidou站点Q值发生异常,请使用恢复脚本进行恢复"
java -Xms${Xms_Para} -Xmx${Xmx_Para} -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.BDSiteImporter -q ${Q_CACHE_FILE_NAME} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf

# if the relt of "java" is wrong then send error message
alert $? "${msg}"

msg="统计站点数据发生异常,请使用恢复脚本进行恢复"
java -Xms${Xms_Para} -Xmx${Xmx_Para} -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.BDSiteImporter -s ${SITE_DATA_PATH}/${STATFILE_PRI}${FILE_SUF} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf

# if the relt of "java" is wrong then send error message
alert $? "${msg}"

msg="计算站点平均值发生异常,请使用恢复脚本进行恢复"
java -Xms${Xms_Para} -Xmx${Xmx_Para} -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.BDSiteImporter -a 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
alert $? "${msg}"

Q_CATCH_FILE=`cat ${Q_CACHE_FILE_NAME}`
if [ -z ${Q_CATCH_FILE} ] ; then
    alert 1 "q值缓存文件名为空"；
fi
msg="导入beidou站点发生异常,请使用恢复脚本进行恢复"
java -Xms${Xms_Para} -Xmx${Xmx_Para} -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.BDSiteImporter -c ${Q_CATCH_FILE} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf

# if the relt of "java" is wrong then send error message
alert $? "${msg}"


