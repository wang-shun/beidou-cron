#!/bin/bash
#@file: formatSinanData.sh
#@author: zhangxu04
#@date: 2011-05-18
#@version: 1.0.0.0
#@brief: ����˾�����ɵ��ļ����������WM123������ļ�

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

# ͳ���������
TOTAL_PROCESS_TASK_NUM=0
SUCCESS_TASK_NUM=0
FAIL_TASK_NUM=0

# ���Ƕȡ����ֶ�ƥ��������ʽ
REGEX_4_DECIMAL="^[0-9]\.[0-9]*"

#--------------- function  --------------

# ��������
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

# ����˾�ϵ�${ά��}.tid�ļ���������WM123��Ҫ����
# ���ڹؼ���
# $1: ${ά��}.tid
# $2: ${WM123��ʽ������ļ�}.tid
function outputTopRecordKeyword()
{
	max_num=30
	if [ ! -e $1 -o ! -s $1 ];then
		echo_error_msg $1"�ļ�Ϊ�ջ򲻴���"
		return 1
	fi
	
	cat $1 | sed 's/ //g' | awk -F"\t" '{if(match($2,/,/)==0)print $0}' | sort +5nr | head -$max_num > $TEMP_PATH/keyword_interest.tmp
	
	# ��֤��ֵ�Ϸ���
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print $4}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"�ļ��������Ƕ��ֶδ��ڲ��Ϸ�������"
		return 1
	fi
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print $5}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"�ļ�ȫ���������Ƕ��ֶδ��ڲ��Ϸ�������"
		return 1
	fi
	cat $TEMP_PATH/keyword_interest.tmp | head -5 | awk -F"\t" '{print $6}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"�ļ����ֶ��ֶδ��ڲ��Ϸ�������"
		return 1
	fi
	
	# ��֤��Ϸ���
	num=`cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print NF}' | sort -u | wc -l`
	if [ $num -ne 1 ];then
		echo_error_msg $1"�ļ��е��в���8����"
		return 1
	fi
	
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print NF}' | sort -u | head -1 | grep "8" > /dev/null 2>&1
	if [ $? -ne 0 ];then
		echo_error_msg $1"�ļ�����8������ɵ�"
		return 1
	fi
	
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{printf("%s,%.8f,%.8f,%.8f|",$2,$4,$5,$6)}'  > $2
	if [ $? -ne 0 ];then
		echo_error_msg $1"�ļ������ؼ��ʡ���Ȥ��ʧ��"
		return 1
	fi
	
	line_num=`cat $TEMP_PATH/keyword_interest.tmp | wc -l`
	if [ $line_num -lt 5 ];then
		echo_error_msg $1"���ݲ���5��"
		return 1
	fi
	if [ $line_num -lt $max_num ];then
		echo_msg $1"���ݲ���${max_num}��"
		return 0
	fi
	
	return 0
}

