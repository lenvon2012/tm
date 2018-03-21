#!/bin/sh
 play run -XX:MaxPermSize=1024M  -Xms2048M -Xmx2048M --%th | colorlog
