FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SPLASH_IMAGES:raspberrypi5 = "file://psplash-goball.png;outsuffix=default"

# Fix framebuffer device path for RPi5 (axi:gpu, not platform-gpu)
SRC_URI:append:raspberrypi5 = " file://framebuf.conf"