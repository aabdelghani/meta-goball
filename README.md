# meta-goball — Yocto BSP Layer for GoBall (RPi5)

Custom Yocto Linux image for the [GoBall](https://github.com/aabdelghani/GoBall) mini golf scoring system running on Raspberry Pi 5.

## Quick Start

```bash
# Clone this repo and run the setup script
git clone https://github.com/aabdelghani/meta-goball.git
cd meta-goball
./setup-goball.sh ~/yocto/goball

# Build the image
cd ~/yocto/goball/poky
source oe-init-build-env build
bitbake goball-image

# Flash to SD card
sudo bmaptool copy tmp-glibc/deploy/images/raspberrypi5/goball-image-raspberrypi5.rootfs.wic.bz2 /dev/sdX
```

The setup script clones all required Yocto layers (poky, meta-raspberrypi, meta-openembedded) and installs the build configuration automatically.

### Credentials Setup

After running `setup-goball.sh`, edit these files with your actual credentials:

| File | What to change |
|------|---------------|
| `recipes-config/goball-config/files/Banhof.nmconnection` | WiFi SSID and password |
| `recipes-config/goball-config/files/authorized_keys` | Your SSH public key |

These files are gitignored. The repo ships `.sample` versions as templates.

## Overview

- **Yocto Branch:** scarthgap (LTS)
- **Machine:** raspberrypi5
- **Distro:** goball-distro (GoBall OS 1.0)
- **Display:** 2560x720 ultrawide via HDMI-A-2 (KMS/DRM, custom CVT mode)
- **Compositor:** Weston (kiosk-shell)
- **Renderer:** SDL2 Wayland backend + OpenGL ES 2.0 (Mesa)
- **Audio:** PulseAudio (system mode) -> ALSA -> HDMI audio
- **GPIO:** libgpiod v1.6.5 (ball sensors)
- **LEDs:** WS2812 via RP1 PIO userspace driver — currently disabled
- **UI Framework:** LVGL 9.x with SDL2 backend
- **Init:** systemd
- **SSH:** OpenSSH (root login, password + key auth)
- **Networking:** NetworkManager (WiFi + Ethernet)

## Layer Structure

```
meta-goball/
├── conf/
│   ├── layer.conf
│   └── distro/goball-distro.conf
├── build-templates/
│   ├── local.conf.sample
│   └── bblayers.conf.sample
├── recipes-core/
│   ├── images/goball-image.bb
│   └── dropbear/                    # Dropbear SSH overrides
├── recipes-goball/goball/
│   ├── goball_1.0.bb                # Main app recipe (fetches from GitHub)
│   └── files/goball.service
├── recipes-graphics/wayland/
│   ├── weston-init.bbappend
│   └── weston-init/weston.ini
├── recipes-config/goball-config/
│   ├── goball-config_1.0.bb
│   └── files/                       # udev rules, network profiles, SSH keys
├── recipes-support/libgpiod/        # libgpiod v1.6.5 (bundled source)
├── recipes-multimedia/sdl2-mixer/   # SDL2_mixer (fetched from GitHub releases)
├── setup-goball.sh                  # One-command build environment setup
├── deploy-config.conf               # Deployment parameter reference
└── GoBall_Yocto_Documentation.md    # Comprehensive beginner guide
```

## Build Configuration

Build configs are in `build-templates/`. The setup script installs them automatically. Key settings in `local.conf`:

| Setting | Purpose |
|---|---|
| `video=HDMI-A-2:2560x720@60D` | Forces custom 2560x720 CVT mode via KMS |
| `console=tty3 quiet loglevel=0` | Silent boot (no kernel messages on screen) |
| `disable_splash=1` | Disables GPU rainbow splash |
| `RPI_USE_U_BOOT = "0"` | Direct firmware boot (U-Boot causes black screen on RPi5) |
| `debug-tweaks` | Allows root SSH login for development |

## Rebuilding After Changes

```bash
cd ~/yocto/goball/poky && source oe-init-build-env build

# Rebuild app only (fetches latest from GitHub)
bitbake goball -c cleansstate && bitbake goball-image

# Rebuild after config file changes
bitbake goball-config -c cleansstate && bitbake goball-image

# Rebuild after weston.ini changes
bitbake weston-init -c cleansstate && bitbake goball-image
```

## First Boot

1. Insert SD card into RPi5
2. Connect HDMI to ultrawide display (**use HDMI port 1** on the board = HDMI-A-2 in DRM)
3. Power on — Weston starts and GoBall launches automatically

### SSH Access

```bash
# Ethernet (direct PC connection, set PC to 10.0.0.1/24)
ssh root@10.0.0.2    # password: 123

# WiFi
ssh root@<dhcp-ip>
```

### Debugging

```bash
systemctl status goball                # App status
journalctl -u goball --no-pager -n 50  # App logs
systemctl status weston                # Compositor status
nmcli device status                    # Network status
```

## Hardware

- **SoC:** Broadcom BCM2712 (RPi5)
- **Display:** AOC CU34G2XP ultrawide (3440x1440 native, driven at 2560x720)
- **HDMI Port:** HDMI-A-2 (physical port 1 on RPi5 board)
- **GPIO Sensors:** Pins 17, 24, 26, 27
- **LEDs:** WS2812 strips via RP1 PIO (GPIO 2+3) — currently disabled
- **Ethernet:** Built-in, static IP 10.0.0.2
- **WiFi:** BCM43455

## Related

- [GoBall Application](https://github.com/aabdelghani/GoBall) — Source code for the mini golf scoring app
- [GoBall_Yocto_Documentation.md](GoBall_Yocto_Documentation.md) — Comprehensive Yocto beginner guide

## License

MIT — see [COPYING.MIT](COPYING.MIT)