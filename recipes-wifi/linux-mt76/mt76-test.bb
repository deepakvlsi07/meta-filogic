DESCRIPTION = "mt76-test"
SECTION = "applications"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://../COPYING;md5=c188eeeb69c0a05d0545816f1458a0c9"

DEPENDS += "libnl-tiny"

inherit pkgconfig cmake

PV = "1.0"

require mt76.inc
SRC_URI = " \
    git://git@github.com/openwrt/mt76.git;protocol=https \
    file://COPYING;subdir=git \
    file://0001-mt76-add-internal-wed_tiny-header-file.patch;apply=no \
    "




DEPENDS += "virtual/kernel"
DEPENDS += "linux-mac80211"
DEPENDS += "linux-mt76"

FILESEXTRAPATHS_prepend := "${THISDIR}/files/patches:"


CFLAGS_append = " -I=${includedir}/libnl-tiny "

S = "${WORKDIR}/git/tools"

SRC_URI += "file://*.patch;apply=no"

do_mtk_patches() {
	cd ${S}/../
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






