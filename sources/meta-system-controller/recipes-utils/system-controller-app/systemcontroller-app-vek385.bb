SUMMARY = "System Controller App - VEK385 board specific files"

LICENSE = "Proprietary & MIT"
LIC_FILES_CHKSUM:append = " file://${WORKDIR}/LICENSE_BINARIES.md;md5=fef5c700acd3d5fa08c24279a8126704"

SRC_URI = "https://edf.amd.com/sswreleases/rel-v2025.2/system-controller/sc_app_bsp/2025.2/2025.2_202603241741/external/systemcontroller-app-vek385.tar.gz"
SRC_URI[sha256sum] = "cb19bc1b408319818191e5138468a199179fd9d81f6f4e7244b8c1685a699a58"

BOARD = "vek385"

require system-controller-app.inc

do_install:append() {
	install -d ${D}${datadir}/system-controller-app/BIT/${BOARD_upper}
	install -d ${D}${datadir}/scweb/static/images/
	install -d ${D}${datadir}/scweb/static/js/
	install -d ${D}${datadir}/config

	install -m 0664 ${WORKDIR}/${BOARD_upper}-A01.json ${D}${datadir}/system-controller-app/board/
	ln -sr ${D}${datadir}/system-controller-app/board/${BOARD_upper}-A01.json ${D}${datadir}/system-controller-app/board/${BOARD_upper}-A02.json

	install -m 0664 ${WORKDIR}/${BOARD}_A01_es1_system_wrapper.pdi ${D}${datadir}/system-controller-app/BIT/${BOARD_upper}/A01_es1_system_wrapper.pdi
	install -m 0664 ${WORKDIR}/${BOARD}_A02_es1_system_wrapper.pdi ${D}${datadir}/system-controller-app/BIT/${BOARD_upper}/A02_es1_system_wrapper.pdi
	install -m 0664 ${WORKDIR}/${BOARD}_es1_system_wrapper.pdi ${D}${datadir}/system-controller-app/BIT/${BOARD_upper}/es1_system_wrapper.pdi
	install -m 0664 ${WORKDIR}/${BOARD}_versal_bit.elf ${D}${datadir}/system-controller-app/BIT/${BOARD_upper}/versal_bit.elf

	install -m 0664 ${WORKDIR}/${BOARD_upper}_home.png ${D}${datadir}/scweb/static/images/
	install -m 0664 ${WORKDIR}/${BOARD}.jpg ${D}${datadir}/scweb/static/images/
	install -m 0664 ${WORKDIR}/${BOARD}_strings.js ${D}${datadir}/scweb/static/js/

	install -m 0664 ${WORKDIR}/ser2net_${BOARD}_a01.yaml ${D}${datadir}/config/
	install -m 0664 ${WORKDIR}/ser2net_${BOARD}.yaml ${D}${datadir}/config/
	ln -sr ${D}${datadir}/config/ser2net_${BOARD}_a01.yaml ${D}${datadir}/config/ser2net_${BOARD}_a02.yaml
}

FILES:${PN} += " \
    ${datadir}/scweb/ \
    ${datadir}/config \
    "
