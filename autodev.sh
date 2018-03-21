#!/bin/sh
git pull
 play run --%tbtdev  -XX:MaxPermSize=256M  -Xms2516M -Xmx2516M | colorlog
