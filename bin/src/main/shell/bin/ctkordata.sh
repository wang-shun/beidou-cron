#!/bin/sh

source ../conf/config.inc.sh
source ../conf/ctkordata.inc.sh

source ../lib/alert.sh
source ../conf/alert.inc.sh

mkdir -p "${LOCAL_CTWORD_PATH}"

#export ctword file
SQL=""
SQL=${SQL}" SELECT groupId,wordId,srch,click "
SQL=${SQL}" FROM   beidoustat.stat_kt_${L1DAY} "
TMP_CTWORD_DATA="${LOCAL_CTWORD_PATH}/ctword${L1DAY}.txt"
${MYSQL_BIN} -e "${SQL}" > ${TMP_CTWORD_DATA}

#success
exit 0
