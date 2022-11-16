DESCRIPTION = "Mediatek Wireless Drivers"
SECTION = "kernel/modules"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=c188eeeb69c0a05d0545816f1458a0c9"

inherit module

PV = "1.0"

require mt76.inc
SRC_URI = " \
    git://git@github.com/openwrt/mt76.git;protocol=https \
    file://COPYING;subdir=git \
    file://0001-mt76-add-internal-wed_tiny-header-file.patch;apply=no \
    "
SRC_URI += " \
    file://src \
    "



DEPENDS += "virtual/kernel"
DEPENDS += "linux-mac80211"

FILESEXTRAPATHS_prepend := "${THISDIR}/files/patches:"
FILESEXTRAPATHS_prepend := "${THISDIR}/src:"

#require files/patches/patches.inc
SRC_URI += "file://*.patch;apply=no"

S = "${WORKDIR}/git"

do_mtk_patches() {
    cd ${S}

    DISTRO_FlowBlock_ENABLED="${@bb.utils.contains('DISTRO_FEATURES','flow_offload','true','false',d)}"
    
    if [ ! -e mtk_wifi_patch_applied ]; then
        for i in ${WORKDIR}/*.patch
        do
        if [ $DISTRO_FlowBlock_ENABLED = 'true' ]; then
            patch -p1 < $i;
        else 
            prefix=$(echo -n "${WORKDIR}"|wc -c)
            patch_number_start=$(expr $prefix + 2)
            patch_number_end=$(expr $patch_number_start + 3 )
            patch_number=$(echo "$i" | cut -c"$patch_number_start"-"$patch_number_end")

            if [ "$patch_number" -ge "3000" ]; then
                continue;
            else
                patch -p1 < $i;
            fi
        fi
        done
    fi
    touch mtk_wifi_patch_applied
}
addtask mtk_patches after do_patch before do_configure

NOSTDINC_FLAGS = " \
    -I${B} \
    -I${STAGING_KERNEL_BUILDDIR}/usr/include/mac80211-backport/uapi \
    -I${STAGING_KERNEL_BUILDDIR}/usr/include/mac80211-backport \
    -I${STAGING_KERNEL_BUILDDIR}/usr/include/mac80211/uapi \
    -I${STAGING_KERNEL_BUILDDIR}/usr/include/mac80211 \
    -include backport/autoconf.h \
    -include backport/backport.h \
    "

PKG_MAKE_FLAGS = " \
    CONFIG_MAC80211_DEBUGFS=y \
    CONFIG_NL80211_TESTMODE=y \
    CONFIG_MT76_CONNAC_LIB=m \
    CONFIG_MT7615_COMMON=m \
    CONFIG_MT7915E=m \
    CONFIG_MT7986_WMAC=y \
    "

NOSTDINC_FLAGS += " \
    -DCONFIG_MAC80211_MESH \
    -DCONFIG_NL80211_TESTMODE \
    -DCONFIG_MAC80211_DEBUGFS \
    -DCONFIG_MT7986_WMAC \
    "

EXTRA_OEMAKE = " \
    -C ${STAGING_KERNEL_BUILDDIR}/ \
    M=${S} \
    ${PKG_MAKE_FLAGS} \
    NOSTDINC_FLAGS="${NOSTDINC_FLAGS}" \
    "

MAKE_TARGETS = "modules"

do_configure[noexec] = "1"

# make_scripts requires kernel source directory to create
# kernel scripts
do_make_scripts[depends] += "virtual/kernel:do_shared_workdir"

do_install() {
    # Module
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/drivers/net/wireless/mediatek/mt76/
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/drivers/net/wireless/mediatek/mt76/mt7615/
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/drivers/net/wireless/mediatek/mt76/mt7915/
    install -m 0644 ${B}/mt76.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/drivers/net/wireless/mediatek/mt76/
    install -m 0644 ${B}/mt76-connac-lib.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/drivers/net/wireless/mediatek/mt76/
    install -m 0644 ${B}/mt7615/mt7615-common.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/drivers/net/wireless/mediatek/mt76/mt7615/
    install -m 0644 ${B}/mt7915/mt7915e.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/drivers/net/wireless/mediatek/mt76/mt7915/
}

do_install_append () {
    install -d ${D}/${base_libdir}/firmware/mediatek/

    install -m 644 ${WORKDIR}/src/firmware/mt7915_rom_patch.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7915_wa.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7915_wm.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7915_eeprom.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7915_eeprom_dbdc.bin ${D}${base_libdir}/firmware/mediatek/

    install -m 644 ${WORKDIR}/src/firmware/mt7916_rom_patch.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7916_wa.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7916_wm.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7916_eeprom.bin ${D}${base_libdir}/firmware/mediatek/

    install -m 644 ${WORKDIR}/src/firmware/mt7986_rom_patch.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7986_rom_patch_mt7975.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7986_wa.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7986_wm.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7986_wm_mt7975.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7986_eeprom_mt7975_dual.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7986_eeprom_mt7976_dbdc.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7986_eeprom_mt7976.bin  ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7986_eeprom_mt7976_dual.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7986_wo_0.bin ${D}${base_libdir}/firmware/mediatek/
    install -m 644 ${WORKDIR}/src/firmware/mt7986_wo_1.bin ${D}${base_libdir}/firmware/mediatek/
}
FILES_${PN} += "${base_libdir}/firmware/mediatek/*"

# Make linux-mt76 depend on all of the split-out packages.
python populate_packages_prepend () {
    firmware_pkgs = oe.utils.packages_filter_out_system(d)
    d.appendVar('RDEPENDS_linux-mt76', ' ' + ' '.join(firmware_pkgs))
}

#RPROVIDES_${PN} += "kernel-module-${PN}-${KERNEL_VERSION}"
#RPROVIDES_${PN} += "kernel-module-${PN}-connac-lib-${KERNEL_VERSION}"

KERNEL_MODULE_AUTOLOAD += "mt7915e"
