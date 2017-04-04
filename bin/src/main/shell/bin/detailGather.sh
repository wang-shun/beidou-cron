#!/bin/bash

#@file:detailGather.sh
#@author:yangyun
#@date:2010-01-14
#@version:1.0.0.0
#@params n m :stat from n days ago to m days ago, make sure n >=m
#@example detailGather.sh 3 1 means gather detail data between 3 days ago and 1 days ago,if today is 20100114 ,then gather 11-13's detail data

source "../conf/stat.conf"

for (( i=$1; i>=$2; i-- ))
do
  TIME_DAY=`date -d "${i} day ago" +%Y%m%d`
  sh +x statDay.sh ${TIME_DAY}
  if [ "$?" -ne "0" ]; then
        echo "gather detail${TIME_DAY} failed"
        exit 1
  fi
done 
