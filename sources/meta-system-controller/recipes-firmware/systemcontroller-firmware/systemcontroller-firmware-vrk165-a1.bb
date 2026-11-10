SUMMARY = "Firmware recipe for the vrk165-a1 SC which is symlink to vrk160"

include systemcontroller-firmware.inc

FW_DIR = "vrk160"
FW_FILENAME = "vrk160-a1"

FW_DIR_vrk165 = "vrk165"

COMPATIBLE_MACHINE:amd-cortexa53-common = "${MACHINE}"

do_install:append() {
    # create symbolic links to supported revisions of the same board
    for board in a1 a4; do
        install -d ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR_vrk165}-${board}
        ln -sr ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/${PN}.bin ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR_vrk165}-${board}/${FW_DIR_vrk165}-${board}.bin
        ln -sr ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/${PN}.dtbo ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR_vrk165}-${board}/${FW_DIR_vrk165}-${board}.dtbo
        ln -sr ${D}${nonarch_base_libdir}/firmware/xilinx/${PN}/shell.json ${D}${nonarch_base_libdir}/firmware/xilinx/${FW_DIR_vrk165}-${board}/shell.json
    done
}

FILES:${PN} = "${nonarch_base_libdir}/firmware/xilinx/*"
