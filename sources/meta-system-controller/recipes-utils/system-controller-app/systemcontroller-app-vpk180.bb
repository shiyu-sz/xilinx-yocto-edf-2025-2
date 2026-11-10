SUMMARY = "System Controller App - VPK180 board specific files"

LICENSE = "Proprietary & MIT"
LIC_FILES_CHKSUM:append = " file://${WORKDIR}/LICENSE_BINARIES.md;md5=fef5c700acd3d5fa08c24279a8126704"

SRC_URI = "https://edf.amd.com/sswreleases/rel-v2025.2/system-controller/sc_app_bsp/2025.2/2025.2_202603241741/external/systemcontroller-app-vpk180.tar.gz"
SRC_URI[sha256sum] = "3a4e10a1a7d4c950e93327182209eb8ff1f423f8f5d14bd89954d8556be5eda1"

BOARD = "vpk180"

require system-controller-app.inc

do_install:append() {
	install -d ${D}${datadir}/system-controller-app/BIT/${BOARD_upper}
	install -d ${D}${datadir}/scweb/static/images/
	install -d ${D}${datadir}/scweb/static/js/

	install -m 0664 ${WORKDIR}/${BOARD}_es1_system_wrapper.pdi ${D}${datadir}/system-controller-app/BIT/${BOARD_upper}/es1_system_wrapper.pdi
	install -m 0664 ${WORKDIR}/${BOARD}_system_wrapper.pdi ${D}${datadir}/system-controller-app/BIT/${BOARD_upper}/system_wrapper.pdi
	install -m 0664 ${WORKDIR}/${BOARD}_versal_bit.elf ${D}${datadir}/system-controller-app/BIT/${BOARD_upper}/versal_bit.elf

	install -m 0664 ${WORKDIR}/${BOARD_upper}_home.png ${D}${datadir}/scweb/static/images/
	install -m 0664 ${WORKDIR}/${BOARD}.jpg ${D}${datadir}/scweb/static/images/
	install -m 0664 ${WORKDIR}/${BOARD}_strings.js ${D}${datadir}/scweb/static/js/
}

FILES:${PN} += " \
	${datadir}/scweb/ \
	"
