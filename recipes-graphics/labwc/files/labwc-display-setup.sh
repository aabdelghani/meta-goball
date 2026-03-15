#!/bin/sh
# Wait for labwc to be ready, then configure displays
sleep 1
wlr-randr --output HDMI-A-2 --mode 2560x720
wlr-randr --output HDMI-A-1 --off 2>/dev/null
exit 0
