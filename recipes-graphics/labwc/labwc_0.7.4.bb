SUMMARY = "A Wayland stacking compositor"
DESCRIPTION = "labwc is a wlroots-based stacking compositor for Wayland, \
inspired by Openbox. It is lightweight and independent with a focus on \
simply stacking windows well and rendering window decorations."
HOMEPAGE = "https://github.com/labwc/labwc"
SECTION = "graphics"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b234ee4d69f5fce4486a80fdaf4a4263"

DEPENDS = " \
    wlroots \
    wayland \
    wayland-native \
    wayland-protocols \
    libdrm \
    libinput \
    libxkbcommon \
    libxml2 \
    cairo \
    pango \
    glib-2.0 \
    seatd \
    pixman \
    virtual/egl \
    virtual/libgles2 \
"

SRC_URI = "git://github.com/labwc/labwc.git;protocol=https;branch=v0.7"
SRCREV = "71136fdf65940b4e924dea7f8285d2a21032755e"

S = "${WORKDIR}/git"

inherit meson pkgconfig features_check

REQUIRED_DISTRO_FEATURES = "wayland opengl"

EXTRA_OEMESON = " \
    -Dman-pages=disabled \
    -Dxwayland=disabled \
    -Dsvg=disabled \
"

FILES:${PN} += " \
    ${datadir}/wayland-sessions \
    ${datadir}/icons \
"
