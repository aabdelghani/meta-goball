#!/bin/sh
# Configure RPi5 EEPROM on first boot
# Disables diagnostic screen and network install prompt

FLAGFILE="/var/lib/goball/eeprom-configured"

# Skip if already configured
[ -f "$FLAGFILE" ] && exit 0

# Read current config
CURRENT=$(rpi-eeprom-config 2>/dev/null)
[ -z "$CURRENT" ] && exit 1

NEEDS_UPDATE=0
TMPCONF=$(mktemp)
echo "$CURRENT" > "$TMPCONF"

# Ensure DISPLAY_DIAGNOSTIC=0
if echo "$CURRENT" | grep -q "DISPLAY_DIAGNOSTIC=1"; then
    sed -i 's/DISPLAY_DIAGNOSTIC=1/DISPLAY_DIAGNOSTIC=0/' "$TMPCONF"
    NEEDS_UPDATE=1
elif ! echo "$CURRENT" | grep -q "DISPLAY_DIAGNOSTIC"; then
    echo "DISPLAY_DIAGNOSTIC=0" >> "$TMPCONF"
    NEEDS_UPDATE=1
fi

# Ensure NET_INSTALL_AT_POWER_ON=0
if echo "$CURRENT" | grep -q "NET_INSTALL_AT_POWER_ON=1"; then
    sed -i 's/NET_INSTALL_AT_POWER_ON=1/NET_INSTALL_AT_POWER_ON=0/' "$TMPCONF"
    NEEDS_UPDATE=1
elif ! echo "$CURRENT" | grep -q "NET_INSTALL_AT_POWER_ON"; then
    echo "NET_INSTALL_AT_POWER_ON=0" >> "$TMPCONF"
    NEEDS_UPDATE=1
fi

if [ "$NEEDS_UPDATE" = "1" ]; then
    echo "Applying EEPROM configuration..."
    rpi-eeprom-config --apply "$TMPCONF"
    echo "EEPROM update staged. Will apply on next reboot."
fi

rm -f "$TMPCONF"

# Mark as configured
mkdir -p /var/lib/goball
touch "$FLAGFILE"
exit 0