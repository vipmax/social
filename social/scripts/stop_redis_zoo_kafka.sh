ps aux | grep -v grep | grep 'redis-server' | awk '{print $2}' | xargs kill -9

jps -l | grep 'kafka.Kafka' | awk '{print $1}' | xargs kill -9
jps -l | grep 'org.apache.zookeeper.server.quorum.QuorumPeerMain' | awk '{print $1}' | xargs kill -9

rm -rf /tmp/zookeeper
rm -rf /tmp/kafka-logs
rm dump.rdb
rm redis.log zoo.log kafka.log