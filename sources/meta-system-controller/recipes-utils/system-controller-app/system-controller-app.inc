LICENSE ?= "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "^$"
COMPATIBLE_MACHINE:amd-cortexa53-common = "${MACHINE}"

BOARD ?= ""
BOARD_upper = "${@d.getVar('BOARD').upper()}"

do_install() {
	install -d ${D}${datadir}/system-controller-app/board

	install -m 0664 ${WORKDIR}/${BOARD_upper}.json ${D}${datadir}/system-controller-app/board/
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES:${PN} += "${datadir}/system-controller-app/"

RDEPENDS:${PN} = "packagegroup-syscontroller"
