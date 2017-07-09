# social

cd osn-crawler 

mvn clean install

cd ../social/scripts

./download_redis_kafka.sh

./start_redis_zoo_kafka.sh

./status_redis_zoo_kafka.sh

cd ../social

./activator run

try it on 77.234.213.237:9000
