#!/bin/bash

# Please firstly prepare "build_chain.sh" and "fisco-bcos" binary before running this script. 
# https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/installation.html

type=$1

# single pc
if [[ $type -eq 1 ]]
then
  num_of_nodes=$2

  bash nodes/127.0.0.1/stop_all.sh
  rm -rf nodes/

  bash build_chain.sh -l 127.0.0.1:$num_of_nodes -p 30300,20200,8545
  bash nodes/127.0.0.1/start_all.sh

  cp -r nodes/127.0.0.1/sdk/* ../dist/conf/

# multiple pc
elif [[ $type -eq 2 ]]
then

  local_ip=$2
  username="intcl"
  passwd="intcl2021"

  rm -rf nodes/

  bash build_chain.sh -f ipconf -p 30300,20200,8545

  bash ~/fisco/$local_ip/stop_all.sh && rm -rf ~/fisco
  mkdir -p ~/fisco
  cp -r nodes/$local_ip/ ~/fisco/$local_ip
  bash ~/fisco/$local_ip/start_all.sh

  exec 3<aliveip.txt

  while read ip<&3;
  do
  if [[ $local_ip == $ip ]]
  then
    echo "skip local ip"
    continue
  fi
  sshpass -p $passwd ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $username@$ip "bash ~/fisco/"$ip"/stop_all.sh && rm -rf ~/fisco"
  sshpass -p $passwd ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $username@$ip "mkdir -p ~/fisco"
  sshpass -p $passwd scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -r nodes/$ip/ $username@$ip:~/fisco/$ip
  sshpass -p $passwd ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $username@$ip "bash ~/fisco/"$ip"/start_all.sh"
  done

  cp -r nodes/$local_ip/sdk/* ~/java-sdk-demo/dist/conf/

else
  echo "check your parameter"
fi
