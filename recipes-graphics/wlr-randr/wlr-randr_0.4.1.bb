SUMMARY = "An xrandr clone for wlroots compositors"
DESCRIPTION = "wlr-randr is a utility to manage outputs of a Wayland \
compositor implementing wlr-output-management-unstable-v1."
HOMEPAGE = "https://gitlab.freedesktop.org/emersion/wlr-randr"
SECTION = "graphics"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=07e8a8f2dc9e6a7f131e81037398c61b"

DEPENDS = "wayland wayland-native wayland-protocols"

SRC_URI = "git://gitlab.freedesktop.org/emersion/wlr-randr.git;protocol=https;branch=master"
SRCREV = "b0784201233fc2f31555039e02371fd8ce7cee1f"

S = "${WORKDIR}/git"

inherit meson pkgconfig