# ����˾�ϵ�${ά��}.tid�ļ���������WM123��Ҫ����
# ������Ȥ��
# $1: ${ά��}.tid
# $2: ${WM123��ʽ������ļ�}.tid
function outputTopRecordInterest()
{
	max_num=10
	if [ ! -e $1 -o ! -s $1 ];then
		echo_error_msg $1"�ļ�Ϊ�ջ򲻴���"
		return 1
	fi
	
	cat $1 | sed 's/ //g' | awk -F"\t" '{if(match($2,/,/)==0)print $0}' | sort +5nr | head -$max_num > $TEMP_PATH/keyword_interest.tmp
	
	# ��֤��ֵ�Ϸ���
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print $4}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"�ļ��������Ƕ��ֶδ��ڲ��Ϸ�������"
		return 1
	fi
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print $5}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"�ļ�ȫ���������Ƕ��ֶδ��ڲ��Ϸ�������"
		return 1
	fi
	cat $TEMP_PATH/keyword_interest.tmp | head -5 | awk -F"\t" '{print $6}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"�ļ����ֶ��ֶδ��ڲ��Ϸ�������"
		return 1
	fi
	
	# ��֤��Ϸ���
	num=`cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print NF}' | sort -u | wc -l`
	if [ $num -ne 1 ];then
		echo_error_msg $1"�ļ��е��в���8����"
		return 1
	fi
	
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{print NF}' | sort -u | head -1 | grep "8" > /dev/null 2>&1
	if [ $? -ne 0 ];then
		echo_error_msg $1"�ļ�����8������ɵ�"
		return 1
	fi
	
	cat $TEMP_PATH/keyword_interest.tmp | awk -F"\t" '{printf("%s,%.8f,%.8f,%.8f|",$2,$4,$5,$6)}'  > $2
	if [ $? -ne 0 ];then
		echo_error_msg $1"�ļ������ؼ��ʡ���Ȥ��ʧ��"
		return 1
	fi
	
	line_num=`cat $TEMP_PATH/keyword_interest.tmp | wc -l`
	if [ $line_num -lt 5 ];then
		echo_error_msg $1"���ݲ���5��"
		return 1
	fi
	if [ $line_num -lt $max_num ];then
		echo_msg $1"���ݲ���${max_num}��"
		return 0
	fi
	
	return 0
}

# ����˾�ϵ�${ά��}.tid�ļ���������WM123��Ҫ����
# �������վ�㣬��ѡ�ѰԵļ�¼������Ѱ�Ϊ�գ�����cpro���˵�վ�����
# $1: ${ά��}.tid
# $2: ${WM123��ʽ������ļ�}.tid
# $3: ���˱����Լ���siteurl
function outputTopRecord4site()
{
	max_num=30
	if [ ! -e $1 ];then
		echo_error_msg $1"�ļ�������"
		return 1
	fi
	record_num=`cat $1 | wc -l`
	if [ $? -ne 0 ];then
		echo_error_msg $1"�Ѱ��ļ�ͳ������ʧ��"
		return 1
	fi
	if [ -z $4 ];then
		echo_error_msg "siteurl for filter is null"
		return 1
	fi
	cat $1 | grep -v "$4" | sed 's/ //g' | awk -F"\t" '{if(match($1,/,/)==0)print $0}' | awk -F"\t" '{if($1!=""){print $0}}' > $TEMP_PATH/visit_site_sobar.tmp
	if [ $record_num -lt $max_num ];then
		if [ ! -e $2 -o ! -s $2 ];then
			echo_error_msg $2"�ļ�Ϊ�ջ򲻴���"
			return 1
		fi
		cpro_input_num=`expr $max_num - $record_num`
		#cat $2 | grep -v "$4" | sed 's/ //g' | sort +10nr | awk -F"\t" '{if($3!="" && match($3,/,/)==0) {print $3"\t"$7"\t"$8"\t"$9"\t"$10"\t"$11"\t"$12"\t"$13;}}' | head -$cpro_input_num >> $TEMP_PATH/visit_site_sobar.tmp

		cat $2 | grep -v "$4" | sed 's/ //g' | sort +4nr | awk -F"\t" '{if($1!="" && match($1,/,/)==0) {print $1"\t"$2"\t"$3"\t"$4"\t"$5;}}' | head -$cpro_input_num >> $TEMP_PATH/visit_site_sobar.tmp
		if [ $? -ne 0 ];then
			echo_error_msg $1"�ļ�����cpro���վ��ʧ��"
			return 1
		fi
		echo_msg $1"�ļ�����"${record_num}"����¼����cpro�ļ�����"
	fi
	
	cat $TEMP_PATH/visit_site_sobar.tmp | sed 's/ //g' | sort +4nr | head -$max_num > $TEMP_PATH/visit_site.tmp
	
	# ��֤��ֵ�Ϸ���
	cat $TEMP_PATH/visit_site.tmp | awk -F"\t" '{print $3}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"�ļ��������Ƕ��ֶδ��ڲ��Ϸ�������"
		return 1
	fi
	cat $TEMP_PATH/visit_site.tmp | awk -F"\t" '{print $4}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"�ļ�ȫ���������Ƕ��ֶδ��ڲ��Ϸ�������"
		return 1
	fi
	cat $TEMP_PATH/visit_site.tmp | head -5 | awk -F"\t" '{print $5}'| grep -v "$REGEX_4_DECIMAL" > /dev/null 2>&1
	if [ $? -eq 0 ];then
		echo_error_msg $1"�ļ����ֶ��ֶδ��ڲ��Ϸ�������"
		return 1
	fi
	
	# ��֤��Ϸ���
	num=`cat $TEMP_PATH/visit_site.tmp | awk -F"\t" '{print NF}' | sort -u | wc -l`
	if [ $num -ne 1 ];then
		echo_error_msg $1"�ļ��е��в���5����"
		return 1
	fi
	
	cat $TEMP_PATH/visit_site.tmp | awk -F"\t" '{print NF}' | sort -u | head -1 | grep "5" > /dev/null 2>&1 
	if [ $? -ne 0 ];then
		echo_error_msg $1"�ļ�����5������ɵ�"
		return 1
	fi
	
	cat $TEMP_PATH/visit_site.tmp | awk -F"\t" '{if($1!=""){printf("%s,%s,%.8f,%.8f,%.8f|",$1,$2,$3,$4,$5);}}'  > $3
	if [ $? -ne 0 ];then
		echo_error_msg $1"�ļ��������վ��ʧ��"
		return 1
	fi
	
	line_num=`cat $TEMP_PATH/visit_site.tmp | wc -l`
	if [ $line_num -lt 5 ];then
		echo_error_msg $1"���ݲ���5��"
		return 1
	fi
	if [ $line_num -lt $max_num ];then
		echo_msg $1"���ݲ���${max_num}��"
		return 0
	fi
	
	return 0
}

