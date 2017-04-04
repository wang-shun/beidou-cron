#!/bin/sh

source ../conf/config.inc.sh
source ../conf/ctkor.inc.sh
source ../lib/alert.sh
source ../conf/alert.inc.sh

mkdir -p "${LOCAL_OUTPUT_PATH}"

#check all input files exist
for LXDAY in ${LXDAYS[*]}
do
	${HADOOP} fs -test -e "${SRC_CTWORD_PATH}/${LXDAY}/"
	alert $? "${0}-check-ctword-${LXDAY}-not-exists"
done

#generate input paths, separated by comma
CTWORD_DATA_PATH=""
FIRST=1
for LXDAY in ${LXDAYS[*]}
do
	if [ ${FIRST} != 1 ]; then
		CTWORD_DATA_PATH="${CTWORD_DATA_PATH},"
	fi
	CTWORD_DATA_PATH="${CTWORD_DATA_PATH}${SRC_CTWORD_PATH}/${LXDAY}/0000/"
	FIRST=0
done

#execute the job, calculate week average
${HADOOP} jar ${JAR_FILE} wordstat -Dmapred.job.name=cm-rank-yangyong01 -Dmapred.job.priority=NORMAL "${CTWORD_DATA_PATH}" "${OUTPUT_AVERAGE_PATH}"
alert $? "${0}-job-wordstat-failed"

#execute the job, recommend by adview
${HADOOP} jar ${JAR_FILE} recommend -Dmapred.job.name=cm-rank-yangyong01 -Dmapred.job.priority=NORMAL -DtransposeUserItem=true -Dmapred.input.dir=${OUTPUT_AVERAGE_PATH}adview/ -Dmapred.output.dir=${OUTPUT_ADVIEW_RECOMMEND_PATH} --numRecommendations ${NUM_RECOMMEND_ADVIEW}
alert $? "${0}-job-recommend-adview-failed"

#execute the job, transpose the recommended data by adview
${HADOOP} jar ${JAR_FILE} transpose -Dmapred.job.name=cm-rank-yangyong01 -Dmapred.job.priority=NORMAL "${OUTPUT_ADVIEW_RECOMMEND_PATH}output/" "${OUTPUT_ADVIEW_TRANSPOSE_PATH}" ${NUM_TRANSPOSE_ADVIEW}
alert $? "${0}-job-transpose-adview-failed"

#execute the job, recommend by ctr
${HADOOP} jar ${JAR_FILE} recommend -Dmapred.job.name=cm-rank-yangyong01 -Dmapred.job.priority=NORMAL -DtransposeUserItem=true -Dmapred.input.dir=${OUTPUT_AVERAGE_PATH}ctr/ -Dmapred.output.dir=${OUTPUT_CTR_RECOMMEND_PATH} --numRecommendations ${NUM_RECOMMEND_CTR}
alert $? "${0}-job-recommend-ctr-failed"

#execute the job, transpose the recommended data by ctr
${HADOOP} jar ${JAR_FILE} transpose -Dmapred.job.name=cm-rank-yangyong01 -Dmapred.job.priority=NORMAL "${OUTPUT_CTR_RECOMMEND_PATH}output/" "${OUTPUT_CTR_TRANSPOSE_PATH}" ${NUM_TRANSPOSE_CTR}
alert $? "${0}-job-transpose-ctr-failed"

#download the output of adview recommender from hdfs
${HADOOP} fs -getmerge "${OUTPUT_ADVIEW_TRANSPOSE_PATH}" ${LOCAL_OUTPUT_PATH}
alert $? "${0}-get-output-adview-failed"

#download the output of ctr recommender from hdfs
${HADOOP} fs -getmerge "${OUTPUT_CTR_TRANSPOSE_PATH}" ${LOCAL_OUTPUT_PATH}
alert $? "${0}-get-output-ctr-failed"

touch "${LOCAL_OUTPUT_PATH}/done"

#success
exit 0

