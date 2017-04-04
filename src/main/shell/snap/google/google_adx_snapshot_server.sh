#!/bin/bash

if [ $# -ne 2 ];then
    echo "Usage: google_adx_snapshot_server.sh < url with http:// prefix> < adid >"
    exit 1
fi

PHANTOMJS_CMD="phantomjs"

SNAP_JS="google_adx_snapshot.js"

DISPLAY=:0 $PHANTOMJS_CMD $SNAP_JS $1 $2
