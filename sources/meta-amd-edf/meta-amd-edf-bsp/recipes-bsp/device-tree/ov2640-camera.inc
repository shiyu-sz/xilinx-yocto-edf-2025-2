# Linux boots with U-Boot's control DT on this board; update both recipes.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append:zynq-xc7z020-custom = " file://zynq-xc7z020-ov2640.dtsi"

do_configure:append:zynq-xc7z020-custom () {
    install -m 0644 ${WORKDIR}/zynq-xc7z020-ov2640.dtsi ${DT_FILES_PATH}/
    printf '\n#include "zynq-xc7z020-ov2640.dtsi"\n' >> ${DT_FILES_PATH}/system-top.dts
}
