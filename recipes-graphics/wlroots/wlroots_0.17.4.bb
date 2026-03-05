SUMMARY = "A modular Wayland compositor library"
DESCRIPTION = "wlroots is a set of pluggable, composable modules for building \
a Wayland compositor."
HOMEPAGE = "https://gitlab.freedesktop.org/wlroots/wlroots"
SECTION = "graphics"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=89e064f90bcb87796ca335cbd2ce4179"

DEPENDS = " \
    wayland \
    wayland-native \
    wayland-protocols \
    libdrm \
    libinput \
    libxkbcommon \
    pixman \
    seatd \
    virtual/egl \
    virtual/libgles2 \
    virtual/libgbm \
    hwdata \
    hwdata-native \
    libdisplay-info \
"

SRC_URI = "git://gitlab.freedesktop.org/wlroots/wlroots.git;protocol=https;branch=0.17"
SRCREV = "a2d2c38a3127745629293066beeed0a649dff8de"

S = "${WORKDIR}/git"

inherit meson pkgconfig features_check

REQUIRED_DISTRO_FEATURES = "wayland opengl"

PACKAGECONFIG ??= ""
PACKAGECONFIG[xwayland] = "-Dxwayland=enabled,-Dxwayland=disabled,xwayland libxcb xcb-util-wm"

EXTRA_OEMESON = " \
    -Dexamples=false \
    -Dbackends=drm,libinput \
    -Drenderers=gles2 \
    -Dallocators=gbm \
    -Dxwayland=disabled \
"

# hwdata.pc is installed to /usr/share/pkgconfig/ (data-only package),
# but meson cross-compilation only searches /usr/lib/pkgconfig/.
# Copy it into the recipe-sysroot lib/pkgconfig so pkg-config finds it.
do_configure:prepend() {
    if [ -f "${RECIPE_SYSROOT}${datadir}/pkgconfig/hwdata.pc" ] && \
       [ ! -f "${RECIPE_SYSROOT}${libdir}/pkgconfig/hwdata.pc" ]; then
        cp ${RECIPE_SYSROOT}${datadir}/pkgconfig/hwdata.pc \
           ${RECIPE_SYSROOT}${libdir}/pkgconfig/hwdata.pc
    fi
}

FILES:${PN} += "${libdir}/lib*.so.*"
FILES:${PN}-dev += "${libdir}/lib*.so ${libdir}/pkgconfig ${includedir}"

BBCLASSEXTEND = ""
