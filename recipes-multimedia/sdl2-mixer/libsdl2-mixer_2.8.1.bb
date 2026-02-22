SUMMARY = "SDL2 multi-channel audio mixer library"
LICENSE = "Zlib"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=fbb0010b2f7cf6e8a13bcac1ef4d2455"

DEPENDS = "libsdl2"

SRC_URI = "https://github.com/libsdl-org/SDL_mixer/releases/download/release-${PV}/SDL2_mixer-${PV}.tar.gz"
SRC_URI[sha256sum] = "cb760211b056bfe44f4a1e180cc7cb201137e4d1572f2002cc1be728efd22660"

S = "${WORKDIR}/SDL2_mixer-${PV}"

inherit cmake pkgconfig

EXTRA_OECMAKE = " \
    -DSDL2MIXER_WAVE=ON \
    -DSDL2MIXER_MP3=OFF \
    -DSDL2MIXER_FLAC=OFF \
    -DSDL2MIXER_MOD=OFF \
    -DSDL2MIXER_MIDI=OFF \
    -DSDL2MIXER_OPUS=OFF \
    -DSDL2MIXER_WAVPACK=OFF \
    -DSDL2MIXER_VORBIS=OFF \
"

FILES:${PN} += "/usr/share/licenses"

PROVIDES = "libsdl2-mixer"
RPROVIDES:${PN} = "libsdl2-mixer"