# ���ɱ���Ҫ���������ε�tid��siteurlӳ���ϵ�ļ�������һ���������Ա�ȡ����
# Ҫͳ�Ƶ�����${TO_BE_PROCESS_TID_URL_FILE} = ��ǰϵͳ���Ѿ���ɵ�����${CURRENT_FINISH_TID_URL_FILE} - �Ѿ��ܹ�������${FINISH_TID_URL_FILE} - ��ʧ�ܵ�����${FAIL_TID_URL_FILE}
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
		echo_error_msg $1"Ϊ��"
		return 1
	fi
	return 0
}

function verifyLineField()
{
 	field_num=`echo $1 | awk '{print NF}'`
	if [ $field_num -ne $2 ];then
		echo_error_msg $1"��ĸ������Ϸ�"
		return 1
	fi
	return 0
}


#--------------- main  --------------

startMills=`date +"%s"`

echo_msg "======================================="
echo_msg "= ��ʼ${TIMESTAMP}����"
echo_msg "======================================="

env_setup

# ���������Ѿ���������tid��siteurlӳ���ϵ�ļ����Ѿ���������statusΪ16
EXPORT_TID_SITEURL_SQL="SELECT a.tid, b.multiUrl FROM sn_task a JOIN sn_url_s b on a.tid=b.tid WHERE a.tstatus=12;"
msg="����˾���Ѿ�������ĳɹ�����ʧ��"
$MYSQL_BIN -h$SINAN_DB_IP -P$SINAN_DB_PORT -u$SINAN_DB_USER -p$SINAN_DB_PASSWORD sinan_beidou --skip-column-name -e "$EXPORT_TID_SITEURL_SQL" > $CURRENT_FINISH_TID_URL_FILE
alert $? ${msg}
echo_msg "����˾���Ѿ�������ĳɹ�����ɹ�"
	
msg="���ɱ��δ�ͳ�������б�ʧ��"
generateToBeProcessTidUrlList
alert $? ${msg}
echo_msg "���ɱ��δ�ͳ�������б�ɹ�"

