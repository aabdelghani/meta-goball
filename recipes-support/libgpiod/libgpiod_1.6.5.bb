SUMMARY = "C library and tools for interacting with the linux GPIO character device"
LICENSE = "LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=2caced0b25dfefd4c601d92bd15116de"

SRC_URI = "file://${BP}"

S = "${WORKDIR}/${BP}"

DEPENDS = "autoconf-archive-native"

inherit autotools pkgconfig

PACKAGECONFIG[tools] = "--enable-tools,--disable-tools"
PACKAGECONFIG = "tools"

PROVIDES = "libgpiod"
RPROVIDES:${PN} = "libgpiod"

PACKAGES =+ "${PN}-tools"
FILES:${PN}-tools = "${bindir}/*"
