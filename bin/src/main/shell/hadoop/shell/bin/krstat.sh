#!/bin/bash

source /home/work/.bash_profile

source ../conf/config.inc.sh
source ../conf/krstat.inc.sh

source ../lib/alert.sh
source ../conf/alert.inc.sh

mkdir -p "${LOCAL_ADVIEW_PATH}"
mkdir -p "${LOCAL_OUTPUT_PATH}"

ERR_LEVEL=2

#check all input files exist
for LXDAY in ${LXDAYS[*]}
do
	${HADOOP} fs -test -e "${SRC_UFS_PATH}/${LXDAY}/"
	alert $? "${0}-check-ufs-${LXDAY}-not-exists" "${ERR_LEVEL}"
	
	${HADOOP} fs -test -e "${SRC_ADVIEW_PATH}/${LXDAY}/"
	alert $? "${0}-check-adview-${LXDAY}-not-exists" "${ERR_LEVEL}"
done

#generate input paths, separated by comma
UFS_DATA_PATH=""
ADVIEW_DATA_PATH=""
FIRST=1
for LXDAY in ${LXDAYS[*]}
do
	if [ ${FIRST} != 1 ]; then
		UFS_DATA_PATH="${UFS_DATA_PATH},"
		ADVIEW_DATA_PATH="${ADVIEW_DATA_PATH},"
	fi
	UFS_DATA_PATH="${UFS_DATA_PATH}${SRC_UFS_PATH}/${LXDAY}/0000/"
	ADVIEW_DATA_PATH="${ADVIEW_DATA_PATH}${SRC_ADVIEW_PATH}/${LXDAY}/0000/"
	FIRST=0
done

#execute the job
${HADOOP} jar ${JAR_FILE} qtkr -Dmapred.job.name=cm-rank-yangyong01 -Dmapred.job.priority=NORMAL -Dmapred.map.tasks=500 -Dmapred.reduce.tasks=500 -Dmapred.job.map.capacity=500 -Dmapred.job.reduce.capacity=500 "${UFS_DATA_PATH}" "${ADVIEW_DATA_PATH}" "${OUTPUT_PATH}"
alert $? "${0}-job-qtkr-failed" "${ERR_LEVEL}"

#download the output from hdfs
${HADOOP} fs -get "${OUTPUT_PATH}/output/" ${LOCAL_OUTPUT_PATH}
alert $? "${0}-get-output-failed" "${ERR_LEVEL}"

touch "${LOCAL_OUTPUT_PATH}/done"

#success
exit 0

