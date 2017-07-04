 ps aux | grep -v grep | grep 'redis-server\|kafka.Kafka\|QuorumPeerMain' | awk '{print $2}' | xargs kill -9

rm -rf /tmp/zookeeper
rm -rf /tmp/kafka-logs