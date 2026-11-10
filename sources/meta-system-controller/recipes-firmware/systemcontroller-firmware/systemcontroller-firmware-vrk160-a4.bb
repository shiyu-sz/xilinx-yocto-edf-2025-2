SUMMARY = "Firmware for the vrk160-a4 system controller"

include systemcontroller-firmware.inc

FW_DIR = "vrk160-a4"
FW_FILENAME = "vrk160-a4"

COMPATIBLE_MACHINE:amd-cortexa53-common = "${MACHINE}"

do_install:append() {
    install -d ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR}
    ln -sr ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/${PN}.bin ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR}/${FW_DIR}.bin
    ln -sr ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/${PN}.dtbo ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR}/${FW_DIR}.dtbo
    ln -sr ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/shell.json ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR}/shell.json
}

FILES:${PN} = "${nonarch_base_libdir}/firmware/xilinx/*"
