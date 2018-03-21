#!/bin/sh
git pull
 play run --%tbtdev  -XX:MaxPermSize=376M  -Xms2516M -Xmx2516M | colorlog
