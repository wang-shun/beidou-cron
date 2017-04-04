#!/bin/sh
CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/site_and_ipmap_export_4ufs.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${SITE_UFS_EXPORT_FILEPATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

program=site_and_ipmap_export_4ufs.sh
reader_list=liangshimu
msg="${program}:error to export siteinfo for ufs from db"

#generate siteinfo
runsql_xdb_read "select siteid,siteurl,isdomain from beidouext.unionsite where valid=1" "${SITE_UFS_EXPORT_FILEPATH}/${SITE_UFS_EXPORT_FILENAME}"
alert $? "${msg}"

#generate ipmap
awk -F"\t" '
{
  if ($1==1 && $4>=1 && $4<=34) {
    if ($5 > 0) {
      print $2 "\t" $3 "\t" $5
    } else {
      print $2 "\t" $3 "\t" $4
    }
  }

}
' ${DATA_PATH}/${SOURCE_IPMAP_FILE} > ${SITE_UFS_EXPORT_FILEPATH}/${DEST_IPMAP_FILE}

#generate md5
cd ${SITE_UFS_EXPORT_FILEPATH}
md5sum ${SITE_UFS_EXPORT_FILENAME} > ${SITE_UFS_EXPORT_FILENAME}.md5

md5sum ${DEST_IPMAP_FILE} > ${DEST_IPMAP_FILE}.md5
