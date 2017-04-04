#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="./alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=correct_beidoufinan.sh
reader_list=hujunhai

DATA_PATH=/home/work/var/correct_beidoufinan
LOG_FILE=${LOG_PATH}/correct_beidoufinan.log

mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}
alert $? "${msg}"

dates=(20131216 20131217 20131218 20131219 20131220 20131221 20131222 20131223 20131224 20131225)

function main(){
	for dateStr in `echo ${dates[@]}`; do
	
		FTP_PATH="ftp://cq01-testing-zfqa27.cq01.baidu.com:/home/users/hujunhai/hujunhai/data"
		DATA_FILE="dcharge.bd.day.${dateStr}.log"
		
		wget -t 3 -q $FTP_PATH/$DATA_FILE -O $DATA_FILE
		wget -t 3 -q $FTP_PATH/$DATA_FILE.md5 -O $DATA_FILE.md5
		msg="download data failed,file path="$DATA_PATH/$DATA_FILE
		md5sum -c $DATA_FILE.md5
		alert $? "${msg}"
		
		awk -F "\t" '{row=$1;x=2;while(x<=17){row=row"\t"$x ;x++;}print row;}' ${DATA_PATH}/${DATA_FILE} > ${DATA_PATH}/${DATA_FILE}.tmp
		alert $? "awk ${DATA_PATH}/${DATA_FILE}.tmp failed!"
		mv ${DATA_PATH}/${DATA_FILE}.tmp ${DATA_PATH}/${DATA_FILE}
		alert $? "mv ${DATA_PATH}/${DATA_FILE}.tmp ${DATA_PATH}/${DATA_FILE} failed!"
		
		BAK_DATA_FILE="beidoufinan.cost.${dateStr}.bak"
		msg="get ${BAK_DATA_FILE} from beidoufinan failed!"
		echo "SELECT adid,wordid,planid,userid,cntnid,cmatch,provid,bid,price,rrate,rank,ip,balance,clktime,cnttime,srchid,orderline FROM beidoufinan.cost_${dateStr};" >> $LOG_FILE
		runsql_clk_read "SELECT adid,wordid,planid,userid,cntnid,cmatch,provid,bid,price,rrate,rank,ip,balance,clktime,cnttime,srchid,orderline FROM beidoufinan.cost_${dateStr};"  ${BAK_DATA_FILE}
		alert $? "${msg}"
		
		echo "use beidoufinan; load data local infile '${DATA_PATH}/${DATA_FILE}' into table cost_${dateStr}" >> $LOG_FILE
		runsql_clk "use beidoufinan; load data local infile '${DATA_PATH}/${DATA_FILE}' into table cost_${dateStr}"
		alert $? "load ${dateStr} data into beidoufinan failed!"
		
		CORRECT_DATA_FILE="correct.icrm.${dateStr}.data"
		msg="get ${CORRECT_DATA_FILE} from beidoufinan failed!"
		echo "SELECT userid, SUM(price*rrate), SUM(price), COUNT(*) FROM beidoufinan.cost_${dateStr} GROUP BY userid ORDER BY userid" >> $LOG_FILE
		runsql_clk_read "SELECT userid, SUM(price*rrate), SUM(price), COUNT(*) FROM beidoufinan.cost_${dateStr} GROUP BY userid ORDER BY userid"  ${CORRECT_DATA_FILE}
		alert $? "${msg}"
	done
}

main
