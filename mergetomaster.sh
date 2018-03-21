#!/bin/sh
git co prod && git merge master && git push  && git co master
