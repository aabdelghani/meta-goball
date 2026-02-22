# GoBall Embedded System - Complete Project Documentation

**Project:** GoBall Mini Golf Scoring System
**Platform:** Raspberry Pi 5
**Build System:** Yocto Project (scarthgap LTS)
**Last Updated:** 2026-02-22

---

## Table of Contents

1. [What is This Project?](#1-what-is-this-project)
2. [Understanding Embedded Linux](#2-understanding-embedded-linux)
3. [The Yocto Project - From Zero](#3-the-yocto-project---from-zero)
4. [Poky - The Reference Distribution](#4-poky---the-reference-distribution)
5. [Layers (Metas) Explained](#5-layers-metas-explained)
6. [Our Layers and Why We Need Each One](#6-our-layers-and-why-we-need-each-one)
7. [Recipes - The Building Blocks](#7-recipes---the-building-blocks)
8. [Our Recipe Hierarchy](#8-our-recipe-hierarchy)
9. [The Build Process](#9-the-build-process)
10. [System Architecture](#10-system-architecture)
11. [Boot Sequence](#11-boot-sequence)
12. [Display Pipeline](#12-display-pipeline)
13. [The GoBall Application Stack](#13-the-goball-application-stack)
14. [Hardware Interface](#14-hardware-interface)
15. [Networking](#15-networking)
16. [Configuration Files Reference](#16-configuration-files-reference)
17. [Common Tasks](#17-common-tasks)
18. [Troubleshooting History](#18-troubleshooting-history)
19. [Glossary](#19-glossary)

---

## 1. What is This Project?

GoBall is a **mini golf scoring system** that runs on a Raspberry Pi 5 connected to an ultrawide display (2560x720). It uses:

- **Sensors** (GPIO pins) to detect when a golf ball enters different holes
- **An LED strip** (WS2812) around the play field for visual effects
- **A touchscreen-style UI** built with LVGL (a graphics library)
- **Sound effects** for player announcements and scoring events

The entire system boots into a **kiosk mode** — there's no desktop, no login screen, no terminal. Power on the Pi, and the scoring UI appears automatically.

To make this work, we build a **custom Linux operating system** from scratch using the Yocto Project. This isn't a regular Raspberry Pi OS — it's a minimal, purpose-built Linux that contains only what we need.

---

## 2. Understanding Embedded Linux

### Why not just use Raspberry Pi OS?

You could, but:

| Feature | Raspberry Pi OS | Custom Yocto Image |
|---|---|---|
| Size | ~4 GB | ~400 MB |
| Boot time | 30-60 seconds | ~8 seconds |
| Unused services | Hundreds | Zero |
| Attack surface | Large | Minimal |
| Reproducibility | Manual setup | Automated build |
| Update mechanism | apt (manual) | Image-based (reliable) |

An embedded Linux system is built specifically for one job. Our image contains exactly the kernel, drivers, libraries, and application needed to run GoBall — nothing more.

### Key Concepts

- **Cross-compilation**: We build the software on a powerful x86 PC (the "host") but the output binary runs on the ARM-based RPi5 (the "target"). You can't just `gcc main.c` — you need a special compiler that outputs ARM instructions.
- **Root filesystem (rootfs)**: The entire file system that gets written to the SD card. Contains `/usr/bin/goball`, all libraries, kernel, device tree, etc.
- **Device tree**: A data structure that tells the Linux kernel what hardware is present on the board (which GPIO pins exist, where HDMI controllers are, etc.)

---

## 3. The Yocto Project - From Zero

### What is Yocto?

The **Yocto Project** is a set of tools for building custom Linux distributions for embedded hardware. Think of it as a "Linux distro factory" — you tell it what hardware you have, what software you want, and it produces a complete bootable image.

### Core Components

```
+--------------------------------------------------+
|              The Yocto Project                    |
|                                                   |
|  +------------+  +----------+  +--------------+  |
|  | BitBake    |  | OpenEmb. |  | Poky         |  |
|  | (build     |  | (recipes |  | (reference   |  |
|  |  engine)   |  |  & meta) |  |  distro)     |  |
|  +------------+  +----------+  +--------------+  |
+--------------------------------------------------+
```

- **BitBake**: The build engine. Like `make` but much smarter. It reads recipes, resolves dependencies, downloads source code, cross-compiles everything, and assembles the final image.
- **OpenEmbedded-Core (OE-Core)**: The core set of recipes — Linux kernel, glibc, busybox, systemd, gcc, etc. These are the fundamental building blocks of any Linux system.
- **Poky**: A reference distribution that bundles BitBake + OE-Core + sample configs. It's your starting point.

### How Yocto Works (Simplified)

```
   You write:                    BitBake does:              You get:

   local.conf          ──>   1. Parse all recipes     ──>  SD card image
   (what machine?)           2. Download source code        with Linux +
                             3. Cross-compile each          your app
   bblayers.conf       ──>   4. Package into RPMs
   (what layers?)            5. Build root filesystem
                             6. Create bootable image
   recipes/*.bb        ──>
   (what software?)
```

---

## 4. Poky - The Reference Distribution

**Poky** is what you clone first. It contains:

```
poky/
├── meta/                  # OE-Core: fundamental recipes (kernel, glibc, gcc...)
├── meta-poky/             # Poky distro config (just a reference, we override it)
├── meta-yocto-bsp/        # Board support for reference hardware (beaglebone, etc.)
├── bitbake/               # The BitBake build engine
├── scripts/               # Helper scripts
├── oe-init-build-env      # THE script you source to start a build
└── build/                 # Created when you source oe-init-build-env
    └── conf/
        ├── local.conf     # YOUR machine/build settings
        └── bblayers.conf  # Which layers to include
```

When you run `source oe-init-build-env build`, it:
1. Sets up environment variables
2. Adds `bitbake` to your PATH
3. Creates `build/conf/` with template configs
4. Changes directory to `build/`

---

## 5. Layers (Metas) Explained

### What is a Layer?

A **layer** is a collection of recipes, configuration, and metadata that adds functionality to your build. Layers are named `meta-*` by convention.

Think of layers like **plugins**:

```
                    ┌─────────────────────┐
                    │   Your Custom Image  │
                    └─────────┬───────────┘
          ┌───────────────────┼───────────────────┐
          │                   │                   │
   ┌──────┴──────┐    ┌──────┴──────┐    ┌──────┴──────┐
   │ meta-goball │    │meta-raspi   │    │meta-oe      │
   │ (your app)  │    │ (RPi5 BSP)  │    │ (extras)    │
   └──────┬──────┘    └──────┬──────┘    └──────┬──────┘
          │                   │                   │
          └───────────────────┼───────────────────┘
                    ┌─────────┴───────────┐
                    │   meta (OE-Core)    │
                    │  kernel, glibc, gcc │
                    └─────────────────────┘
```

### Why Layers?

- **Separation of concerns**: Hardware support is separate from your application, which is separate from the base OS
- **Reusability**: meta-raspberrypi works for ANY RPi project, not just ours
- **Override mechanism**: Higher-priority layers can modify recipes from lower layers using `.bbappend` files
- **Version tracking**: Each layer has its own git repo and branch

### Layer Priority

Each layer has a priority number. When two layers provide the same recipe, the higher-priority one wins.

```
meta (OE-Core)         priority 5   (base recipes)
meta-poky              priority 5   (reference distro)
meta-raspberrypi       priority 9   (RPi hardware support)
meta-openembedded      priority 6   (extra community recipes)
meta-goball            priority 10  (our customizations — highest, wins conflicts)
```

---

## 6. Our Layers and Why We Need Each One

Our `bblayers.conf` includes 9 layers:

### Core Layers (required for any Yocto build)

| Layer | Purpose | What happens without it |
|---|---|---|
| `meta` | OE-Core: Linux kernel, glibc, gcc, coreutils, systemd | Nothing compiles |
| `meta-poky` | Poky reference distro configs | No default distro settings |
| `meta-yocto-bsp` | Reference BSP (board support) | Not strictly needed for RPi, but provides defaults |

### Hardware Support Layer

| Layer | Purpose | What happens without it |
|---|---|---|
| `meta-raspberrypi` | RPi5 machine definition, GPU drivers, boot firmware, config.txt generation, device tree overlays | BitBake doesn't know what "raspberrypi5" is |

This layer provides:
- Machine config (`conf/machine/raspberrypi5.conf`) — defines CPU architecture, kernel type, boot files
- Boot firmware (`recipes-bsp/bootfiles/`) — the proprietary GPU firmware that boots the Pi
- `rpi-config` recipe — generates `config.txt` from Yocto variables
- `rpi-cmdline` recipe — generates kernel command line
- `rpi-eeprom` recipe — EEPROM bootloader tools
- Device tree overlays (vc4-kms-v3d for GPU, etc.)

### OpenEmbedded Extra Layers

| Layer | Purpose | What it provides for us |
|---|---|---|
| `meta-oe` | Extra community recipes | Utilities, development tools |
| `meta-python` | Python packages | Dependencies for some OE recipes |
| `meta-multimedia` | Audio/video libraries | PulseAudio, ALSA plugins |
| `meta-networking` | Network management | NetworkManager recipes |

### Our Custom Layer

| Layer | Purpose |
|---|---|
| `meta-goball` | Everything specific to GoBall: the app recipe, display config, network config, boot splash, GPIO library, SDL2 mixer, systemd services |

---

## 7. Recipes - The Building Blocks

### What is a Recipe?

A **recipe** (`.bb` file) is instructions for BitBake to build one piece of software. It answers:

1. **Where** is the source code? (git repo, tarball, local files)
2. **How** to build it? (cmake, autotools, meson, make)
3. **What** does it depend on? (other recipes that must be built first)
4. **Where** to install the output? (/usr/bin, /usr/lib, /etc)

### Anatomy of a Recipe

```bitbake
# metadata
SUMMARY = "GoBall Mini Golf Scoring System"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=..."

# where to get source
SRC_URI = "git://github.com/aabdelghani/GoBall.git;protocol=https;branch=master"
SRCREV = "${AUTOREV}"

# build dependencies (must be built before this recipe)
DEPENDS = "libsdl2 libsdl2-mixer libgpiod"

# build system
inherit cmake

# cmake flags
EXTRA_OECMAKE = "-DYOCTO_BUILD=ON"

# install additional files
do_install:append() {
    install -m 0644 ${WORKDIR}/goball.service ${D}${systemd_system_unitdir}/
}
```

### Recipe Types

| Type | Extension | Purpose |
|---|---|---|
| Recipe | `.bb` | Build instructions for one software package |
| Append | `.bbappend` | Modify an existing recipe from another layer without editing it |
| Image | `.bb` (in recipes-core/images/) | Defines which packages go into the final SD card image |
| Config | `.conf` | Machine, distro, or layer configuration |

### How .bbappend Works

When we want to customize a recipe from another layer (e.g., change Dropbear SSH config), we DON'T edit the original recipe. Instead, we create a `.bbappend` file with the same name:

```
Original (meta/recipes-core/dropbear/dropbear_%.bb):
  → Installs dropbear with default config

Our append (meta-goball/recipes-core/dropbear/dropbear_%.bbappend):
  → Overrides the config file to allow root login
```

The `%` is a wildcard — our append applies to ANY version of the dropbear recipe.

---

## 8. Our Recipe Hierarchy

### Dependency Graph

```
                        goball-image.bb
                     (the final SD card image)
                              │
          ┌───────────────────┼───────────────────────┐
          │                   │                       │
    ┌─────┴─────┐    ┌───────┴───────┐    ┌─────────┴──────────┐
    │  goball   │    │ goball-config │    │ System packages    │
    │ (our app) │    │ (device cfg) │    │ weston, mesa,      │
    └─────┬─────┘    └───────────────┘    │ pulseaudio, etc.  │
          │                               └──────────────────────┘
    ┌─────┼──────────┐
    │     │          │
┌───┴───┐│    ┌──────┴──────┐
│libsdl2││    │libsdl2-mixer│
└───┬───┘│    └──────┬──────┘
    │    │           │
    │  ┌─┴────────┐  │
    │  │ libgpiod │  │
    │  └──────────┘  │
    │                │
    └────────┬───────┘
             │
    ┌────────┴────────┐
    │  OE-Core base   │
    │ (glibc, kernel, │
    │  systemd, etc.) │
    └─────────────────┘
```

### All Recipes in meta-goball

| Recipe | Category | What it builds | Why we need it |
|---|---|---|---|
| `goball_1.0.bb` | Application | The GoBall scoring app | Our main application |
| `goball-config_1.0.bb` | Configuration | Network profiles, udev rules, SSH keys | Device setup on first boot |
| `goball-image.bb` | Image | The complete SD card image | Combines everything into one flashable file |
| `libgpiod_1.6.5.bb` | Support library | GPIO access library v1.6.5 | Ball sensor detection (v2.x has incompatible API) |
| `libsdl2-mixer_2.8.1.bb` | Multimedia | SDL2 audio mixer | Sound effects (WAV playback only) |
| `dropbear_%.bbappend` | SSH override | Modifies Dropbear SSH config | Allows root login for development |
| `weston-init.bbappend` | Display override | Modifies Weston compositor config | Kiosk mode, custom resolution |
| `psplash_%.bbappend` | Boot splash override | Custom boot splash image | Shows "GoBall Loading..." during boot |

### Package Dependencies Explained

**Why does goball need libsdl2?**
SDL2 (Simple DirectMedia Layer) provides the window/surface that LVGL renders into. LVGL is a GUI framework — it can draw buttons and text, but it needs something to put pixels on screen. SDL2 handles the display backend (Wayland in our case), input events, and audio output.

**Why do we need Mesa?**
SDL2's Wayland backend requires OpenGL (even for 2D rendering). Mesa is the open-source OpenGL implementation. Specifically:
- `mesa-megadriver` — GPU driver (vc4/v3d for RPi5)
- `libegl-mesa` — EGL library (connects OpenGL to Wayland)
- `libgles2-mesa` — OpenGL ES 2.0 (the actual GL implementation)
- `libgbm` — Buffer management for graphics memory

**Why libgpiod and not just /dev/gpiomem?**
libgpiod is the modern Linux way to access GPIO pins. It uses the kernel's GPIO character device (`/dev/gpiochip*`) instead of the deprecated sysfs interface (`/sys/class/gpio/`). We specifically need v1.6.5 because v2.x has a completely different API.

**Why a custom SDL2_mixer?**
The upstream SDL2_mixer recipe pulls in many audio codec libraries (mp3, flac, ogg, midi...). We only play WAV files, so we build a stripped-down version with all codecs disabled to save space and reduce dependencies.

---

## 9. The Build Process

### Step by Step

```
1. source oe-init-build-env build
   └─> Sets PATH, creates build/conf/ if needed

2. bitbake goball-image
   └─> BitBake starts:
       a. Parses ALL .bb and .bbappend files in all layers
       b. Resolves dependency tree (5000+ tasks)
       c. For each recipe:
          i.   do_fetch      — Download source (git clone, wget)
          ii.  do_unpack     — Extract to work directory
          iii. do_patch      — Apply any patches
          iv.  do_configure  — Run cmake/autoconf/meson
          v.   do_compile    — Cross-compile the source
          vi.  do_install    — Install to staging area
          vii. do_package    — Create RPM/DEB/IPK packages
       d. do_rootfs  — Assemble all packages into root filesystem
       e. do_image   — Create bootable .wic image with partitions

3. Output: build/tmp-glibc/deploy/images/raspberrypi5/
   ├── goball-image-raspberrypi5.rootfs.wic.bz2  (SD card image)
   └── goball-image-raspberrypi5.rootfs.wic.bmap  (block map for fast flashing)
```

### Build Directories

```
build/
├── conf/
│   ├── local.conf          # Your machine & build settings
│   └── bblayers.conf       # Which layers to include
├── tmp-glibc/
│   ├── deploy/
│   │   └── images/
│   │       └── raspberrypi5/  # Output images go here
│   ├── work/
│   │   └── cortexa76-oe-linux/
│   │       ├── goball/1.0/          # GoBall build tree
│   │       ├── libsdl2/2.30.1/      # SDL2 build tree
│   │       └── ...                  # Every recipe has a work dir
│   └── sysroots-components/         # Cross-compilation sysroot
└── cache/                           # BitBake parse cache
```

### First Build vs Incremental

| | First Build | Incremental |
|---|---|---|
| Time | 2-6 hours | 5-15 minutes |
| What happens | Everything from scratch | Only changed recipes rebuild |
| Bandwidth | Downloads ~5GB of source | Nothing (cached) |
| CPU usage | 100% on all cores | Brief spike |

To force a recipe to rebuild (e.g., after pushing new code to GitHub):
```bash
bitbake goball -c cleansstate    # Delete all build artifacts for goball
bitbake goball-image             # Rebuild image (goball will be re-fetched & built)
```

---

## 10. System Architecture

### Software Stack (from bottom to top)

```
┌──────────────────────────────────────────────────────┐
│                    GoBall Application                 │
│            (LVGL UI + game logic + sound)             │
├──────────────┬───────────────┬───────────┬───────────┤
│  SDL2        │  SDL2_mixer   │ libgpiod  │ piolib    │
│  (display)   │  (audio)      │ (sensors) │ (LEDs)    │
├──────────────┼───────────────┼───────────┼───────────┤
│  Weston      │  PulseAudio   │ /dev/     │ /dev/     │
│  (Wayland    │  → ALSA       │ gpiochip4 │ pio0      │
│  compositor) │               │           │           │
├──────────────┴───────────────┴───────────┴───────────┤
│              Linux Kernel 6.6.x (aarch64)            │
│        vc4/v3d GPU │ GPIO │ PIO │ ALSA │ Network    │
├──────────────────────────────────────────────────────┤
│              RPi5 Boot Firmware (EEPROM + GPU)       │
├──────────────────────────────────────────────────────┤
│              Hardware: BCM2712 SoC (Cortex-A76)      │
│              HDMI × 2 │ GPIO × 40 │ PIO │ Ethernet  │
└──────────────────────────────────────────────────────┘
```

### Process Tree (at runtime)

```
PID 1: systemd
  ├── weston (Wayland compositor — owns the display)
  │     └── GoBall (Wayland client — renders the UI)
  ├── pulseaudio (audio server)
  ├── NetworkManager (network management)
  ├── dropbear (SSH server)
  └── journald (logging)
```

---

## 11. Boot Sequence

What happens from power-on to the GoBall UI appearing:

```
Power On
  │
  ▼
RPi5 EEPROM Bootloader (burned into chip)
  │ - Reads EEPROM config (DISPLAY_DIAGNOSTIC=0 hides splash)
  │ - Finds SD card, reads boot partition
  │ - Loads GPU firmware (start4.elf)
  ▼
GPU Firmware
  │ - Reads config.txt (resolution, kernel path, etc.)
  │ - Initializes HDMI at 2560x720 (custom CVT mode)
  │ - Loads kernel Image + device tree
  │ - disable_splash=1 prevents rainbow screen
  ▼
Linux Kernel
  │ - console=tty3 (invisible) + quiet + loglevel=0
  │ - logo.nologo (no Tux penguin)
  │ - Initializes drivers: GPU, GPIO, PIO, Network, ALSA
  │ - Mounts root filesystem from SD card partition 2
  │ - Starts systemd (PID 1)
  ▼
psplash (early userspace)
  │ - Shows "GoBall Loading..." on framebuffer
  │ - Runs until Weston takes over the display
  ▼
systemd
  │ - Starts services in dependency order:
  │   1. NetworkManager (network)
  │   2. PulseAudio (audio)
  │   3. Weston (display compositor)
  │   4. GoBall (after weston.service is ready)
  ▼
Weston (kiosk-shell)
  │ - Takes over display from psplash
  │ - Creates Wayland socket (wayland-1)
  │ - Sets HDMI-A-2 to 2560x720
  ▼
GoBall Application
  │ - Connects to Wayland via SDL2
  │ - SDL2 loads libGLESv2.so.2 for rendering
  │ - LVGL initializes 2560x720 display
  │ - UI appears — system is ready!
  ▼
Running (kiosk mode — no way to exit without SSH)
```

---

## 12. Display Pipeline

### The Problem We Solved

Getting pixels from the GoBall app to the screen involves a complex chain:

```
LVGL draws pixels
       │
       ▼
SDL2 Wayland backend
       │ ← SDL_VIDEO_GL_DRIVER=libGLESv2.so.2 (CRITICAL!)
       │ ← Without this, SDL2 tries to load libGL.so.1
       │   which doesn't exist (no X11 = no desktop GL)
       │   and the window silently fails to create
       ▼
Wayland protocol (shared memory / EGL buffer)
       │
       ▼
Weston compositor (kiosk-shell)
       │ ← weston.ini: mode=2560x720 on HDMI-A-2
       ▼
KMS/DRM (kernel display subsystem)
       │ ← video=HDMI-A-2:2560x720@60D (kernel cmdline)
       │   Creates custom CVT mode since monitor doesn't
       │   natively support 2560x720
       ▼
vc4/v3d GPU driver (Mesa)
       │
       ▼
HDMI-A-2 output → Monitor
```

### Why 5 Different Places Set the Resolution

This is the unfortunate reality of embedded Linux display pipelines:

| Location | File | What it controls |
|---|---|---|
| config.txt `hdmi_cvt` | local.conf → config.txt | GPU firmware mode table (fallback) |
| Kernel `video=` param | local.conf → cmdline | KMS/DRM mode selection at boot |
| weston.ini `mode=` | weston.ini | Weston compositor mode request |
| `hal_init(w, h)` | main.c | LVGL display buffer size |
| `LV_SDL_FULLSCREEN` | lv_conf.h | Whether SDL window fills the screen |

All 5 must agree, or you get black screens, wrong resolution, or misaligned rendering.

---

## 13. The GoBall Application Stack

```
GoBall Application (main.c)
├── LVGL 9.x (UI framework)
│   ├── Screens: Welcome, Game Setup, Scorecard, etc.
│   ├── Widgets: Buttons, labels, tables, charts
│   └── SDL2 display driver (lv_sdl_window.c)
├── Game Logic (modules/logic/)
│   ├── gpio_event — Sensor polling, ball detection
│   ├── game_modes — Strokeplay, Quotaplay, Vegas
│   └── player — Score tracking, turn management
├── LED Logic (modules/led_logic/)
│   └── led_logic_event — WS2812 animations via PIO
├── Sound Logic (modules/sound_logic/)
│   └── sound_logic_event — WAV playback via SDL2_mixer
└── Debug System (modules/debug/)
    └── debug — Leveled logging (TRACE/INFO/WARN/ERROR)
```

### Build Flags

| Flag | Value | Effect |
|---|---|---|
| `YOCTO_BUILD=ON` | CMake | Disables hardcoded cross-compiler paths, uses Yocto toolchain |
| `SOUND_DIR_PATH=/opt/goball/sounds/` | CMake | Absolute path to sound files on target |
| `CMAKE_BUILD_TYPE=Debug` | CMake | Full debug logging. Use `Release` for WARN-only |

---

## 14. Hardware Interface

### GPIO Pin Mapping

```
RPi5 GPIO Header
  ┌─────────────────────┐
  │ Pin 17 ── 3pt hole  │  Score: 3 points
  │ Pin 24 ── 0pt hole  │  Score: 0 points (gutter)
  │ Pin 26 ── 4pt hole  │  Score: 4 points
  │ Pin 27 ── 5pt hole  │  Score: 5 points
  └─────────────────────┘
  Debounce: 3000ms (prevents double-detection)
  Library: libgpiod v1.6.5
  Device: /dev/gpiochip4 (RPi5 GPIO controller)
```

### LED Strip

```
WS2812 LED Strip (144 pixels)
  ┌──────────────────────────────┐
  │ GPIO 3 ── Strip 1 data line │
  │ GPIO 2 ── Strip 2 data line │
  │ Protocol: 800kHz WS2812     │
  │ Controller: RP1 PIO (pio0)  │
  │ Status: DISABLED (no strips │
  │         physically connected)│
  └──────────────────────────────┘
```

### Display

```
AOC CU34G2XP Ultrawide Monitor
  ┌──────────────────────────────────────────────┐
  │ Native: 3440x1440 @ 144Hz                   │
  │ Driven: 2560x720 @ 60Hz (custom CVT mode)   │
  │ Connection: HDMI-A-2 (physical port 1)       │
  │ NOTE: "Port 1" on the RPi5 board maps to     │
  │       HDMI-A-2 in Linux DRM. Confusing!      │
  └──────────────────────────────────────────────┘
```

---

## 15. Networking

### Ethernet (Development)

```
PC (10.0.0.1/24) ──── Ethernet cable ──── RPi5 (10.0.0.2/24)

# On your PC:
sudo ip addr add 10.0.0.1/24 dev enp113s0
sudo ip link set enp113s0 up
ssh root@10.0.0.2
```

### WiFi

Pre-configured in `Banhof.nmconnection`. Auto-connects on boot if the network is in range.

### Priority

Ethernet (priority 200) is preferred over WiFi (priority 100) when both are available.

---

## 16. Configuration Files Reference

### Files That Live in meta-goball (Built Into the Image)

| File | On-device location | Purpose |
|---|---|---|
| `goball.service` | `/usr/lib/systemd/system/goball.service` | Auto-starts GoBall |
| `weston.ini` | `/etc/xdg/weston/weston.ini` | Display compositor settings |
| `Banhof.nmconnection` | `/etc/NetworkManager/system-connections/` | WiFi profile |
| `Ethernet.nmconnection` | `/etc/NetworkManager/system-connections/` | Static IP profile |
| `99-pio.rules` | `/etc/udev/rules.d/` | PIO device permissions |
| `authorized_keys` | `/home/root/.ssh/authorized_keys` | SSH public keys |

### Files Generated During Build

| File | On-device location | Generated from |
|---|---|---|
| `config.txt` | `/boot/config.txt` | `RPI_EXTRA_CONFIG` in local.conf |
| kernel cmdline | `/proc/cmdline` | `CMDLINE:append` in local.conf |

### Files That Persist in EEPROM (Survive Reflash)

| Setting | How to change |
|---|---|
| `DISPLAY_DIAGNOSTIC=0` | `rpi-eeprom-config --apply` on running device |
| `BOOT_ORDER` | Same tool |
| `BOOT_UART` | Same tool |

---

## 17. Common Tasks

### Push code changes and rebuild

```bash
# 1. Push GoBall code to GitHub
cd ~/1Projects/GoBall/SquareLine_Project
git add -A && git commit -m "description" && git push

# 2. Rebuild Yocto image
cd ~/yocto/goball/poky
source oe-init-build-env build
bitbake goball -c cleansstate    # Force re-fetch from GitHub
bitbake goball-image             # Rebuild image

# 3. Flash to SD card
sudo bmaptool copy \
    build/tmp-glibc/deploy/images/raspberrypi5/goball-image-raspberrypi5.rootfs.wic.bz2 \
    /dev/sdX
```

### Change WiFi credentials

Edit `meta-goball/recipes-config/goball-config/files/Banhof.nmconnection`, then:
```bash
bitbake goball-config -c cleansstate && bitbake goball-image
```

### Change display resolution

Must update ALL of these:
1. `build/conf/local.conf` — `hdmi_cvt=` and `video=HDMI-A-2:WxH@60D`
2. `meta-goball/recipes-graphics/wayland/weston-init/weston.ini` — `mode=WxH`
3. `GoBall source main.c` — `hal_init(W, H)`
4. `GoBall source lv_conf.h` — `LV_SDL_FULLSCREEN` (0 or 1)

Then rebuild everything.

### Debug on the running device

```bash
ssh root@10.0.0.2

# Check service status
systemctl status goball
systemctl status weston

# View live logs
journalctl -u goball -f

# Restart the app
systemctl restart goball

# Stop app and run manually
systemctl stop goball
SDL_VIDEODRIVER=wayland SDL_VIDEO_GL_DRIVER=libGLESv2.so.2 \
    WAYLAND_DISPLAY=wayland-1 XDG_RUNTIME_DIR=/run/weston \
    /usr/bin/goball
```

---

## 18. Troubleshooting History

### Issues Encountered and Resolved

| # | Issue | Root Cause | Fix | Date |
|---|---|---|---|---|
| 1 | Black screen on boot | Missing `kernel=Image` in config.txt | Added to `RPI_EXTRA_CONFIG` | 2026-02-20 |
| 2 | Black screen (U-Boot) | U-Boot experimental on RPi5 | `RPI_USE_U_BOOT = "0"` | 2026-02-20 |
| 3 | LED DMA blocks app | PIO DMA hangs without physical strips | Fork-based probe + force-disable | 2026-02-21 |
| 4 | Audio crash | `init_audio_system()` failure was fatal | Made non-fatal | 2026-02-21 |
| 5 | SDL2 window not rendering | SDL2 Wayland forces `libGL.so.1` (doesn't exist) | Mesa packages + `SDL_VIDEO_GL_DRIVER=libGLESv2.so.2` | 2026-02-22 |
| 6 | EEPROM bootloader splash | RPi5 EEPROM diagnostic enabled | `DISPLAY_DIAGNOSTIC=0` via `rpi-eeprom-config` | 2026-02-22 |
| 7 | Boot console text visible | Console output on tty1 | `console=tty3 quiet loglevel=0` | 2026-02-22 |
| 8 | Custom resolution ignored | KMS ignores firmware hdmi_cvt | `video=HDMI-A-2:2560x720@60D` kernel param | 2026-02-22 |
| 9 | Wrong HDMI port | Monitor on HDMI-A-2 | Updated weston.ini and kernel cmdline | 2026-02-22 |
| 10 | PulseAudio not connecting | System mode timing | Non-fatal (app continues) | OPEN |

---

## 19. Glossary

| Term | Definition |
|---|---|
| **BitBake** | The build engine that reads recipes and compiles everything |
| **BSP** | Board Support Package — hardware-specific layer (e.g., meta-raspberrypi) |
| **Cross-compilation** | Compiling on x86 PC to produce ARM binaries for RPi5 |
| **CVT mode** | Coordinated Video Timing — standard formula for generating display timings from resolution + refresh rate |
| **Device tree** | Data structure describing hardware layout, loaded by kernel at boot |
| **DRM/KMS** | Direct Rendering Manager / Kernel Mode Setting — Linux display subsystem |
| **EDID** | Extended Display Identification Data — monitor's supported modes, sent via HDMI |
| **EEPROM** | Electrically Erasable ROM — RPi5 stores its bootloader here (persists across SD reflash) |
| **EGL** | Embedded Graphics Library — connects OpenGL to the display system (Wayland/X11) |
| **GLES2** | OpenGL ES 2.0 — mobile/embedded OpenGL variant (what we use) |
| **GPIO** | General Purpose Input/Output — digital pins for sensors and LEDs |
| **KMS** | Kernel Mode Setting — kernel manages display modes (not userspace) |
| **Layer** | A collection of recipes (meta-*) that adds functionality to the build |
| **LVGL** | Light and Versatile Graphics Library — embedded GUI framework |
| **Mesa** | Open-source OpenGL/Vulkan implementation (GPU driver) |
| **OE-Core** | OpenEmbedded-Core — base recipes for any Linux system |
| **PIO** | Programmable IO — RPi5's custom peripheral for bitbanging protocols (WS2812) |
| **Poky** | Yocto reference distribution (BitBake + OE-Core + sample configs) |
| **psplash** | Plymouth-like splash screen for embedded Linux boot |
| **Recipe** | A `.bb` file with instructions to build one software package |
| **rootfs** | Root filesystem — the entire `/` directory tree on the target |
| **SDL2** | Simple DirectMedia Layer — cross-platform multimedia library |
| **systemd** | Linux init system and service manager (PID 1) |
| **Wayland** | Modern display protocol (replacement for X11) |
| **Weston** | Reference Wayland compositor implementation |
| **WIC** | Wic Image Creator — Yocto tool that creates partitioned disk images |
| **WS2812** | Addressable RGB LED protocol (NeoPixel) |
