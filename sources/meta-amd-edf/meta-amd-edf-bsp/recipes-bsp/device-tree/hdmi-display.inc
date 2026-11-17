# EDF's Zynq boot script passes U-Boot's control DT to Linux. Keep both
# device-tree recipes consistent, so BOOT.bin contains the display topology.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append:zynq-xc7z020-custom = " file://zynq-xc7z020-hdmi.dtsi"

# uboot-device-tree does not implement EXTRA_DT_INCLUDE_FILES. Both XSCT
# generators produce system-top.dts, so include the board fragment explicitly.
do_configure:append:zynq-xc7z020-custom () {
    install -m 0644 ${WORKDIR}/zynq-xc7z020-hdmi.dtsi ${DT_FILES_PATH}/
    printf '\n#include "zynq-xc7z020-hdmi.dtsi"\n' >> ${DT_FILES_PATH}/system-top.dts
}
