#!/bin/bash

JAVA_HOME=$JAVA_HOME_1_6
export JAVA_HOME
PATH=$JAVA_HOME/bin:$PATH

export MAVEN_HOME=$MAVEN_3_0_4
PATH=$MAVEN_HOME:$PATH
export PATH

mvn clean
mvn -Ponline deploy -Dmaven.test.skip=true
mkdir output
cp target/*.tar.gz output
cd output
tar -zxvf beidou-cron.tar.gz
rm beidou-cron.tar.gz

#转换文件格式为unix
for i in `ls ./bin | grep .sh`
do
	dos2unix ./bin/$i
done
for j in `ls ./conf | grep .conf`
do
	dos2unix ./conf/$j
done

tar -zcvf beidou-cron.tar.gz bin conf lib
rm -r bin conf lib