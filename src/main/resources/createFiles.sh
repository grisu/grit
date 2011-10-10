#!/bin/bash

FOLDERS=$1
FILES=$2
INPUT=$3

for i in $(seq 1 $FOLDERS)
do
   FOLDER="folder_$i"
   mkdir -p $FOLDER
   for j in $(seq 1 $FILES)
   do
       FILE="file_$j"
       echo "copying file $FOLDER/$FILE"
       cp $INPUT "$FOLDER/$FILE"
   done
done
