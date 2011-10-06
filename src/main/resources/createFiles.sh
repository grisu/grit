#!/bin/bash

TIMES=$1
INPUT=$2

for i in $(seq 1 $TIMES)
do
   FILE="file_$i"
   echo "copying file $FILE"
   cp $INPUT $FILE
done
