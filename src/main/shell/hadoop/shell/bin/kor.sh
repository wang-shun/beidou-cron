#!/bin/sh

source ../conf/config.inc.sh
source ../conf/kor.inc.sh
source ../lib/alert.sh
source ../conf/alert.inc.sh

mkdir -p "${LOCAL_OUTPUT_PATH}"

#check all input files exist
for LXDAY in ${LXDAYS[*]}
do
	${HADOOP} fs -test -e "${SRC_KTWORD_PATH}/${LXDAY}/"
	alert $? "${0}-check-ktword-${LXDAY}-not-exists"
done

#generate input paths, separated by comma
KTWORD_DATA_PATH=""
FIRST=1
for LXDAY in ${LXDAYS[*]}
do
	if [ ${FIRST} != 1 ]; then
		KTWORD_DATA_PATH="${KTWORD_DATA_PATH},"
	fi
	KTWORD_DATA_PATH="${KTWORD_DATA_PATH}${SRC_KTWORD_PATH}/${LXDAY}/0000/${SRC_SUB_PATH}"
	FIRST=0
done

#execute the job, calculate week average
${HADOOP} jar ${JAR_FILE} wordstat -Dmapred.job.name=cm-rank-yangyong -Dmapred.job.priority=NORMAL "${KTWORD_DATA_PATH}" "${OUTPUT_AVERAGE_PATH}"
alert $? "${0}-job-wordstat-failed"

FILTER_FILE=""
${HADOOP} fs -test -e "${SRC_FILTER_FILE}"
if [ $? == 0 ]; then
	FILTER_FILE=" --filterFile ${SRC_FILTER_FILE}"
fi

#execute the job, recommend by adview
${HADOOP} jar ${JAR_FILE} recommend -Dmapred.job.name=cm-rank-yangyong -Dmapred.job.priority=NORMAL -Dmapred.input.dir=${OUTPUT_AVERAGE_PATH}adview/ -Dmapred.output.dir=${OUTPUT_ADVIEW_RECOMMEND_PATH} --numRecommendations ${NUM_RECOMMEND_ADVIEW} ${FILTER_FILE}
alert $? "${0}-job-recommend-adview-failed"

#download the output of adview recommender from hdfs
${HADOOP} fs -getmerge "${OUTPUT_ADVIEW_RECOMMEND_PATH}output/" ${LOCAL_OUTPUT_PATH}
alert $? "${0}-get-output-adview-failed"
mv "${LOCAL_OUTPUT_PATH}/output" "${LOCAL_OUTPUT_PATH}/adview"

touch "${LOCAL_OUTPUT_PATH}/done"

#success
exit 0

