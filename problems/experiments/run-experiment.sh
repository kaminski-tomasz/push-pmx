#!/bin/bash

######################################################################################
### This script runs single experiment consisting of concrete problem and operator ###
######################################################################################

### REQUIRED ENVIRONMENT VARIABLES:
###	CLASSPATH		- java classpath with required libraries
###	EXP_ROOT		- root directory for experiments
###	EXP_RESULTS		- output directory for results, typicaly
###					EXP_ROOT/results
###	EXP_COMPRESS		- boolean true/false indicating whether resulting 
###					directories should be compressed
###	EXP_COMP_DIR		- output directory for compressed result, typically
###					EXP_ROOT/compressed
###	EXP_PROBLEMS		- directory with problems parameter files, typically
###					EXP_ROOT/problems
###	EXP_OPERATORS		- directory with problems parameter files, typically
###					EXP_ROOT/operators

### OPTIONAL ENVIRONMENT VARIABLES:
###	EXP_ECJ_PARAMS		- parameters that should be passed to the cmd line

######################################################################################
# determining the default values for variables if needed

if [ -z "$CLASSPATH" ] ; then
	echo "CLASSPATH is not set, exiting!"
	exit
fi

if [ -z "$EXP_ROOT" ] ; then
	export EXP_ROOT=`pwd`
	echo "EXP_ROOT is not set, assuming ${EXP_ROOT}"
fi

if [ -z "$EXP_PROBLEMS" ] ; then
	export EXP_PROBLEMS=$EXP_ROOT/problems
	echo "EXP_PROBLEMS is not set, assuming ${EXP_PROBLEMS}"
fi

if [ -z "$EXP_OPERATORS" ] ; then
	export EXP_OPERATORS=$EXP_ROOT/operators
	echo "EXP_OPERATORS is not set, assuming ${EXP_OPERATORS}"
fi

if [ -z "$EXP_RESULTS" ] ; then
	export EXP_RESULTS=$EXP_ROOT/results
	echo "EXP_RESULTS is not set, assuming ${EXP_RESULTS}"
fi
mkdir -p $EXP_RESULTS

######################################################################################

if [ $# -lt 3 ] ; then
	echo Usage: ./experiment.sh PROBLEM OPERATOR EXP_NO [ECJ_PARAMS]
	exit
fi

problem=$1		# problem codename
exp_no=$2		# experiment number
operator=$3		# operator codename
ecj_params=$4		# optional ecj cmd line parameters


if [ -z "$ecj_params" ] ; then
	ecj_params="${EXP_ECJ_PARAMS}"
fi

# experiment name
exp_name=${problem}.${exp_no}.${operator}

# preparing the experiment parameter file

problem_param=${EXP_PROBLEMS}/${problem}.params
if [ ! -f ${problem_param} ] ; then
	echo "Problem ${problem_param} dos not exists"
	exit
fi

operator_param=${EXP_OPERATORS}/${operator}.params
if [ ! -f ${operator_param} ] ; then
	echo "Operator ${operator_param} dos not exists"
	exit
fi

common_param=${EXP_ROOT}/common.params
if [ ! -f $common_param ] ; then
	echo "Common parameter file ${common_param} dos not exist"
	exit
fi

# creating results directory for experiment, remove if exists
rm -rf ${EXP_RESULTS}/${exp_name}
mkdir -p ${EXP_RESULTS}/${exp_name}

param_file=${EXP_RESULTS}/${exp_name}/experiment.params

echo "parent.0=${problem_param}"	> ${param_file}
echo "parent.1=${operator_param}"	>> ${param_file}
echo "parent.2=${common_param}"		>> ${param_file}

######################################################################################
### Executing the experiment

java -classpath ${CLASSPATH} ec.Evolve -file ${param_file} ${ecj_params}\
	-p stat.file=${EXP_RESULTS}/${exp_name}/run.stat\
	-p stat.summary-file=${EXP_RESULTS}/${exp_name}/summary.stat\
	 2> ${EXP_RESULTS}/${exp_name}/experiment.stderr


