#!/bin/sh

git pull
nohup play run --%tbtdev  -XX:MaxPermSize=512M  -Xms1024M -Xmx1024M  > nohup.out &

