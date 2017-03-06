#!/bin/sh
basepath=$(cd `dirname $0`; pwd)
if [ $# -ne 1 ]
then
	echo "输入1个上传文件地址"
else
	filepath="$basepath/$1"
	if [ ! -f $filepath ]; then
   		echo "文件不存在！"
	else
		echo -n "请输入升级或者修复bug描述: "
		read descripe # request host
    	curl  --form "file=@$filepath" --form "des=$descripe"  http://fasttest.dingliqc.com:3000/upload/api
    fi
fi