#!/bin/bash
#@file: formatSinanData.sh
#@author: zhangxu04
#@date: 2011-05-18
#@version: 1.0.0.0
#@brief: 根据司南生成的文件，输出符合WM123需求的文件

#--------------- var  --------------
CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=classpath.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=formatSinanData.sh
reader_list=zhangxu

TIMESTAMP=`date +%Y%m%d`

TEMP_PATH=$BASE_PATH/"temp"

DATA_PATH=$BASE_PATH/"data"

LOG_PATH=$BASE_PATH/"log"

SYSOUT_LOG=$LOG_PATH/"formateSinanData.log"

ERROR_LOG=$LOG_PATH/"formateSinanData.log.wf"

JAVA_SYSOUT_LOG=$LOG_PATH/"java_sysout.log"

JAVA_ERROR_LOG=$LOG_PATH/"java_sysout.log.wf"

FINISH_TID_URL_FILE=$DATA_PATH/"finish_tid_url.txt"

FAIL_TID_URL_FILE=$DATA_PATH/"fail_tid_url.txt"

CURRENT_FINISH_TID_URL_FILE=$DATA_PATH/$TIMESTAMP/"current_finish_tid_url.txt"

TO_BE_PROCESS_TID_URL_FILE=$DATA_PATH/$TIMESTAMP/"to_be_process_tid_url.txt"

DATA4WM123_DIR=$DATA_PATH/$TIMESTAMP
DATA4WM123=$DATA_PATH/$TIMESTAMP/visitor.$TIMESTAMP

# 统计任务计数
TOTAL_PROCESS_TASK_NUM=0
SUCCESS_TASK_NUM=0
FAIL_TASK_NUM=0

# 覆盖度、区分度匹配正则表达式
REGEX_4_DECIMAL="^[0-9]\.[0-9]*"

#--------------- function  --------------

# 环境清理
function env_setup()
{
	cd $TEMP_PATH && rm -rf $TEMP_PATH
	
	mkdir -p $TEMP_PATH
	mkdir -p $LOG_PATH
	mkdir -p $DATA_PATH
	mkdir -p $DATA_PATH/$TIMESTAMP/
	
	cd $BASE_PATH
}

echo_msg(){
   echo "[INFO] "$TIMESTAMP" "$1 | tee -a $SYSOUT_LOG
}

echo_error_msg(){
	echo "[ERROR] "$TIMESTAMP" "$1 >> $SYSOUT_LOG
    echo "[ERROR] "$TIMESTAMP" "$1 | tee -a $ERROR_LOG
}

# 根据司南的${维度}.tid文件导出其中WM123需要的域
# 用于关键词
# $1: ${维度}.tid
# $2: ${WM123格式化后的文件}.tid
function outputTopRecordKeyword()
{
	max_num=30
	if [ ! -e $1 -o ! -s $1 ];then
		echo_error_msg $1"文件为空或不存在"
		return 1
	fi
	
	cat $1 | sed 's/ //g' | awk -F"\t" '{if(match($2,/,/)==0)print $0}' | sort +5nr | head -$max_num > $TEMP_PATH/keyword_interest.tmp
	
	# 验证数值合法性
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print $4}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"文件样本覆盖度字段存在不合法的数字"
		return 1
	fi
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print $5}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"文件全网样本覆盖度字段存在不合法的数字"
		return 1
	fi
	cat $TEMP_PATH/keyword_interest.tmp | head -5 | awk -F"\t" '{print $6}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"文件区分度字段存在不合法的数字"
		return 1
	fi
	
	# 验证域合法性
	num=`cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print NF}' | sort -u | wc -l`
	if [ $num -ne 1 ];then
		echo_error_msg $1"文件有的行不是8个域"
		return 1
	fi
	
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print NF}' | sort -u | head -1 | grep "8" > /dev/null 2>&1
	if [ $? -ne 0 ];then
		echo_error_msg $1"文件不是8个域组成的"
		return 1
	fi
	
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{printf("%s,%.8f,%.8f,%.8f|",$2,$4,$5,$6)}'  > $2
	if [ $? -ne 0 ];then
		echo_error_msg $1"文件导出关键词、兴趣点失败"
		return 1
	fi
	
	line_num=`cat $TEMP_PATH/keyword_interest.tmp | wc -l`
	if [ $line_num -lt 5 ];then
		echo_error_msg $1"数据不足5条"
		return 1
	fi
	if [ $line_num -lt $max_num ];then
		echo_msg $1"数据不足${max_num}条"
		return 0
	fi
	
	return 0
}

