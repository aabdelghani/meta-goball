SUMMARY = "GoBall Kiosk Image for Raspberry Pi 5"

IMAGE_FEATURES += "ssh-server-openssh"

inherit core-image extrausers

# Set root password to "123"
EXTRA_USERS_PARAMS = "usermod -p '\$5\$goball\$3WgxxN89Gbwt9NMiFlfW7eIlIgynHvdLkQqY5ItT3U5' root;"

# Disable getty on tty1 — Weston uses it for kiosk display
disable_getty() {
    ln -sf /dev/null ${IMAGE_ROOTFS}${systemd_system_unitdir}/getty@tty1.service
}
ROOTFS_POSTPROCESS_COMMAND += "disable_getty;"

IMAGE_INSTALL += " \
    goball \
    libsdl2 \
    libsdl2-mixer \
    libgpiod \
    libgpiod-tools \
    goball-config \
    weston \
    weston-init \
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
    rpi-gpio \
    i2c-tools \
    devmem2 \
    nano \
    htop \
    networkmanager \
    networkmanager-nmcli \
    wpa-supplicant \
    linux-firmware-rpidistro-bcm43455 \
    iw \
"
