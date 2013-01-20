#!/bin/bash

# script variables
SPLAT_BUILD_LOG=splat_build_log.txt # the log file for cleaning & building SPLAT (only)
SCRIPT_BASE_DIR=`pwd`
STARLINK_INST_DIR_BASE=../bin # installation directory

CMD_ENABLE_LOG='--log'
CMD_DISABLE_ANT_BUILD='--no-ant-build'

CMD_SUBPROJ='--subproject'
CMD_TASK='--task'

ENABLE_LOG=false
ENABLE_ANT_BUILD=true

SUBPROJ=''
TASK='build'

ERROR_IN_CMD=false
SHOW_HELP=true

# starlink variables
export STAR_JAVA=/usr/bin/java # will be reset during runtime

# header
echo
echo --------------------------
echo ---- Starlink Builder ----
echo --------------------------
echo

# parse command line
CMD_PARAMS_COUNTER=0
declare -a cmdparamstmp=("$@")
for var in "${cmdparamstmp[@]}"
do
	case $var in
		# show help?
		"--help" )
			SHOW_HELP=true			
			break
			;;
		# output to log?
		$CMD_ENABLE_LOG )
			ENABLE_LOG=true
			SHOW_HELP=false
			;;
		# disable ant build?
		$CMD_DISABLE_ANT_BUILD )
			ENABLE_ANT_BUILD=false
			SHOW_HELP=false
			;;
		# process a subproject only?
		$CMD_SUBPROJ )
			# TODO treat an unusual usage...			
			SUBPROJ=${cmdparamstmp[$CMD_PARAMS_COUNTER+1]}
			SHOW_HELP=false
			;;
		# pass to Ant some task other than 'build'?
		$CMD_TASK )
			# TODO treat an unusual usage...			
			TASK=${cmdparamstmp[$CMD_PARAMS_COUNTER+1]}
			SHOW_HELP=false
			;;
			
	esac
	CMD_PARAMS_COUNTER=$(($CMD_PARAMS_COUNTER+1))
done

if [ "$1" == "" ]
then
	SHOW_HELP=false
fi

# show help
#if [ "$1" != "$CMD_ENABLE_LOG" -a "$1" != "$CMD_DISABLE_ANT_BUILD" -a -n "$1" ]
if [ $SHOW_HELP == true ]
then
	echo "Usage: $0 [--help] | [ [$CMD_ENABLE_LOG] | [$CMD_SUBPROJ <project_name>] | [$CMD_TASK <task_name>] | [$CMD_DISABLE_ANT_BUILD] ]"
	echo
	echo Parameters:
	echo
	echo "     --help                        Shows this help"
	echo "     $CMD_ENABLE_LOG                         Logs the build output to the file: $SPLAT_BUILD_LOG"
	echo "     $CMD_DISABLE_ANT_BUILD                Disables building of Ant"
	echo "     $CMD_SUBPROJ <project_name>   Apply task only at <project_name> subproject"
	echo "     $CMD_TASK <task_name>            Run Ant with this task"
	echo
	exit 0
fi

echo "Subproject: $SUBPROJ"
echo "Ant task: $TASK"
echo
echo "Log: $ENABLE_LOG"
echo "Build Ant: $ENABLE_ANT_BUILD"
echo
echo $STARLINK_INST_DIR_BASE 

echo
echo   Prerequisities
echo -----------------
echo
echo -- First make sure you have a 1.5.x JDK available. Current running version:
echo 
java -version
echo 
# Setting up STAR_JAVA variable
echo -- Setting up STAR_JAVA variable:
java_binarires=$(echo `whereis -b java` | tr "java:" "\n")

for b in $java_binarires
do
	STAR_JAVA="$b"java
	break
done
echo $STAR_JAVA
echo

# Ant part
echo
echo   Ant part
echo -----------