# 根据司南的${维度}.tid文件导出其中WM123需要的域
# 用于兴趣点
# $1: ${维度}.tid
# $2: ${WM123格式化后的文件}.tid
function outputTopRecordInterest()
{
	max_num=10
	if [ ! -e $1 -o ! -s $1 ];then
		echo_error_msg $1"文件为空或不存在"
		return 1
	fi
	
	cat $1 | sed 's/ //g' | awk -F"\t" '{if(match($2,/,/)==0)print $0}' | sort +5nr | head -$max_num > $TEMP_PATH/keyword_interest.tmp
	
	# 验证数值合法性
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print $4}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"文件样本覆盖度字段存在不合法的数字"
		return 1
	fi
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print $5}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"文件全网样本覆盖度字段存在不合法的数字"
		return 1
	fi
	cat $TEMP_PATH/keyword_interest.tmp | head -5 | awk -F"\t" '{print $6}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"文件区分度字段存在不合法的数字"
		return 1
	fi
	
	# 验证域合法性
	num=`cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print NF}' | sort -u | wc -l`
	if [ $num -ne 1 ];then
		echo_error_msg $1"文件有的行不是8个域"
		return 1
	fi
	
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print NF}' | sort -u | head -1 | grep "8" > /dev/null 2>&1
	if [ $? -ne 0 ];then
		echo_error_msg $1"文件不是8个域组成的"
		return 1
	fi
	
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{printf("%s,%.8f,%.8f,%.8f|",$2,$4,$5,$6)}'  > $2
	if [ $? -ne 0 ];then
		echo_error_msg $1"文件导出关键词、兴趣点失败"
		return 1
	fi
	
	line_num=`cat $TEMP_PATH/keyword_interest.tmp | wc -l`
	if [ $line_num -lt 5 ];then
		echo_error_msg $1"数据不足5条"
		return 1
	fi
	if [ $line_num -lt $max_num ];then
		echo_msg $1"数据不足${max_num}条"
		return 0
	fi
	
	return 0
}

