#/bin/bash

source conf/snap_include.conf

retry_num=8
run_program="snap_deal_ad.sh"
kill_list="supervise_$run_program $run_program"
log_fname="log/$run_program.log.warning"

mobile_list=${MOBILE_LIST}
mail_list=${MAIL_LIST}

# write log and send warning message
if [ "$1" -eq "1" -o "$1" -eq "$retry_num" ]
then
	msg="FATAL: [`date +%Y-%m-%d\ %T`]: ${1}th restart $run_program"
	mkdir -p `dirname $log_fname`
	echo "$msg" >> $log_fname
	echo "" | mail -s "$msg" $mail_list
	for mobile in $mobile_list
	do gsmsend -s emp01.baidu.com:15003 -s emp02.baidu.com:15003 $mobile@"$msg [`hostname`]"
	done

	# kill supervise and program
	if [ "$1" -ge "$retry_num" ]
	then killall -9 $kill_list
	fi
fi
