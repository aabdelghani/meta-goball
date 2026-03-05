SUMMARY = "EDID and DisplayID library"
DESCRIPTION = "libdisplay-info provides a set of high-level, easy-to-use, \
opinionated functions as well as low-level functions for accessing detailed \
EDID and DisplayID display information, prioritizing simplicity and \
correctness over performance."
HOMEPAGE = "https://gitlab.freedesktop.org/emersion/libdisplay-info"
SECTION = "libs"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e4426409957080ee0352128354cea2de"

DEPENDS = "hwdata-native"

SRC_URI = "git://gitlab.freedesktop.org/emersion/libdisplay-info.git;protocol=https;branch=main"
SRCREV = "92b031749c0fe84ef5cdf895067b84a829920e25"

S = "${WORKDIR}/git"

inherit meson pkgconfig lib_package

# hwdata.pc is installed to /usr/share/pkgconfig/ (data-only package),
# but meson cross-compilation only searches /usr/lib/pkgconfig/.
# Copy it into the recipe-sysroot lib/pkgconfig so pkg-config finds it.
do_configure:prepend() {
    if [ -f "${RECIPE_SYSROOT_NATIVE}${datadir}/pkgconfig/hwdata.pc" ] && \
       [ ! -f "${RECIPE_SYSROOT_NATIVE}${libdir}/pkgconfig/hwdata.pc" ]; then
        install -d ${RECIPE_SYSROOT_NATIVE}${libdir}/pkgconfig
        cp ${RECIPE_SYSROOT_NATIVE}${datadir}/pkgconfig/hwdata.pc \
           ${RECIPE_SYSROOT_NATIVE}${libdir}/pkgconfig/hwdata.pc
    fi
}

FILES:${PN} += "${libdir}/lib*.so.*"
FILES:${PN}-dev += "${libdir}/lib*.so ${libdir}/pkgconfig ${includedir}"
