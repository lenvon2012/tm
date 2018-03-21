#!/bin/sh
git pull
 play run --%defender  -XX:MaxPermSize=376M  -Xms1516M -Xmx1516M | colorlog
