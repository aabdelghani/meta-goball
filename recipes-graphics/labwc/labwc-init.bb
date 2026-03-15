SUMMARY = "Startup configuration for labwc Wayland compositor"
DESCRIPTION = "Provides systemd service, environment, and rc.xml config for labwc"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://labwc.service \
    file://labwc-env \
    file://labwc-display-setup.sh \
    file://rc.xml \
    file://environment \
"

S = "${WORKDIR}"

inherit systemd

RDEPENDS:${PN} = "labwc"

do_install() {
    # systemd service
    install -D -m0644 ${WORKDIR}/labwc.service ${D}${systemd_system_unitdir}/labwc.service

    # Environment file for systemd service
    install -D -m0644 ${WORKDIR}/labwc-env ${D}${sysconfdir}/default/labwc

    # labwc config directory
    install -d ${D}${sysconfdir}/labwc
    install -m0644 ${WORKDIR}/rc.xml ${D}${sysconfdir}/labwc/rc.xml
    install -m0644 ${WORKDIR}/environment ${D}${sysconfdir}/labwc/environment

    # Display setup script (called by labwc -s)
    install -D -m0755 ${WORKDIR}/labwc-display-setup.sh ${D}${bindir}/labwc-display-setup.sh

    # /run/labwc is created at runtime by ExecStartPre in labwc.service
}

SYSTEMD_SERVICE:${PN} = "labwc.service"
SYSTEMD_AUTO_ENABLE = "enable"

FILES:${PN} += " \
    ${sysconfdir}/labwc \
    ${sysconfdir}/default/labwc \
"

CONFFILES:${PN} = " \
    ${sysconfdir}/labwc/rc.xml \
    ${sysconfdir}/labwc/environment \
    ${sysconfdir}/default/labwc \
"
