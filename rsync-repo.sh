#!/usr/bin/env bash
pwd

rsync -aurv \
 --exclude '.idea'       \
 --exclude '.git'        \
 --exclude 'logs'        \
 --exclude '*.log'       \
 --progress              \
 ./ 192.168.13.133:/home/nano/sncrawler