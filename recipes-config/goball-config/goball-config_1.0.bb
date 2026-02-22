SUMMARY = "GoBall device configuration"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://99-pio.rules \
           file://Banhof.nmconnection \
           file://Ethernet.nmconnection \
           file://authorized_keys"

inherit useradd

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "-r pulse-access"

do_install() {
    # PIO udev rules
    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 0644 ${WORKDIR}/99-pio.rules ${D}${sysconfdir}/udev/rules.d/

    # WiFi auto-connect profile (NetworkManager)
    install -d ${D}${sysconfdir}/NetworkManager/system-connections
    install -m 0600 ${WORKDIR}/Banhof.nmconnection ${D}${sysconfdir}/NetworkManager/system-connections/
    install -m 0600 ${WORKDIR}/Ethernet.nmconnection ${D}${sysconfdir}/NetworkManager/system-connections/

    # SSH authorized keys for root
    install -d ${D}/root/.ssh
    install -m 0600 ${WORKDIR}/authorized_keys ${D}/root/.ssh/authorized_keys
    chmod 700 ${D}/root/.ssh

}

FILES:${PN} += "/root/.ssh"
