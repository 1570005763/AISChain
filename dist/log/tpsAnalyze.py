# -*- coding: utf8 -*-
import matplotlib.pyplot as plt

utc_dict = {}

with open("PerformanceCollectorContinuous.log", 'r') as f:
    for line in f.readlines():
        data = line.split(':')
        if len(data) == 4:
            utc = data[3].strip().split(',')[0][:-4]
        elif len(data) == 5:
            utc = data[4].strip().split(',')[0][:-4]
        if utc not in utc_dict.keys():
            utc_dict[utc] = 1
        else:
            utc_dict[utc] += 1

min_key = int(min(utc_dict.keys()))
max_key = int(max(utc_dict.keys()))
print("running",(max_key-min_key+1)*10,"seconds")

for utc in range(min_key,max_key+1):
    if str(utc) not in utc_dict.keys():
        utc_dict[str(utc)] = 0
    else:
        utc_dict[str(utc)] /= 10

keys = sorted(utc_dict.keys())
tps = [utc_dict[key] for key in keys]

plt.plot(tps)
plt.ylabel('txns/s')
plt.show()
