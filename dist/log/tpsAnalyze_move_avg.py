# -*- coding: utf8 -*-
import matplotlib.pyplot as plt
import sys
import numpy as np


def np_move_avg(scalars,n,mode="same"):
    # n is the size of sliding window
    return np.convolve(scalars, np.ones((n,))/n, mode=mode)


def moving_average(scalars,weight=0.85):
    """
    滑动平均算法
    :param scalars:  单层列表
    :param weight:  滑动平均权重（last的权重）
    :return:
    """
    last = scalars[0]
    smoothed = [scalars[0]]
    for point in scalars:
        smoothed_value = last * weight + (1 - weight) * point
        smoothed.append(smoothed_value)
        last = smoothed_value # 用smoothed[-1]参与下一步的加权和计算
    return smoothed


def mean_smooth(scalars,weight=0.85):
    """
    均值滤波
    :param scalars:  单层列表
    :param weight:  加权和权重
    :return:
    """
    last = scalars[0]
    smoothed = [scalars[0]]
    for index,point in enumerate(scalars,1):
        smoothed_value = last * weight + (1 - weight) * point
        smoothed.append(smoothed_value)
        last = point  #用scalars的元素参与下一个环节的加权和计算
    return smoothed


utc_dict = {}

with open(sys.argv[1], 'r') as f:
    for line in f.readlines():
        data = line.split(':')
        if len(data) == 4:
            utc = data[3].strip().split(',')[0][:-3]
        elif len(data) == 5:
            utc = data[4].strip().split(',')[0][:-3]
        if utc not in utc_dict.keys():
            utc_dict[utc] = 1
        else:
            utc_dict[utc] += 1

min_key = int(min(utc_dict.keys()))
max_key = int(max(utc_dict.keys()))
print("running",(max_key-min_key+1),"seconds")

for utc in range(min_key,max_key+1):
    if str(utc) not in utc_dict.keys():
        utc_dict[str(utc)] = 0
    #else:
        #utc_dict[str(utc)] /= 10

keys = sorted(utc_dict.keys())
tps = [utc_dict[key] for key in keys]

#plt.plot(tps)
plt.plot(np_move_avg(tps,20))
plt.ylabel('txns/s')
plt.show()
