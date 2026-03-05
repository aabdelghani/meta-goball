FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SPLASH_IMAGES:raspberrypi5 = "file://psplash-goball.png;outsuffix=default"

# Custom bar image and colors for GoBall splash
SRC_URI:append = " file://psplash-bar.png \
                   file://psplash-colors.h"

# Fix framebuffer device path for RPi5 (axi:gpu, not platform-gpu)
SRC_URI:append:raspberrypi5 = " file://framebuf.conf"

# Fullscreen splash image (no split) — already enabled by default in recipe

# Reposition progress bar to match green rectangle in splash image
# Green bar: x=1549-2139, y=344-399 on 2560x720 screen
do_configure:prepend() {
    # Replace bar image with our custom 590x56 green-bordered bar
    cp ${WORKDIR}/psplash-bar.png ${S}/base-images/psplash-bar.png

    # Override colors
    cp ${WORKDIR}/psplash-colors.h ${S}/psplash-colors.h

    # Replace the SPLIT_LINE_POS macro definition to return fixed y=344
    sed -i '/#define SPLIT_LINE_POS/,/)$/c\#define SPLIT_LINE_POS(fb) (344)' ${S}/psplash.c

    # Patch bar X position: shift 564px right of center
    sed -i 's|x      = ((fb->width  - BAR_IMG_WIDTH)/2) + 4 ;|x      = ((fb->width  - BAR_IMG_WIDTH)/2) + 564 + 4 ;|' ${S}/psplash.c

    # Patch bar border X position
    sed -i 's|(fb->width  - BAR_IMG_WIDTH)/2,|(fb->width  - BAR_IMG_WIDTH)/2 + 564,|' ${S}/psplash.c
}
