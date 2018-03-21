#!/bin/sh
git pull
 play run --%helislave  -XX:MaxPermSize=376M  -Xms1516M -Xmx1516M | colorlog