echo
echo -- Entering directory:
cd ant && pwd
echo
echo -- Setting up PATH to:
export PATH=`pwd`/bin:$PATH
echo $PATH
echo
echo -- Ant now should work:
echo
whereis ant
echo
ant -projecthelp
echo
echo -- Setting up Ant build directory to:
cd ..
export ANT_BUILD=`pwd`/bin
cd ant
echo $ANT_BUILD
echo
if [ $ENABLE_ANT_BUILD == true ]
then
	echo -- Attempting to build Ant
	echo
	ant -Dstar.dir=`echo $ANT_BUILD` clean
	rm -R -f `echo $ANT_BUILD`/*
	ant -Dstar.dir=`echo $ANT_BUILD` install
	ant -Dstar.dir=`echo $ANT_BUILD` clean
fi
echo
echo -- Setting up PATH to:
export PATH=$ANT_BUILD/bin:$PATH

echo $PATH
echo

# Starlink build part
echo
echo   Starlink build part
echo -------------------

echo
echo -- Stepping back to the source directory:
cd ..

# 'cd' to possible subproject
if [ "$SUBPROJ" != "" ]
then
	echo
	echo -- Entering subproject	
	cd $SUBPROJ
	if [ "`pwd`" != $SCRIPT_BASE_DIR/$SUBPROJ ]
	then
		echo		
		echo "ERROR! The subproject '$SUBPROJ' didn't find ..."
		echo
		exit 1
	fi
fi

pwd
echo
echo -- Attempting to build ...
if [ $ENABLE_LOG == true ]
then
	echo Log file: $SPLAT_BUILD_LOG
fi
echo Cleaning ...
if [ $ENABLE_LOG == true ]
then
	ant clean > `echo $SPLAT_BUILD_LOG`
else
	ant clean
fi
echo Building ...
if [ $ENABLE_LOG == true ]
then
	ant -Dfile.encoding=iso-8859-1 build > `echo $SPLAT_BUILD_LOG`
else
	ant -Dfile.encoding=iso-8859-1 build
fi
# Starlink Installation part
# FIX this is awful and buggy...
echo Installing ...
STARLINK_INST_DIR=../$STARLINK_INST_DIR_BASE
if [ "$SUBPROJ" != "" ]
then
	STARLINK_INST_DIR=../$STARLINK_INST_DIR_BASE
fi
echo Instalation directory: $STARLINK_INST_DIR
echo
echo Treating error when installing ttools...:
TTOOLS_ERR1_DEST=`pwd`/ttools/src/$STARLINK_INST_DIR_BASE/etc/xdoc
echo      Creating $TTOOLS_ERR1_DEST
mkdir `pwd`/ttools/src/$STARLINK_INST_DIR_BASE
mkdir `pwd`/ttools/src/$STARLINK_INST_DIR_BASE/etc
mkdir $TTOOLS_ERR1_DEST
TTOOLS_ERR1_SRC=`pwd`/xdoc/src/etc
echo copying from $TTOOLS_ERR1_SRC to $TTOOLS_ERR1_DEST
cp -v $TTOOLS_ERR1_SRC/* $TTOOLS_ERR1_DEST/
sleep 10

echo Treating error when installing TOPCAT...:
TOPCAT_ERR1_DEST=`pwd`/topcat/src/$STARLINK_INST_DIR_BASE/etc/xdoc
echo      Creating $TOPCAT_ERR1_DEST
mkdir `pwd`/topcat/src/$STARLINK_INST_DIR_BASE
mkdir `pwd`/topcat/src/$STARLINK_INST_DIR_BASE/etc
mkdir $TOPCAT_ERR1_DEST
TOPCAT_ERR1_SRC=`pwd`/xdoc/src/etc
echo copying from $TOPCAT_ERR1_SRC to $TOPCAT_ERR1_DEST
cp -v $TOPCAT_ERR1_SRC/* $TOPCAT_ERR1_DEST/
sleep 10

ant -Dstar.dir=$STARLINK_INST_DIR install

echo
echo
echo ----------------------
echo
echo Installation directory: `pwd`/$STARLINK_INST_DIR_BASE
echo
echo
