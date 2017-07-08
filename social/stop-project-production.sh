echo "play social:"
jps -l | grep 'play.core.server.ProdServerStart' | xargs echo
jps -l | grep 'play.core.server.ProdServerStart' | awk '{print $1}' | xargs kill -9

rm /home/nano/sncrawler/social/target/universal/stage/RUNNING_PID