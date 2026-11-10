SUMMARY = "Firmware for the ve-p-a1225-00-a01 system controller"

include systemcontroller-firmware.inc

FW_DIR = "ve-p-a1225-00-a01"
FW_FILENAME = "ve-p-a1225-00-a01"

COMPATIBLE_MACHINE:amd-cortexa53-common = "${MACHINE}"

do_install:append() {
    install -d ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR}
    ln -sr ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/${PN}.bin ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR}/${FW_DIR}.bin
    ln -sr ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/${PN}.dtbo ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR}/${FW_DIR}.dtbo
    ln -sr ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/shell.json ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR}/shell.json
}

FILES:${PN} = "${nonarch_base_libdir}/firmware/xilinx/*"
