#!/bin/bash

today=`date +%Y-%m-%d.%H:%M:%S` 
titl=Console_Log_${today}.txt

(sudo java -cp ./out:$PWD/res/lib/mysql-connector-java-8.0.19/mysql-connector-java-8.0.19.jar:$PWD/res/lib/JBend.jar com/Main "$@") 2>&1 | tee logs/${titl}
