#!/usr/bin/env bash
pwd

rsync -aurv \
 --exclude '.idea'       \
 --exclude '.git'        \
 --progress              \
 ./ 192.168.13.133:/home/nano/sncrawler