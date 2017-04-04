#!/bin/sh

DATA_PATH=/home/work/beidou-cron/data/cdn/
mkdir -p ${DATA_PATH}
cd ${DATA_PATH}

wget "ftp://tc-beidou-web00.tc.baidu.com/home/work/beidou-static/index.html" -O index.html
grep "beidou.baidustatic.com" index.html | sed "s/.*\(beidou.baidustatic.com\)\([^'\"]*\).*/\2/g;" | sed '/.\{2,\}/!d' > resource.data

grep "beidou.baidustatic.com/static/asset/css" index.html | sed "s/.*\(beidou.baidustatic.com\/static\)\([^'\"]*\).*/\2/g;" | sed "s/\(.*\)?v=.*/\1/g" > css.data
while read line
do
	wget "ftp://tc-beidou-web00.tc.baidu.com/home/work/beidou-static/${line}" -O triones.css
	sed "s/url([\"']*/\n/g" triones.css | grep "?v=" | sed 's/\(^.*?v=[0-9]*\).*/\1/g' | sort | uniq > tmp
	awk '{str=$0;token=substr($0,0,2); if(token=="./") str="/asset/css"substr($0,2,100); else {if(token=="..") str=substr($0,3,100); else {token=substr($0,0,1); if(token=="/") str=$0; else{str="/asset/css/"$0}}} print str}' tmp | sed 's/^/\/static/g' | sort | uniq >> resource.data

done < css.data

cd /home/work/beidou-cron/bin/ && sh cdnTool.sh purge -f ${DATA_PATH}/resource.data


