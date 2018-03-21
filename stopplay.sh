#!/bin/sh

PID=`ps aux | grep play |grep play-1.2 | grep java | sed 's/ \+/\n/g' | sed -n '2p'`
echo PID:$PID 
kill -9 $PID
rm server.pid

