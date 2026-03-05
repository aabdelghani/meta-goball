SUMMARY = "Reusable library for GPU-accelerated video/image rendering"
DESCRIPTION = "libplacebo is the core rendering algorithms and ideas of mpv \
rewritten as an independent library."
HOMEPAGE = "https://code.videolan.org/videolan/libplacebo"
SECTION = "libs"
LICENSE = "LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=435ed639f84d4585d93824e7da3d85da"

SRC_URI = "git://code.videolan.org/videolan/libplacebo.git;protocol=https;branch=master;name=libplacebo \
           git://github.com/pallets/jinja.git;protocol=https;branch=main;destsuffix=git/3rdparty/jinja;name=jinja \
           git://github.com/pallets/markupsafe.git;protocol=https;branch=main;destsuffix=git/3rdparty/markupsafe;name=markupsafe \
           git://github.com/fastfloat/fast_float.git;protocol=https;branch=main;destsuffix=git/3rdparty/fast_float;name=fastfloat \
           git://github.com/Dav1dde/glad.git;protocol=https;branch=glad2;destsuffix=git/3rdparty/glad;name=glad \
           git://github.com/KhronosGroup/Vulkan-Headers.git;protocol=https;branch=main;destsuffix=git/3rdparty/Vulkan-Headers;name=vulkanheaders \
"

SRCREV_libplacebo = "b2ea27dceb6418aabfe9121174c6dbb232942998"
SRCREV_jinja = "15206881c006c79667fe5154fe80c01c65410679"
SRCREV_markupsafe = "297fc8e356e6836a62087949245d09a28e9f1b13"
SRCREV_fastfloat = "97b54ca9e75f5303507699d27c6b4f4efe4641a1"
SRCREV_glad = "73db193f853e2ee079bf3ca8a64aa2eaf6459043"
SRCREV_vulkanheaders = "450bd2232225d6c7728a4108055ac2e37cef6475"

SRCREV_FORMAT = "libplacebo_jinja_markupsafe_fastfloat_glad_vulkanheaders"

S = "${WORKDIR}/git"

inherit meson pkgconfig

DEPENDS = "python3-native python3-jinja2-native python3-markupsafe-native"

EXTRA_OEMESON = " \
    -Dvulkan=disabled \
    -Dopengl=disabled \
    -Dd3d11=disabled \
    -Dglslang=disabled \
    -Dshaderc=disabled \
    -Dlcms=disabled \
    -Ddovi=disabled \
    -Dlibdovi=disabled \
    -Ddemos=false \
    -Dtests=false \
    -Dbench=false \
    -Dfuzz=false \
    -Dunwind=disabled \
    -Dxxhash=disabled \
"

BBCLASSEXTEND = ""
