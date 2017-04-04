#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/unionsite.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


program=importUnionSite.sh
reader_list=zengyunfeng,zhuqian

LOG_FILE=${LOG_PATH}/importunionsite.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_DATA_PATH}
mkdir -p ${BEIDOU_DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录${SITE_DATA_PATH}失败"
cd ${SITE_DATA_PATH}
alert $? "${msg}"

#备份union站点文件
CONF_SH=${UNION_FILE}
[ -f "${CONF_SH}" ] && mv $CONF_SH ${UNION_FILE_BAK}
CONF_SH=${UNION_FILE_NEW_MD5}
[ -f "${CONF_SH}" ] && rm $CONF_SH 

#抓取文件并验证MD5
msg="wget文件${UNION_FILE}失败"
wget -q  ${UNION_URL}${UNION_FILE_NEW} 
alert $? "${msg}"

msg="wget文件${UNION_FILE_MD5}失败"
wget  -q ${UNION_URL}${UNION_FILE_NEW_MD5} 
alert $? "${msg}"

msg="${UNION_FILE}文件的md5校验失败"
md5sum -c ${UNION_FILE_NEW_MD5}
alert $? "${msg}"

mv ${UNION_FILE_NEW} ${UNION_FILE}




msg="追加百度自有流量文件${CENTRAL_SITE_WHITE_LIST_FILE}失败"
if [ -f "${CENTRAL_SITE_WHITE_LIST_FILE}" ]
then
###modify by liangshimu@cpweb-250, 取前14列，同时追加两个1到最后二列，以配合union一二级域名合并
    #cat ${CENTRAL_SITE_WHITE_LIST_FILE} >> ${UNION_FILE}
    awk -F"\t" 'BEGIN{ORS="\n";OFS="\t"}{line="";for(i=1;i<=14;i++){line=line$i"\t"};line=line"1\t1";print line}' ${CENTRAL_SITE_WHITE_LIST_FILE} >> ${UNION_FILE}
    alert $? "${msg}"
fi

msg="进入工作目录${BIN_PATH}失败"
cd ${BIN_PATH}
alert $? "${msg}"

msg="导入union站点发生异常"
java -Xms512m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.UnionSiteImporter >> ${LOG_FILE}

# if the relt of "java" is wrong then send error message
alert $? "${msg}"


