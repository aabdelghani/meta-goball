# Allow all local clients to connect to PulseAudio in system mode
# Without this, root/goball cannot access the PulseAudio socket
do_install:append() {
    sed -i 's/load-module module-native-protocol-unix.*/load-module module-native-protocol-unix auth-anonymous=1/' \
        ${D}${sysconfdir}/pulse/system.pa
}