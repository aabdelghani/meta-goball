SUMMARY = "GoBall Kiosk Image for Raspberry Pi 5"

IMAGE_FEATURES += "ssh-server-openssh splash"

inherit core-image extrausers

# Set root password to "123"
EXTRA_USERS_PARAMS = "usermod -p '\$5\$goball\$3WgxxN89Gbwt9NMiFlfW7eIlIgynHvdLkQqY5ItT3U5' root;"

# Disable getty on tty1 — labwc uses it for kiosk display
disable_getty() {
    ln -sf /dev/null ${IMAGE_ROOTFS}${systemd_system_unitdir}/getty@tty1.service
}
ROOTFS_POSTPROCESS_COMMAND += "disable_getty; write_build_info;"

write_build_info() {
    cat > ${IMAGE_ROOTFS}${sysconfdir}/goball-build-info <<BUILDEOF
BUILD_TIME=$(date -u '+%Y-%m-%d %H:%M:%S UTC')
BUILD_HOST=$(hostname)
BUILD_ID=$(date -u '+%Y%m%d%H%M%S')
META_GOBALL_REV=$(cd ${TOPDIR}/../meta-goball && git rev-parse --short HEAD 2>/dev/null || echo "unknown")
MACHINE=${MACHINE}
DISTRO=${DISTRO}
BUILDEOF
}

IMAGE_INSTALL += " \
    goball \
    libsdl2 \
    libsdl2-mixer \
    libgpiod \
    libgpiod-tools \
    goball-config \
    labwc \
    labwc-init \
    xkeyboard-config \
    wlr-randr \
    mpv \
    mpv-raise \
    grim \
    fontconfig \
    ttf-bitstream-vera \
    mesa-megadriver \
    libegl-mesa \
    libgles2-mesa \
    libgbm \
    pulseaudio \
    pulseaudio-server \
    pulseaudio-module-alsa-sink \
    pulseaudio-module-alsa-source \
    alsa-utils \
    alsa-plugins \
    kernel-modules \
    rpi-eeprom \
    rpi-gpio \
    i2c-tools \
    devmem2 \
    nano \
    htop \
    networkmanager \
    networkmanager-nmcli \
    networkmanager-wifi \
    wpa-supplicant \
    linux-firmware-rpidistro-bcm43455 \
    iw \
"