# 根据司南的${维度}.tid文件导出其中WM123需要的域
# 用于相关站点，首选搜霸的记录，如果搜霸为空，则用cpro网盟的站点填充
# $1: ${维度}.tid
# $2: ${WM123格式化后的文件}.tid
# $3: 过滤本身自己的siteurl
function outputTopRecord4site()
{
	max_num=30
	if [ ! -e $1 ];then
		echo_error_msg $1"文件不存在"
		return 1
	fi
	record_num=`cat $1 | wc -l`
	if [ $? -ne 0 ];then
		echo_error_msg $1"搜霸文件统计行数失败"
		return 1
	fi
	if [ -z $4 ];then
		echo_error_msg "siteurl for filter is null"
		return 1
	fi
	cat $1 | grep -v "$4" | sed 's/ //g' | awk -F"\t" '{if(match($1,/,/)==0)print $0}' | awk -F"\t" '{if($1!=""){print $0}}' > $TEMP_PATH/visit_site_sobar.tmp
	if [ $record_num -lt $max_num ];then
		if [ ! -e $2 -o ! -s $2 ];then
			echo_error_msg $2"文件为空或不存在"
			return 1
		fi
		cpro_input_num=`expr $max_num - $record_num`
		#cat $2 | grep -v "$4" | sed 's/ //g' | sort +10nr | awk -F"\t" '{if($3!="" && match($3,/,/)==0) {print $3"\t"$7"\t"$8"\t"$9"\t"$10"\t"$11"\t"$12"\t"$13;}}' | head -$cpro_input_num >> $TEMP_PATH/visit_site_sobar.tmp

		cat $2 | grep -v "$4" | sed 's/ //g' | sort +4nr | awk -F"\t" '{if($1!="" && match($1,/,/)==0) {print $1"\t"$2"\t"$3"\t"$4"\t"$5;}}' | head -$cpro_input_num >> $TEMP_PATH/visit_site_sobar.tmp
		if [ $? -ne 0 ];then
			echo_error_msg $1"文件补余cpro相关站点失败"
			return 1
		fi
		echo_msg $1"文件存在"${record_num}"个记录，以cpro文件补足"
	fi
	
	cat $TEMP_PATH/visit_site_sobar.tmp | sed 's/ //g' | sort +4nr | head -$max_num > $TEMP_PATH/visit_site.tmp
	
	# 验证数值合法性
	cat $TEMP_PATH/visit_site.tmp | awk -F"\t" '{print $3}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"文件样本覆盖度字段存在不合法的数字"
		return 1
	fi
	cat $TEMP_PATH/visit_site.tmp | awk -F"\t" '{print $4}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"文件全网样本覆盖度字段存在不合法的数字"
		return 1
	fi
	cat $TEMP_PATH/visit_site.tmp | head -5 | awk -F"\t" '{print $5}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"文件区分度字段存在不合法的数字"
		return 1
	fi
	
	# 验证域合法性
	num=`cat $TEMP_PATH/visit_site.tmp | awk -F"\t" '{print NF}' | sort -u | wc -l`
	if [ $num -ne 1 ];then
		echo_error_msg $1"文件有的行不是5个域"
		return 1
	fi
	
	cat $TEMP_PATH/visit_site.tmp | awk -F"\t" '{print NF}' | sort -u | head -1 | grep "5" > /dev/null 2>&1 
	if [ $? -ne 0 ];then
		echo_error_msg $1"文件不是5个域组成的"
		return 1
	fi
	
	cat $TEMP_PATH/visit_site.tmp | awk -F"\t" '{if($1!=""){printf("%s,%s,%.8f,%.8f,%.8f|",$1,$2,$3,$4,$5);}}'  > $3
	if [ $? -ne 0 ];then
		echo_error_msg $1"文件导出相关站点失败"
		return 1
	fi
	
	line_num=`cat $TEMP_PATH/visit_site.tmp | wc -l`
	if [ $line_num -lt 5 ];then
		echo_error_msg $1"数据不足5条"
		return 1
	fi
	if [ $line_num -lt $max_num ];then
		echo_msg $1"数据不足${max_num}条"
		return 0
	fi
	
	return 0
}

# 生成本次要导出给下游的tid和siteurl映射关系文件，与上一次任务做对比取补集
# 要统计的任务${TO_BE_PROCESS_TID_URL_FILE} = 当前系统里已经完成的任务${CURRENT_FINISH_TID_URL_FILE} - 已经跑过的任务${FINISH_TID_URL_FILE} - 跑失败的任务${FAIL_TID_URL_FILE}
# ${TO_BE_PROCESS_TID_URL_FILE} = ${CURRENT_FINISH_TID_URL_FILE} - ${FINISH_TID_URL_FILE} - ${FAIL_TID_URL_FILE}
function generateToBeProcessTidUrlList()
{
	if [ -e $FINISH_TID_URL_FILE ];then
		cp $FINISH_TID_URL_FILE $TEMP_PATH/"all_finish_tid_url.tmp"
		if [ -e $FAIL_TID_URL_FILE ];then
			cat $FAIL_TID_URL_FILE >> $TEMP_PATH/"all_finish_tid_url.tmp"
		fi
	else
		if [ -e $FAIL_TID_URL_FILE ];then
			cat $FAIL_TID_URL_FILE > $TEMP_PATH/"all_finish_tid_url.tmp"
		fi
	fi

	if [ ! -e $TEMP_PATH/"all_finish_tid_url.tmp" -o ! -s $TEMP_PATH/"all_finish_tid_url.tmp" ];then
		cp $CURRENT_FINISH_TID_URL_FILE $TO_BE_PROCESS_TID_URL_FILE
	else
		awk 'BEGIN {
			OFS="\t";
		}
		{ 
			if(NR==FNR)
			{
				m[$1];
			} 
			else if(NR>FNR)
			{
				if(!($1 in m)) 
					print $0
			}
		} ' $TEMP_PATH/"all_finish_tid_url.tmp" $CURRENT_FINISH_TID_URL_FILE > $TO_BE_PROCESS_TID_URL_FILE
	fi
}

