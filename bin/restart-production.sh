cd social
rm -rf target
./compile-project.sh
./stop-project-production.sh
./start-project-production.sh