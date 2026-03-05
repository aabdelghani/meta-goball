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

SRC_URI = "git://gitlab.freedesktop.org/wlroots/wlroots.git;protocol=https;branch=0.19"
SRCREV = "a047c2a33ff7724a476892cc4fe5dcb803607ef5"

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

# wlroots 0.19 ships an unversioned .so (libwlroots-0.19.so) that is the
# runtime library, not a dev symlink. Override SOLIBS so it lands in the
# main package.
SOLIBS = ".so"
FILES_SOLIBSDEV = ""
FILES:${PN} += "${libdir}/libwlroots-*.so"
FILES:${PN}-dev += "${libdir}/pkgconfig ${includedir}"

BBCLASSEXTEND = ""
