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
- **Boot Splash:** Custom psplash with GoBall branding (5s visible before compositor)
- **Compositor:** labwc 0.9.5 (wlroots 0.19.2, pure Wayland — no XWayland)
- **Renderer:** SDL2 Wayland backend + OpenGL ES 2.0 (Mesa)
- **Video Playback:** mpv 0.41.0 via native Wayland (always-on-top via mpv-raise + wlr-foreign-toplevel-management)
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
│   ├── psplash/                     # Custom boot splash (GoBall branding)
│   └── dropbear/                    # Dropbear SSH overrides
├── recipes-goball/goball/
│   ├── goball_1.0.bb                # Main app recipe (externalsrc from local)
│   └── files/
│       ├── goball.service
│       ├── tap                      # Virtual touch input script
│       ├── tips_scenario            # Tips screen test scenario
│       └── quotapoints_scenario     # Quota points test scenario
├── recipes-graphics/
│   ├── labwc/                       # labwc compositor + init config
│   │   ├── labwc_0.9.5.bb
│   │   ├── labwc-init.bb
│   │   └── files/
│   │       ├── labwc.service
│   │       ├── rc.xml               # Window rules, kiosk mode, cursor hiding
│   │       ├── environment          # WLR env vars
│   │       └── labwc-env
│   ├── wlroots/wlroots_0.19.2.bb   # Compositor library (pure Wayland)
│   ├── libdisplay-info/             # EDID library (wlroots dependency)
│   └── wlr-randr/                   # Output management tool
├── recipes-support/
│   ├── libgpiod/                    # libgpiod v1.6.5 (bundled source)
│   └── hwdata/                      # hwdata native bbappend for wlroots
├── recipes-config/goball-config/
│   ├── goball-config_1.0.bb
│   └── files/                       # udev rules, network profiles, SSH keys
├── recipes-multimedia/
│   ├── sdl2-mixer/                  # SDL2_mixer (fetched from GitHub releases)
│   ├── mpv/mpv_0.41.0.bb           # mpv video player (Wayland, PulseAudio, ALSA, LuaJIT OSC)
│   └── libplacebo/libplacebo_7.360.0.bb  # GPU-accelerated rendering (mpv dependency)
├── recipes-goball/mpv-raise/
│   └── mpv-raise_1.0.bb            # Keeps mpv always on top via wlr-foreign-toplevel protocol
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

# Rebuild app only (uses local externalsrc)
bitbake goball -c cleansstate && bitbake goball-image

# Rebuild after config file changes
bitbake goball-config -c cleansstate && bitbake goball-image

# Rebuild after labwc config changes (rc.xml, environment)
bitbake labwc-init -c cleansstate && bitbake goball-image

# Rebuild after splash image changes
bitbake psplash -c cleansstate && bitbake goball-image
```

## Quick Deploy (Without Reflash)

```bash
# Stop app, copy new binary, restart
sshpass -p '123' ssh root@10.0.0.2 "systemctl stop goball.service"
sshpass -p '123' scp /path/to/build/goball root@10.0.0.2:/usr/bin/goball
sshpass -p '123' ssh root@10.0.0.2 "systemctl start goball.service"
```

## Boot Splash

The image includes a custom psplash boot splash (`psplash-goball.png`, 2560x720) that displays during early boot. labwc startup is delayed 5 seconds to keep the splash visible.

To replace the splash image, put your PNG in `recipes-core/psplash/files/psplash-goball.png` and rebuild:
```bash
bitbake psplash -c cleansstate && bitbake goball-image
```

**RPi5 psplash fix:** The meta-raspberrypi layer's `framebuf.conf` references a framebuffer device path that udev doesn't activate on RPi5. This layer overrides it to remove the broken dependency.

## Test Scripts

Test scripts are installed to `/opt/tests/` on the device:

| Script | Purpose |
|---|---|
| `tap` | Python virtual touchscreen — sends touch events at coordinates or named positions |
| `tips_scenario` | Navigates to Tips screen and plays first video |
| `quotapoints_scenario` | Full game flow: select mode, set players, edit names |

Run from SSH:
```bash
/opt/tests/tips_scenario
/opt/tests/quotapoints_scenario
```

## First Boot

1. Insert SD card into RPi5
2. Connect HDMI to ultrawide display (**use HDMI port 1** on the board = HDMI-A-2 in DRM)
3. Power on — GoBall splash appears, then labwc starts and the app launches automatically

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
systemctl status labwc                 # Compositor status
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

## Compositor: labwc

labwc is a wlroots-based stacking compositor chosen because:
- Supports window positioning and wlr-foreign-toplevel-management protocol
- Matches Raspberry Pi OS compositor stack
- Lightweight, kiosk-friendly configuration via `rc.xml`

### Key Configuration (rc.xml)

- `<decoration>none</decoration>` — no window decorations globally
- `<cursor><hide>0</hide></cursor>` — hides mouse cursor immediately (kiosk/touchscreen mode)
- mpv window rule: no decoration, fixed position
- goball window rule: maximized, no decoration
- `<mouse>` with Focus-only binding (no Raise)
- mpv-raise service: uses `wlr-foreign-toplevel-management` protocol to keep mpv on top (activates every 500ms)
- Empty `<keyboard>` section for kiosk mode

### Display Resolution

Set via `wlr-randr` startup command in labwc.service:
```
ExecStart=/usr/bin/labwc -s "wlr-randr --output HDMI-A-2 --mode 2560x720"
```

## Related

- [GoBall Application](https://github.com/aabdelghani/GoBall) — Source code for the mini golf scoring app
- [GoBall_Yocto_Documentation.md](GoBall_Yocto_Documentation.md) — Comprehensive Yocto beginner guide

## License

MIT — see [COPYING.MIT](COPYING.MIT)
