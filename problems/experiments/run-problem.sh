#!/bin/bash

######################################################################################
### This script runs multple experiments consisting of concrete problem and a 
### list of operators
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

if [ "${EXP_COMPRESS}" == "true" ] ; then
	if [ -z "$EXP_COMP_DIR" ] ; then
		export EXP_COMP_DIR=$EXP_ROOT/compressed
		echo "EXP_COMP_DIR is not set, assuming ${EXP_COMP_DIR}"
		mkdir -p $EXP_COMP_DIR
	fi
fi 

######################################################################################
### Running the problem

if [ $# -lt 3 ] ; then
	echo Usage: ./run-problem.sh PROBLEM EXP_NO OPERATOR1 [OPERATOR2 ...]
	exit
fi

problem=$1		# problem codename
exp_no=$2		# experiment number
shift
shift

problem_param=${EXP_PROBLEMS}/${problem}.params
if [ ! -f ${problem_param} ] ; then
	echo "Problem ${problem_param} dos not exists"
	exit
fi

result_dirs=""
while (( "$#" )); do
	operator=$1
	operator_param=${EXP_OPERATORS}/${operator}.params
	if [ ! -f ${operator_param} ] ; then
		echo "Operator ${operator_param} does not exist"
	else
		echo "Running problem ${problem} for operator ${operator}"
		./run-experiment.sh "${problem}" "${exp_no}" "${operator}" "${EXP_ECJ_PARAMS}"
		result_dirs="${result_dirs} ${problem}.${exp_no}.${operator}"
	fi	
	shift
done

if [ "${EXP_COMPRESS}" == "true" ] ; then
	package=${problem}.${exp_no}_`date +%x_%X`.tar.gz
	tar -C ${EXP_RESULTS} -zcf ${EXP_COMP_DIR}/${package} ${result_dirs}
fi 

exit



