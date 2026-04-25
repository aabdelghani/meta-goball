# Enable OpenSSL so FFmpeg (and mpv-via-ffmpeg) can open https:// streams.
# Without this, the PGA Tour Radio HLS URL fails with
#   "No protocol handler found to open URL https://..."
PACKAGECONFIG:append = " openssl"
LICENSE_FLAGS_ACCEPTED:append = " commercial"
