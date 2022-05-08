#!/bin/bash

rm ./log/*.log

java -cp 'conf/:dbft/*:lib/*:apps/*' org.fisco.bcos.sdk.demo.AISChainDemo
