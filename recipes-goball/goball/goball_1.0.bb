SUMMARY = "GoBall Mini Golf Scoring System"
DESCRIPTION = "LVGL-based mini golf scoring application with GPIO sensors and LED support"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "libsdl2 libsdl2-mixer libgpiod"

SRC_URI = "git://github.com/aabdelghani/GoBall.git;protocol=https;branch=master \
           file://goball.service \
           file://pulseaudio-system.service"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit cmake systemd pkgconfig

EXTRA_OECMAKE = "-DCMAKE_BUILD_TYPE=Debug -DYOCTO_BUILD=ON -DSOUND_DIR_PATH=/opt/goball/sounds/"

SYSTEMD_SERVICE:${PN} = "goball.service pulseaudio-system.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/SquareLine_Project ${D}${bindir}/goball

    install -d ${D}/opt/goball/sounds
    for f in $(find ${S}/modules/game_sounds -name '*.wav'); do
        install -m 0644 "$f" ${D}/opt/goball/sounds/
    done

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/goball.service ${D}${systemd_system_unitdir}/goball.service
    install -m 0644 ${WORKDIR}/pulseaudio-system.service ${D}${systemd_system_unitdir}/pulseaudio-system.service
}

FILES:${PN} += "/opt/goball /opt/goball/sounds"
