# social

cd osn-crawler 

mvn clean install

cd ../social/scripts

./download_redis_kafka.sh

./start_redis_zoo_kafka.sh

./status_redis_zoo_kafka.sh

cd ..

./activator run
