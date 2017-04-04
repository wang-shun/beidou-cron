#!/bin/sh

source ../conf/config.inc.sh
source ../conf/kordata.inc.sh

source ../lib/alert.sh
source ../conf/alert.inc.sh

mkdir -p "${LOCAL_QTWORD_PATH}"

#export qtword file
SQL=""
SQL=${SQL}" SELECT groupId,wordId,srch,click "
SQL=${SQL}" FROM   beidoustat.stat_qt_${L1DAY} "
TMP_QTWORD_DATA="${LOCAL_QTWORD_PATH}/qtword${L1DAY}.txt"
${MYSQL_BIN} -e "${SQL}" > ${TMP_QTWORD_DATA}

#success
exit 0
