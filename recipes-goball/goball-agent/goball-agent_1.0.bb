SUMMARY = "GoBall Fleet Monitoring Agent"
DESCRIPTION = "Python agent that connects to fleet server via mTLS, publishes telemetry, and handles remote commands"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://goball_agent.py \
           file://goball-agent.service \
           file://goball-agent.conf"

RDEPENDS:${PN} += "python3-core python3-paho-mqtt"

inherit systemd

SYSTEMD_SERVICE:${PN} = "goball-agent.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install() {
    # Agent script
    install -d ${D}/opt/goball-agent
    install -m 0755 ${WORKDIR}/goball_agent.py ${D}/opt/goball-agent/goball_agent.py

    # Default config (user edits venue/location after flash)
    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/goball-agent.conf ${D}${sysconfdir}/goball-agent.conf

    # Cert directory (empty — certs deployed per-device after flash)
    install -d ${D}${sysconfdir}/goball-agent

    # Systemd service
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/goball-agent.service ${D}${systemd_system_unitdir}/goball-agent.service
}

FILES:${PN} = "/opt/goball-agent ${sysconfdir}/goball-agent.conf ${sysconfdir}/goball-agent"
