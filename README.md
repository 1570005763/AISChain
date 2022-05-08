# Introduction

AISChain is a secure and fast
blockchain-based AIS data platform. AISChain adopts to participate in the consensus protocol,
and is compatible with current commodity AIS hardware. Since
the whole system is co-maintained by multiple authorized parties,
AISChain can integrate AIS data resources in a secure way.
For avoiding repeated recording of AIS data on the chain, we
design the Dynamic Bloom Filter Tree (DBFT) to realize efficient
duplication detection in the transaction verification phase.
Here we implement a prototype of AISChain,
and conduct extensive experiments to evaluate the performance
of AISChain. Evaluation results show that the search time of
DBFT is negligible (4.3 ms) with an extreme low error ratio
(0.4%). Meanwhile, AISChain can achieve more than 730 tx/s
throughput even when nodes scale to 36. To the best of our
knowledge, AISChain is the first work to apply the blockchain
technology to secure the AIS data platform.

The benchmark:

* Provide a prototype of AISChain and a quick start demo.
* Provide complete realization of Dynamic Bloom Filter Tree (DBFT).
* Provide contract compilation function, convert Solidity contract files into Java contract files.
* Provide transfer pressure test Demo.
* Provide AMOP test Demo.
* Provide CRUD contract stress test Demo.

## Dependence

* Linux OS
* JAVA SDK 14

## Manual

**Compile source code**

```bash
# clone the source code
$ git clone https://github.com/1570005763/AISChain.git
$ cd AISChain

# (optional) format java file if code changes
$ ./gradlew googleJavaFormat

# compile the source code
$ ./gradlew build
```

**AISChain Demo**

```bash
# This is a quick start of AISChain demo, where you can run entire AISChain and see outcome in a few steps.

# build AISChain (this command will stop and delete current chain if one is running).
$ cd fisco
$ bash AISChain.sh

# run AISChain demo.
$ cd ../dist
$ bash run_AISChain.sh
```


**Configure Java SDK Demo**

Before using the Java SDK Demo, you must first configure the Java SDK, including certificate copy and port configuration. For details, please refer to [here](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/sdk/java_sdk/quick_start.html#sdk).

```
# bash
# Copy the certificate (suppose the SDK certificate is located in ~/fisco/nodes/127.0.0.1/sdk directory)
$ cp -r ~/fisco/nodes/127.0.0.1/sdk/* conf

# Copy configuration file
# Note:
#   The channel port of the FISCO BCOS blockchain system built by default is 20200. 
#   If you modify this port, please modify the [network.peers] configuration option in config.toml
$ cp conf/config-example.toml conf/config.toml
```


**Execute stress test Demo**

Java SDK Demo provides a series of stress testing programs, including serial transfer contract stress testing, parallel transfer contract stress testing, AMOP stress testing, etc. The specific usage examples are as follows:

```
# bash
# Enter the dist directory
$ cd dist

# Copy the sol file that needs to be converted to java code to the dist/contracts/solidity path
# convert sol, where ${packageName} is the generated java code package path
# The generated java code is located in the /dist/contracts/sdk/java directory
$ java -cp "apps/*:lib/*:conf/" org.fisco.bcos.sdk.demo.codegen.DemoSolcToJava ${packageName}

# Pressure test PerformanceOk contract:
# count: total transaction count
# tps: qps
# groupId: the group ID
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceOk [count] [tps] [groupId]

# Pressure test parallel transfer contract
# --------------------------
# Add accounts based on the Solidity parallel contract parallelok:
# groupID: the group ID
# count: total transaction count
# tps: qps
# file: the file of the generated account saved in
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [parallelok] [groupID] [add] [count] [tps] [file]
# Add accounts based on Precompiled parallel contract precompiled
# (Parameter meaning is the same as above)
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [precompiled] [groupID] [add] [count] [tps] [file]
# --------------------------
# Based on the Solidity parallel contract parallelok to initiate a transfer transaction stress test
# groupID: Group ID of pressure test
# count: total amount of transactions
# tps: qps
# file: User file for transfer
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [parallelok] [groupID] [transfer] [count] [tps] [file]
# 基于Precompiled并行合约Precompiled发起转账压测
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParallelOkPerf [precompiled] [groupID] [transfer] [count] [tps] [file]


# CRUD contract stress test:
# CRUD insert
# count: total amount of transactions
# tps: qps
# groupId: the groupId
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [insert] [count] [tps] [groupId]
# CRUD update
# (Parameter explanation is the same as above)
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [update] [count] [tps] [groupId]
# CRUD remove
# (Parameter explanation is the same as above)
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [remove] [count] [tps] [groupId]
# CRUD query
# (Parameter explanation is the same as above)
$ java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceTable [query] [count] [tps] [groupId]
```

**Other Operations**

```
# bash
# if you want to build and start a chain.
$ cd fisco
$ bash build_chain.sh -l 127.0.0.1:4 -p 30300,20200,8545

# if you want to end and delete a chain
$ cd fisco
$ bash nodes/127.0.0.1/stop_all.sh
```

## License
![license](http://img.shields.io/badge/license-Apache%20v2-blue.svg)

All contributions are made under the [Apache License 2.0](http://www.apache.org/licenses/). See [LICENSE](LICENSE).