#!/bin/bash

cd src/loader
gcc -c main.c
gcc -o ../../out/loader main.c
cd ../..