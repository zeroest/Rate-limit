#!/bin/bash

set -e

echo "===== Starting Redis ====="
redis-server /etc/redis.conf &

echo "Start redis server in background"
sleep 4 && echo -e '';

echo "===== Master clustering ====="
redis-cli --cluster create 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 --cluster-yes

sleep 4;

echo "===== Slave clustering ====="
redis-cli --cluster add-node 127.0.0.1:7101 127.0.0.1:7001 --cluster-slave
redis-cli --cluster add-node 127.0.0.1:7102 127.0.0.1:7002 --cluster-slave
redis-cli --cluster add-node 127.0.0.1:7103 127.0.0.1:7003 --cluster-slave

redis-server