# ���ݵ�ǰ�Ѿ�����������б�
msg="���ݵ�ǰ�Ѿ�����������б�ʧ��"
if [ -e ${FINISH_TID_URL_FILE} ];then
	cp ${FINISH_TID_URL_FILE} $DATA_PATH/$TIMESTAMP/
fi
alert $? ${msg}
echo_msg "���ݵ�ǰ�Ѿ�����������б�ɹ�"

# ���ݵ�ǰ�Ѿ���ʧ�ܵ������б�
msg="���ݵ�ǰ�Ѿ�����������б�ʧ��"
if [ -e ${FAIL_TID_URL_FILE} ];then
	cp ${FAIL_TID_URL_FILE} $DATA_PATH/$TIMESTAMP/
fi
alert $? ${msg}
echo_msg "���ݵ�ǰ�Ѿ���ʧ�ܵ������б�ɹ�"

msg="����û���κο���������"
if [ ! -s ${TO_BE_PROCESS_TID_URL_FILE} ];then
	alert 1 ${msg}
fi

echo_msg "Merge sub domain data to output new data... pls wait"
msg="����Java�ϲ����������������Ƕȡ�ȫ�����Ƕȡ����ֶ�ʧ�ܣ�˾�϶�ֹͣ�����ʽ������ṩ��WM123�ķÿ������ļ���"
java -Xms24m -Xmx128m -classpath ${CUR_CLASSPATH} com.baidu.beidou.wm123.task.MergeSobarAndCproSubdomainTask ${BASE_PATH}/bin/common.conf ${TO_BE_PROCESS_TID_URL_FILE} 1>>${JAVA_SYSOUT_LOG} 2>>${JAVA_ERROR_LOG}
alert $? ${msg}

# ��յ����ļ�
echo > $DATA4WM123

# ��ȡ���������б��ÿһ�У�������tid��siteurl��ͳ��
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
	echo_msg "��ʼ����tid=${tid}��siteurl=${siteurl}"
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
	# д���Ѿ���ɵ������б�
	echo $LINE >> $FINISH_TID_URL_FILE
	SUCCESS_TASK_NUM=`expr $SUCCESS_TASK_NUM + 1`
	echo_msg "��������tid=${tid}��siteurl=${siteurl}"
done < $TO_BE_PROCESS_TID_URL_FILE

# ȥ������
sed -e '/^$/d' $DATA4WM123 > $TEMP_PATH/visitor.$TIMESTAMP.tmp
cp $TEMP_PATH/visitor.$TIMESTAMP.tmp $DATA4WM123

#make md5
cd $DATA4WM123_DIR && md5sum visitor.${TIMESTAMP} > visitor.${TIMESTAMP}.md5

# ȥ���Ѿ������finish_tid_url.txt��fail_tid_url.txt
cat $FINISH_TID_URL_FILE | sort -u > $TEMP_PATH/"finish_tid_url.${TIMESTAMP}.tmp"
cp $TEMP_PATH/"finish_tid_url.${TIMESTAMP}.tmp" $FINISH_TID_URL_FILE
cat $FAIL_TID_URL_FILE | sort -u > $TEMP_PATH/"fail_tid_url.${TIMESTAMP}.tmp"
cp $TEMP_PATH/"fail_tid_url.${TIMESTAMP}.tmp" $FAIL_TID_URL_FILE

endMills=`date +"%s"`
spendtime=$(($endMills-$startMills))

notify "������${TOTAL_PROCESS_TASK_NUM}�����񣬳ɹ�${SUCCESS_TASK_NUM}������������${FAIL_TASK_NUM}��"
echo_msg "================================================================="
echo_msg "= ������${TOTAL_PROCESS_TASK_NUM}�����񣬳ɹ�${SUCCESS_TASK_NUM}������������${FAIL_TASK_NUM}��"
echo_msg "= ����${TIMESTAMP}���񣻽���ʱ�䣺 `date +"%Y-%m-%d_%H:%M:%S"`, ����ʱ��${spendtime}s"
echo_msg "================================================================="

exit 0