function verifyFieldNotNull()
{
	if [ "$1" = "" -o -z $1 ];then
		echo_error_msg $1"为空"
		return 1
	fi
	return 0
}

function verifyLineField()
{
 	field_num=`echo $1 | awk '{print NF}'`
	if [ $field_num -ne $2 ];then
		echo_error_msg $1"域的个数不合法"
		return 1
	fi
	return 0
}


#--------------- main  --------------

startMills=`date +"%s"`

echo_msg "======================================="
echo_msg "= 开始${TIMESTAMP}任务"
echo_msg "======================================="

env_setup

# 导出所有已经完成任务的tid和siteurl映射关系文件，已经完成任务的status为16
EXPORT_TID_SITEURL_SQL="SELECT a.tid, b.multiUrl FROM sn_task a JOIN sn_url_s b on a.tid=b.tid WHERE a.tstatus=12;"
msg="导出司南已经运行完的成功任务失败"
$MYSQL_BIN -h$SINAN_DB_IP -P$SINAN_DB_PORT -u$SINAN_DB_USER -p$SINAN_DB_PASSWORD sinan_beidou --skip-column-name -e "$EXPORT_TID_SITEURL_SQL" > $CURRENT_FINISH_TID_URL_FILE
alert $? ${msg}
echo_msg "导出司南已经运行完的成功任务成功"
	
msg="生成本次待统计任务列表失败"
generateToBeProcessTidUrlList
alert $? ${msg}
echo_msg "生成本次待统计任务列表成功"

# 备份当前已经跑完的任务列表
msg="备份当前已经跑完的任务列表失败"
if [ -e ${FINISH_TID_URL_FILE} ];then
	cp ${FINISH_TID_URL_FILE} $DATA_PATH/$TIMESTAMP/
fi
alert $? ${msg}
echo_msg "备份当前已经跑完的任务列表成功"

# 备份当前已经跑失败的任务列表
msg="备份当前已经跑完的任务列表失败"
if [ -e ${FAIL_TID_URL_FILE} ];then
	cp ${FAIL_TID_URL_FILE} $DATA_PATH/$TIMESTAMP/
fi
alert $? ${msg}
echo_msg "备份当前已经跑失败的任务列表成功"

msg="本次没有任何可运行任务"
if [ ! -s ${TO_BE_PROCESS_TID_URL_FILE} ];then
	alert 1 ${msg}
fi

echo_msg "Merge sub domain data to output new data... pls wait"
msg="利用Java合并二级域名样本覆盖度、全网覆盖度、区分度失败，司南端停止输出格式化后的提供给WM123的访客特征文件。"
java -Xms24m -Xmx128m -classpath ${CUR_CLASSPATH} com.baidu.beidou.wm123.task.MergeSobarAndCproSubdomainTask ${BASE_PATH}/bin/common.conf ${TO_BE_PROCESS_TID_URL_FILE} 1>>${JAVA_SYSOUT_LOG} 2>>${JAVA_ERROR_LOG}
alert $? ${msg}

# 清空导出文件
echo > $DATA4WM123

