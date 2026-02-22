FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Disable PAM for dropbear — use native password auth against /etc/shadow
# Keep the PAM config file (required by do_install) but skip PAM patches
PAM_SRC_URI = "file://dropbear"
PACKAGECONFIG:remove = "pam"
