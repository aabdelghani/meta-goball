SUMMARY = "Keep mpv window always on top via wlr-foreign-toplevel-management"
DESCRIPTION = "Wayland client that uses the wlr-foreign-toplevel-management protocol \
to periodically activate the mpv window, keeping it above other windows."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "wayland wayland-native wayland-protocols"

SRC_URI = "file://mpv-raise.service"

inherit systemd pkgconfig

SYSTEMD_SERVICE:${PN} = "mpv-raise.service"
SYSTEMD_AUTO_ENABLE = "enable"

# Source lives in the GoBall tree
MPV_RAISE_SRC = "/home/q/1Projects/GoBall/SquareLine_Project/tools/mpv-raise"

do_compile() {
    # Generate protocol bindings from XML
    wayland-scanner client-header \
        ${MPV_RAISE_SRC}/wlr-foreign-toplevel-management-unstable-v1.xml \
        ${B}/wlr-foreign-toplevel-management-unstable-v1-client-protocol.h

    wayland-scanner private-code \
        ${MPV_RAISE_SRC}/wlr-foreign-toplevel-management-unstable-v1.xml \
        ${B}/wlr-foreign-toplevel-management-unstable-v1-protocol.c

    # Compile
    ${CC} ${CFLAGS} ${LDFLAGS} \
        -I${B} \
        ${MPV_RAISE_SRC}/mpv-raise.c \
        ${B}/wlr-foreign-toplevel-management-unstable-v1-protocol.c \
        -o ${B}/mpv-raise \
        $(pkg-config --cflags --libs wayland-client)
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/mpv-raise ${D}${bindir}/mpv-raise

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/mpv-raise.service ${D}${systemd_system_unitdir}/mpv-raise.service
}
