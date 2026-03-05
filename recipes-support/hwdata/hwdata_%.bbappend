# wlroots uses hwdata with native:true in meson (build-time code generation
# from pnp.ids), so we need a native variant of hwdata.
BBCLASSEXTEND = "native"

# hwdata installs its .pc file to ${datadir}/pkgconfig/ (data-only package).
# Meson native pkg-config only searches lib/pkgconfig, so also install there.
do_install:append() {
    install -d ${D}${libdir}/pkgconfig
    if [ -f ${D}${datadir}/pkgconfig/hwdata.pc ]; then
        cp ${D}${datadir}/pkgconfig/hwdata.pc ${D}${libdir}/pkgconfig/hwdata.pc
    fi
}
