SUMMARY = "Open Source multimedia player"
DESCRIPTION = "mpv is a media player based on MPlayer and mplayer2. \
It supports a wide variety of video file formats, audio and video codecs, \
and subtitle types."
SECTION = "multimedia"
HOMEPAGE = "https://mpv.io/"

LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE.GPL;md5=570a9b3749dd0463a1778803b12a6dce"

DEPENDS = " \
    zlib \
    ffmpeg \
    jpeg \
    libass \
    libplacebo \
"

SRC_URI = "git://github.com/mpv-player/mpv.git;protocol=https;branch=release/0.41"
SRCREV = "2c219aa822df18a1b7fd9abe3e151cd93ad67307"

S = "${WORKDIR}/git"

inherit meson pkgconfig mime-xdg

PACKAGECONFIG ??= " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'wayland egl', '', d)} \
    ${@bb.utils.filter('DISTRO_FEATURES', 'opengl pulseaudio', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'alsa', 'alsa', '', d)} \
    drm gbm lua \
"

PACKAGECONFIG[wayland] = "-Dwayland=enabled,-Dwayland=disabled,wayland wayland-native wayland-protocols libxkbcommon"
PACKAGECONFIG[egl] = "-Degl=enabled,-Degl=disabled,virtual/egl"
PACKAGECONFIG[opengl] = "-Dgl=enabled,-Dgl=disabled,virtual/libgl"
PACKAGECONFIG[drm] = "-Ddrm=enabled,-Ddrm=disabled,libdrm libdisplay-info"
PACKAGECONFIG[gbm] = "-Dgbm=enabled,-Dgbm=disabled,virtual/libgbm"
PACKAGECONFIG[lua] = "-Dlua=luajit,-Dlua=disabled,luajit"
PACKAGECONFIG[vaapi] = "-Dvaapi=enabled,-Dvaapi=disabled,libva"
PACKAGECONFIG[pulseaudio] = "-Dpulse=enabled,-Dpulse=disabled,pulseaudio"
PACKAGECONFIG[alsa] = "-Dalsa=enabled,-Dalsa=disabled,alsa-lib"

EXTRA_OEMESON = " \
    -Dmanpage-build=disabled \
    -Dlibbluray=disabled \
    -Ddvdnav=disabled \
    -Dcdda=disabled \
    -Duchardet=disabled \
    -Drubberband=disabled \
    -Dlcms2=disabled \
    -Dvapoursynth=disabled \
    -Dlibarchive=disabled \
    -Djack=disabled \
    -Dvdpau=disabled \
    -Djavascript=disabled \
    -Dcplugins=disabled \
"

FILES:${PN} += " \
    ${datadir}/icons \
    ${datadir}/zsh \
    ${datadir}/bash-completion \
    ${datadir}/fish \
    ${datadir}/metainfo \
"
