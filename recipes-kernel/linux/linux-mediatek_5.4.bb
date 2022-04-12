FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}:"
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}/generic/backport-5.4:"
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}/generic/pending-5.4:"
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}/generic/hack-5.4:"
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}/mediatek/patches-5.4:"

KBRANCH ?= "v5.4/standard/base"

LINUX_VERSION ?= "5.4.183"
SRCREV_machine ?= "f840db108606f987e174f1658dc120795798e808"
KMETA = "kernel-meta"
SRCREV_meta ?= "feeb59687bc0f054af837a5061f8d413ec7c93e9"

DEPENDS_append = " kern-tools-native xz-native bc-native"

SRC_URI = " \
    git://git.yoctoproject.org/linux-yocto.git;branch=${KBRANCH};name=machine \
    git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=yocto-5.4;destsuffix=${KMETA} \
    file://generic \
    file://mediatek \
    file://generic/defconfig \
    file://001-rdkb-eth-mtk-change-ifname-for.patch;apply=no \
    file://002-rdkb-mtd-ubi-relayout.patch;apply=no \
    "
SRC_URI += " \
    file://rdkb_cfg/iptables.cfg \
    file://rdkb_cfg/turris_rdkb.cfg \
    file://rdkb_cfg/openvswitch.cfg \
    file://rdkb_cfg/mac80211.cfg \
    file://rdkb_cfg/prplmesh.cfg \
"

SRC_URI_append_mt7986 += " \
    file://mediatek/mt7986.cfg \
"
SRC_URI_append_mt7986-32bit += " \
    file://mediatek/mt7986-32bit.cfg \
"

require ${PN}-${PV}/generic/backport-5.4/backport-5.4.inc

require ${PN}-${PV}/generic/pending-5.4/pending-5.4.inc
SRC_URI_remove = " \
    file://530-jffs2_make_lzma_available.patch \
    "
require ${PN}-${PV}/generic/hack-5.4/hack-5.4.inc
SRC_URI_remove = " \
    file://531-debloat_lzma.patch \
    "
require ${PN}-${PV}/mediatek/patches-5.4/patches-5.4.inc
SRC_URI_remove = " \
    file://1004_remove_eth_transmit_timeout_hw_reset.patch \
    file://1005-mtkhnat-fix-pse-hang-for-multi-stations.patch \
    file://738-mt7531-gsw-internal_phy_calibration.patch \
    file://739-mt7531-gsw-port5_external_phy_init.patch \
    file://9010-iwconfig-wireless-rate-fix.patch \
    file://9999-null-test.patch \
    "
require linux-mediatek.inc

do_patch_prepend () {
    cp -Rfp ${WORKDIR}/generic/files/* ${S}/
    cp -Rfp ${WORKDIR}/generic/files-5.4/* ${S}/
    cp -Rfp ${WORKDIR}/mediatek/files-5.4/* ${S}/
}

do_filogic_patches() {
    cd ${S}
        if [ ! -e patch_applied ]; then
            patch -p1 < ${WORKDIR}/001-rdkb-eth-mtk-change-ifname-for.patch
            patch -p1 < ${WORKDIR}/002-rdkb-mtd-ubi-relayout.patch
            touch patch_applied
        fi
}

addtask filogic_patches after do_patch before do_compile
