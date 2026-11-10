SUMMARY = "System Controller App - SCU200 board specific files"

LICENSE = "Proprietary & MIT"
LIC_FILES_CHKSUM:append = " file://${WORKDIR}/LICENSE_BINARIES.md;md5=fef5c700acd3d5fa08c24279a8126704"

SRC_URI = "https://edf.amd.com/sswreleases/rel-v2025.2/system-controller/sc_app_bsp/2025.2/2025.2_202603241741/external/systemcontroller-app-scu200.tar.gz"
SRC_URI[sha256sum] = "096cc0474def825d0c51667fa00ecb83785d842845616104e881e47c1d276f19"

BOARD = "scu200"

require system-controller-app.inc

# The default QA checks the installed elf binaries against ARM aarch64
# architecture.  On this board, the device-under-test runs the elf binaries
# that are compiled for MicroBlaze RISC-V architecture.  Therefore, skip
# the QA architecture check for this board.
INSANE_SKIP:${PN} = "arch"

do_install:append() {
	install -d ${D}${datadir}/system-controller-app/BIT/${BOARD_upper}
	install -d ${D}${datadir}/scweb/static/images/
	install -d ${D}${datadir}/scweb/static/js/

	install -m 0664 ${WORKDIR}/${BOARD}_es1_system_wrapper.pdi ${D}${datadir}/system-controller-app/BIT/${BOARD_upper}/es1_system_wrapper.pdi
	install -m 0664 ${WORKDIR}/${BOARD}_versal_bit.elf ${D}${datadir}/system-controller-app/BIT/${BOARD_upper}/versal_bit.elf

	install -m 0664 ${WORKDIR}/${BOARD_upper}_home.png ${D}${datadir}/scweb/static/images/
	install -m 0664 ${WORKDIR}/${BOARD}.jpg ${D}${datadir}/scweb/static/images/
	install -m 0664 ${WORKDIR}/${BOARD}_strings.js ${D}${datadir}/scweb/static/js/
}

FILES:${PN} += " \
	${datadir}/scweb/ \
	"
