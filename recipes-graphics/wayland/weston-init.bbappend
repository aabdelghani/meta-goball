FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Kiosk mode: no idle timeout
PACKAGECONFIG:append = " no-idle-timeout"
