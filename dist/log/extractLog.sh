#!/bin/bash

grep 'PerformanceCollectorContinuousLog:' sdk.log* > PerformanceCollectorContinuous.log

python3 tpsAnalyze.py
