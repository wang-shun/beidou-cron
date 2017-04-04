#!/bin/sh

./snap_supervise -p status/snap_deal -f "nohup sh ./snap_deal_ad.sh" -r ./snap_warning.sh -t 60 >/dev/null 2>&1 &

