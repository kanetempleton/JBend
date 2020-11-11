#!/bin/bash

javac -d out $(find src/com/* | grep .java)

# https://stackoverflow.com/questions/5194926/compiling-java-files-in-all-subfolders
