#!/usr/bin/env bash

git submodule update --init --recursive

cd bin
if [ ! -d "kafka" ]; then
 sh download_redis_kafka.sh
fi

sh stop_redis_zoo_kafka.sh
sh start_redis_zoo_kafka.sh
cd ..

cd osn-crawler && mvn clean install -DskipTests && cd ..

cd social && ./activator run && cd ..
