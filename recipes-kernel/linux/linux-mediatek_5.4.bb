FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}:"
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}/generic/backport-5.4:"
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}/generic/pending-5.4:"
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}/generic/hack-5.4:"
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}/mediatek/patches-5.4:"
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}/mediatek/flow_patch:"
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}/mediatek/nf_hnat:"

KBRANCH ?= "linux-5.4.y"

LINUX_VERSION ?= "5.4.230"
SRCREV_machine ?= "90245959a5b936ee013266e5d1e6a508ed69274e"
KMETA = "kernel-meta"
SRCREV_meta ?= "feeb59687bc0f054af837a5061f8d413ec7c93e9"

DEPENDS_append = " kern-tools-native xz-native bc-native"

SRC_URI = " \
    git://git.kernel.org/pub/scm/linux/kernel/git/stable/linux.git;protocol=https;branch=${KBRANCH};name=machine \
    git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=yocto-5.4;destsuffix=${KMETA} \
    file://generic \
    file://mediatek \
    file://generic/defconfig \
    file://001-rdkb-eth-mtk-change-ifname-for.patch;apply=no \
    file://003-rdkb-mtd-kernel-ubi-relayout.patch;apply=no \
    file://004-rdkb-hnat-bind-ifname.patch;apply=no \
    "
SRC_URI_append_mt7988 += " \
    file://mediatek/mt7988.cfg \
"
SRC_URI_append_mt7986 += " \
    file://mediatek/mt7986.cfg \
"
SRC_URI_append_mt7986-32bit += " \
    file://mediatek/patches-32bit-5.4/mt7986-32bit.cfg \
    file://mediatek/patches-32bit-5.4/401-pinctrl-add-mt7986-driver-32bit.patch \
    file://mediatek/patches-32bit-5.4/999-add_armv7_support_for_panther.patch \
"
SRC_URI_append += " \
    file://rdkb_cfg/iptables.cfg \
    file://rdkb_cfg/turris_rdkb.cfg \
    file://rdkb_cfg/openvswitch.cfg \
    ${@bb.utils.contains('DISTRO_FEATURES', 'mt76', 'file://rdkb_cfg/mac80211.cfg', 'file://rdkb_cfg/nf_hnat.cfg', d)} \
    file://rdkb_cfg/prplmesh.cfg \
    file://rdkb_cfg/filogic_rdkb.cfg \
    ${@bb.utils.contains('DISTRO_FEATURES','emmc','file://rdkb_cfg/emmc.cfg','',d)} \
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

SRC_URI_remove_mt7986-32bit = " \
    file://401-pinctrl-add-mt7986-driver.patch \
    "
require linux-mediatek.inc

do_patch_prepend () {
    cp -Rfp ${WORKDIR}/generic/files/* ${S}/
    cp -Rfp ${WORKDIR}/generic/files-5.4/* ${S}/
    cp -Rfp ${WORKDIR}/mediatek/files-5.4/* ${S}/
}

do_filogic_patches() {
    cd ${S}
    DISTRO_FlowBlock_ENABLED="${@bb.utils.contains('DISTRO_FEATURES','flow_offload','true','false',d)}"
    DISTRO_logan_ENABLED="${@bb.utils.contains('DISTRO_FEATURES','logan','true','false',d)}"
        if [ ! -e patch_applied ]; then
            patch -p1 < ${WORKDIR}/001-rdkb-eth-mtk-change-ifname-for.patch
            patch -p1 < ${WORKDIR}/003-rdkb-mtd-kernel-ubi-relayout.patch
            patch -p1 < ${WORKDIR}/0600-net-phylink-propagate-resolved-link-config-via-mac_l.patch
            patch -p1 < ${WORKDIR}/738-mt7531-gsw-internal_phy_calibration.patch
            patch -p1 < ${WORKDIR}/739-mt7531-gsw-port5_external_phy_init.patch
            patch -p1 < ${WORKDIR}/753-net-mt753x-phy-coverity-scan.patch
            patch -p1 < ${WORKDIR}/9010-iwconfig-wireless-rate-fix.patch
            if [ $DISTRO_FlowBlock_ENABLED = 'true' ]; then
                for i in ${WORKDIR}/mediatek/flow_patch/*.patch; do patch -p1 < $i; done
            fi
            if [ $DISTRO_logan_ENABLED = 'true' ]; then
                for i in ${WORKDIR}/mediatek/nf_hnat/6*.patch; do patch -p1 < $i; done
                for i in ${WORKDIR}/mediatek/nf_hnat/10*.patch; do patch -p1 < $i; done
                patch -p1 < ${WORKDIR}/004-rdkb-hnat-bind-ifname.patch
            fi
            touch patch_applied
        fi
}

addtask filogic_patches after do_patch before do_compile

KERNEL_MODULE_AUTOLOAD += "${@bb.utils.contains('DISTRO_FEATURES','logan','mtkhnat nf_flow_table_hw','',d)}"