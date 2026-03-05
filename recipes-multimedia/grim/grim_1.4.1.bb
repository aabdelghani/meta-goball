SUMMARY = "Screenshot tool for Wayland compositors"
DESCRIPTION = "Grab images from a Wayland compositor using the \
wlr-screencopy protocol."
HOMEPAGE = "https://git.sr.ht/~emersion/grim"
SECTION = "graphics"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e316e9609dd7672b87ff25b46b2cf3e1"

DEPENDS = "wayland wayland-native wayland-protocols pixman libpng jpeg"

SRC_URI = "git://git.sr.ht/~emersion/grim;protocol=https;branch=master"
SRCREV = "7ba46364ab95141c79e0e18093aa66597256182c"

S = "${WORKDIR}/git"

inherit meson pkgconfig
