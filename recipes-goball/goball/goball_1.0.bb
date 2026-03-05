SUMMARY = "GoBall Mini Golf Scoring System"
DESCRIPTION = "LVGL-based mini golf scoring application with GPIO sensors and LED support"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

DEPENDS = "libsdl2 libsdl2-mixer libgpiod"

SRC_URI = "file://goball.service \
           file://pulseaudio-system.service \
           file://tap \
           file://tips_scenario \
           file://quotapoints_scenario"

inherit externalsrc cmake systemd pkgconfig

EXTERNALSRC = "/home/q/1Projects/GoBall/SquareLine_Project"
EXTERNALSRC_BUILD = "${WORKDIR}/build"

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

    install -d ${D}/opt/goball/videos
    for f in $(find ${S}/modules/game_videos -name '*.mp4'); do
        install -m 0644 "$f" ${D}/opt/goball/videos/
    done

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/goball.service ${D}${systemd_system_unitdir}/goball.service
    install -m 0644 ${WORKDIR}/pulseaudio-system.service ${D}${systemd_system_unitdir}/pulseaudio-system.service

    install -d ${D}/opt/tests
    install -m 0755 ${WORKDIR}/tap ${D}/opt/tests/tap
    install -m 0755 ${WORKDIR}/tips_scenario ${D}/opt/tests/tips_scenario
    install -m 0755 ${WORKDIR}/quotapoints_scenario ${D}/opt/tests/quotapoints_scenario
}

RDEPENDS:${PN} += "rpidistro-ffmpeg python3-core"

FILES:${PN} += "/opt/goball /opt/goball/sounds /opt/goball/videos /opt/tests"
