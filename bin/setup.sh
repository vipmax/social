#!/usr/bin/env bash

git submodule update --init --recursive

sh download_redis_kafka.sh

sh start_redis_zoo_kafka.sh

cd osn-crawler && mvn clean install -DskipTests
