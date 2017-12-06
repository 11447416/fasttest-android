#!/bin/bash
while getopts "lspodrhu:n:w:b:" opt; do
  case $opt in
    u)
      u=$OPTARG
      ;;
    n)
      n=$OPTARG
       ;;
    l)
      l=true
      ;;
    s)
      env="Staging"
      ;;
    p)
      env="Product"
      ;;
    o)
      env="IP131"
      ;;
    d)
      type="Debug"
      ;;
    r)
      type="Release"
      ;;
    w)
       user=$OPTARG
       ;;
    b)
       branch=$OPTARG
       ;;
    h)
       echo "本打包脚本的使用方法如下："
       echo "usage: 直接上传包     sh package.sh -u filePath [-n note|-w author|-b branch]"
       echo "       本地机打包     sh package.sh -l <-s|-p|-o> <-d|-r> [-n note|-w author|-b branch]"
       echo "       服务器打包     sh package.sh <-s|-p|-o> <-d|-r> [-n note|-w author|-b branch]"
       echo "默认是服务器打包模式"
       echo "参数说明："
       echo "-u       -> upload     直接上传文件模式"
       echo "-l       -> local      本地打包模式"
       echo "-s/-p/-o -> 不同的环境   staging/product/office(ip131)"
       echo "-d/-r    -> 不同的类型   debug/release"
       echo "-n       -> node       说明描述"
       echo "-w       -> who        打包的人"
       echo "-b       -> branch     分支"
       ;;
    ?)
      echo "参数错误！"
      echo "use -h for help"
      exit 1
      ;;
  esac
done

#读取描述信息
if [ ! $branch ];then
  branch=`git symbolic-ref --short -q HEAD`
fi
log=`git log -1 --pretty=%h`
if [ ! $user ];then
  user=`git config user.name`
fi

if [ $u ]; then
  echo "直接上传apk模式"
  if [ ! -f $u ]; then
    echo "文件不存在：$u"
    exit 1
  fi
  echo "当前分支："$branch",当前作者："$user",当前commit："$log
  #上传
  if [ ! $n ]; then
      echo "请输入升级或者修复bug描述: "
      read descripe
  else
      descripe=$n
  fi
  if [ ! $descripe ];then
    echo "你必须输入打包描述"
    exit 1
  fi
  curl  --form "file=@$u" --form "des=$branch($log)#$user:$descripe"  http://fasttest.dingliqc.com:3000/upload/api
elif [ $l ]; then
  echo "使用本地打包模式,本地打包会直接打包当前工作空间的代码"
  echo "当前分支："$branch",当前作者："$user",当前commit："$log
  if [ ! $env ]; then
    echo "打包环境参数错误，请使用（-s、-p or -o)"
    exit 1
  elif [ ! $type ]; then
    echo "打包类型错误，请使用（-d or -r）"
    exit 1
  fi
  if [ ! $n ]; then
      echo "请输入升级或者修复bug描述: "
      read descripe
  else
      descripe=$n
  fi
  if [ ! $descripe ];then
    echo "你必须输入打包描述"
    exit 1
  fi
  build="assemble$env$type"
  echo "开始本地打包$build"
  echo "sh gradlew clean"
  sh gradlew clean
  echo "sh gradlew $build"
  sh gradlew $build
  echo "本地打包完成，开始上传"
  cp app/build/outputs/apk/*.apk app/build/outputs/apk/test.apk 
  #上传
  curl  --form "file=@app/build/outputs/apk/test.apk" --form "des=$branch($log)#$user#$build:$descripe"  http://fasttest.dingliqc.com:3000/upload/api
  rm app/build/outputs/apk/test.apk
  echo "本地打包任务完成"
else
  echo "服务器打包模式"
  if [ ! $env ]; then
    echo "打包环境参数错误，请使用（-s、-p or -o)"
    exit 1
  elif [ ! $type ]; then
    echo "打包类型错误，请使用（-d or -r）"
    exit 1
  fi
  echo "请输入需要打包的分支: （默认：$branch )"
  read newbranch
  if [ $newbranch ];then 
    branch=$newbranch
  fi
  echo "请输入需要打包的commit: （不输入，直接打包分支$branch 的最新代码，输入后，commit优先级更高 )"
  read newlog
  if [ $newlog ];then 
    log=$newlog
  else
    log=""
   fi
  if [ ! $n ]; then
      echo "请输入升级或者修复bug描述: "
      read descripe
  else
      descripe=$n
  fi
  if [ ! $descripe ];then
    echo "你必须输入打包描述"
    exit 1
  fi
  echo "提交打包任务:$descripe"
  curl -G --data-urlencode "branch=$branch" --data-urlencode "commit=$log" --data-urlencode  "des=$descripe" --data-urlencode "type=assemble$env$type" --data-urlencode "user=$user" http://fasttest.dingliqc.com:3000/build/
fi