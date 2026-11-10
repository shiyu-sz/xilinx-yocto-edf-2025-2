SUMMARY = "Firmware for the vrk160-a1/a2 system controller"

PROVIDES += " systemcontroller-firmware-vrk160-a2"

include systemcontroller-firmware.inc

FW_DIR = "vrk160"
FW_FILENAME = "vrk160-a1"

COMPATIBLE_MACHINE:amd-cortexa53-common = "${MACHINE}"

do_install:append() {
    # create symbolic links to supported revisions of the same board
    for board in a1 a2 a3; do
        install -d ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR}-${board}
        ln -sr ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/${PN}.bin ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR}-${board}/${FW_DIR}-${board}.bin
        ln -sr ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/${PN}.dtbo ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR}-${board}/${FW_DIR}-${board}.dtbo
        ln -sr ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/shell.json ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR}-${board}/shell.json
    done
}

FILES:${PN} = "${nonarch_base_libdir}/firmware/xilinx/*"
