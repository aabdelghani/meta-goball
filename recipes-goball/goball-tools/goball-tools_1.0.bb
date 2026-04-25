SUMMARY = "GoBall Testing Tools"
DESCRIPTION = "GPIO loopback simulator and other testing tools for GoBall"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://gpio_loopback_simulator.py"

RDEPENDS:${PN} += "python3-core"

do_install() {
    install -d ${D}/opt/goball/tools
    install -m 0755 ${WORKDIR}/gpio_loopback_simulator.py ${D}/opt/goball/tools/gpio_loopback_simulator.py
}

FILES:${PN} = "/opt/goball/tools"
