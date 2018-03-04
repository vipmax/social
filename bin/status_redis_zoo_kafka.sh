echo "redis:"
ps aux | grep -v grep | grep 'redis-server' | awk '{print $2}' 

echo "zookeeper:"
jps -l | grep 'QuorumPeerMain' | awk '{print $1}'

echo "kafka:"
jps -l | grep -v grep | grep 'kafka.Kafka' | awk '{print $1}'