# 读取待跑任务列表的每一行，解析出tid和siteurl，统计
while read LINE
do
	TOTAL_PROCESS_TASK_NUM=`expr $TOTAL_PROCESS_TASK_NUM + 1`
	verifyLineField "$LINE" 2
	if [ $? -ne 0 ];then
		echo $LINE >> $FAIL_TID_URL_FILE
		FAIL_TASK_NUM=`expr $FAIL_TASK_NUM + 1`
		continue
	fi
	tid=`echo $LINE | awk  '{print $1}'`
	verifyFieldNotNull "$tid"
	if [ $? -ne 0 ];then
		echo $LINE >> $FAIL_TID_URL_FILE
		FAIL_TASK_NUM=`expr $FAIL_TASK_NUM + 1`
		continue
	fi
	mkdir -p $TEMP_PATH/$tid
	siteurl=`echo $LINE | awk  '{print $2}'`
	verifyFieldNotNull "$siteurl"
	if [ $? -ne 0 ];then
		echo $LINE >> $FAIL_TID_URL_FILE
		FAIL_TASK_NUM=`expr $FAIL_TASK_NUM + 1`
		continue
	fi
	echo_msg "开始处理tid=${tid}，siteurl=${siteurl}"
    outputTopRecordInterest $SINAN_TASK_DATA_PATH/$tid/$SN_INTEREST_PREFIX$tid $TEMP_PATH/$tid/$SN_INTEREST_PREFIX$tid 
	if [ $? -ne 0 ];then
		echo $LINE >> $FAIL_TID_URL_FILE
		FAIL_TASK_NUM=`expr $FAIL_TASK_NUM + 1`
		continue
	fi
	outputTopRecordKeyword $SINAN_TASK_DATA_PATH/$tid/$SN_BIGSEARCHQUERY_PREFIX$tid $TEMP_PATH/$tid/$SN_BIGSEARCHQUERY_PREFIX$tid
	if [ $? -ne 0 ];then
		echo $LINE >> $FAIL_TID_URL_FILE
		FAIL_TASK_NUM=`expr $FAIL_TASK_NUM + 1`
		continue
	fi
	tempsiteurl=`echo ${siteurl##*www.}`
	outputTopRecord4site $SINAN_TASK_DATA_PATH/$tid/$SN_SOBAR_PREFIX$tid".new" $SINAN_TASK_DATA_PATH/$tid/$SN_CPRO_PREFIX$tid".new" $TEMP_PATH/$tid/$SN_SOBAR_PREFIX$tid ${tempsiteurl} 
	if [ $? -ne 0 ];then
		echo $LINE >> $FAIL_TID_URL_FILE
		FAIL_TASK_NUM=`expr $FAIL_TASK_NUM + 1`
		continue
	fi
	interest_record=`cat $TEMP_PATH/$tid/$SN_INTEREST_PREFIX$tid `
	sobar_record=`cat $TEMP_PATH/$tid/$SN_SOBAR_PREFIX$tid `
	bigsearchquery_record=`cat $TEMP_PATH/$tid/$SN_BIGSEARCHQUERY_PREFIX$tid `
	echo -e "$tid\t$siteurl\t$sobar_record\t$bigsearchquery_record\t$interest_record" >> $DATA4WM123
	# 写入已经完成的任务列表
	echo $LINE >> $FINISH_TID_URL_FILE
	SUCCESS_TASK_NUM=`expr $SUCCESS_TASK_NUM + 1`
	echo_msg "结束处理tid=${tid}，siteurl=${siteurl}"
done < $TO_BE_PROCESS_TID_URL_FILE

# 去除空行
sed -e '/^$/d' $DATA4WM123 > $TEMP_PATH/visitor.$TIMESTAMP.tmp
cp $TEMP_PATH/visitor.$TIMESTAMP.tmp $DATA4WM123

#make md5
cd $DATA4WM123_DIR && md5sum visitor.${TIMESTAMP} > visitor.${TIMESTAMP}.md5

# 去重已经跑完的finish_tid_url.txt和fail_tid_url.txt
cat $FINISH_TID_URL_FILE | sort -u > $TEMP_PATH/"finish_tid_url.${TIMESTAMP}.tmp"
cp $TEMP_PATH/"finish_tid_url.${TIMESTAMP}.tmp" $FINISH_TID_URL_FILE
cat $FAIL_TID_URL_FILE | sort -u > $TEMP_PATH/"fail_tid_url.${TIMESTAMP}.tmp"
cp $TEMP_PATH/"fail_tid_url.${TIMESTAMP}.tmp" $FAIL_TID_URL_FILE

endMills=`date +"%s"`
spendtime=$(($endMills-$startMills))

notify "处理了${TOTAL_PROCESS_TASK_NUM}个任务，成功${SUCCESS_TASK_NUM}个，发生错误${FAIL_TASK_NUM}个"
echo_msg "================================================================="
echo_msg "= 处理了${TOTAL_PROCESS_TASK_NUM}个任务，成功${SUCCESS_TASK_NUM}个，发生错误${FAIL_TASK_NUM}个"
echo_msg "= 结束${TIMESTAMP}任务；结束时间： `date +"%Y-%m-%d_%H:%M:%S"`, 共用时：${spendtime}s"
echo_msg "================================================================="

exit 0

