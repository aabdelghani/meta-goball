#!/bin/bash
# setup-goball.sh — Clone all Yocto layers and configure the GoBall build
#
# Usage: ./setup-goball.sh [TARGET_DIR]
#   TARGET_DIR defaults to ~/yocto/goball

set -e

TARGET_DIR="${1:-$HOME/yocto/goball}"
POKY_BRANCH="scarthgap"

echo "=== GoBall Yocto Build Setup ==="
echo "Target directory: $TARGET_DIR"
echo ""

# 1. Clone Poky
mkdir -p "$TARGET_DIR"
cd "$TARGET_DIR"
if [ ! -d poky ]; then
    echo ">>> Cloning Poky ($POKY_BRANCH)..."
    git clone -b "$POKY_BRANCH" git://git.yoctoproject.org/poky
else
    echo ">>> Poky already exists, skipping clone."
fi
cd poky
POKY_DIR="$(pwd)"

# 2. Clone dependency layers
if [ ! -d meta-raspberrypi ]; then
    echo ">>> Cloning meta-raspberrypi..."
    git clone -b "$POKY_BRANCH" git://git.yoctoproject.org/meta-raspberrypi
else
    echo ">>> meta-raspberrypi already exists, skipping."
fi

if [ ! -d meta-openembedded ]; then
    echo ">>> Cloning meta-openembedded..."
    git clone -b "$POKY_BRANCH" https://github.com/openembedded/meta-openembedded.git
else
    echo ">>> meta-openembedded already exists, skipping."
fi

# 3. Clone or symlink meta-goball
if [ ! -d meta-goball ]; then
    echo ">>> Cloning meta-goball..."
    git clone -b main https://github.com/aabdelghani/meta-goball.git
else
    echo ">>> meta-goball already exists, skipping."
fi

META_GOBALL="$POKY_DIR/meta-goball"

# 4. Initialize build environment
echo ">>> Initializing build environment..."
source oe-init-build-env build

# 5. Install build configs from templates (only if not already customized)
if [ ! -f conf/local.conf.original ]; then
    echo ">>> Installing build configuration from templates..."
    cp conf/local.conf conf/local.conf.original 2>/dev/null || true
    cp conf/bblayers.conf conf/bblayers.conf.original 2>/dev/null || true

    cp "$META_GOBALL/build-templates/local.conf.sample" conf/local.conf
    sed "s|##POKYDIR##|$POKY_DIR|g" "$META_GOBALL/build-templates/bblayers.conf.sample" > conf/bblayers.conf
else
    echo ">>> Build configs already customized, skipping template install."
fi

# 6. Handle sensitive files
CONFIG_DIR="$META_GOBALL/recipes-config/goball-config/files"
if [ ! -f "$CONFIG_DIR/Banhof.nmconnection" ]; then
    cp "$CONFIG_DIR/Banhof.nmconnection.sample" "$CONFIG_DIR/Banhof.nmconnection"
    echo ""
    echo ">>> EDIT WiFi credentials: $CONFIG_DIR/Banhof.nmconnection"
fi
if [ ! -f "$CONFIG_DIR/authorized_keys" ]; then
    cp "$CONFIG_DIR/authorized_keys.sample" "$CONFIG_DIR/authorized_keys"
    echo ""
    echo ">>> ADD your SSH public key: $CONFIG_DIR/authorized_keys"
fi

echo ""
echo "=== Setup complete! ==="
echo ""
echo "To build the image:"
echo "  cd $POKY_DIR"
echo "  source oe-init-build-env build"
echo "  bitbake goball-image"
echo ""
echo "To flash to SD card:"
echo "  sudo bmaptool copy tmp-glibc/deploy/images/raspberrypi5/goball-image-raspberrypi5.rootfs.wic.bz2 /dev/sdX"