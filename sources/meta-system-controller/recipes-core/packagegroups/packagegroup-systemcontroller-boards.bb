DESCRIPTION = "SC Board support packages"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

SC_BOARDS_PACKAGES = " \
	packagegroup-systemcontroller-vek385 \
	packagegroup-systemcontroller-vek385-b1 \
	packagegroup-systemcontroller-scu200 \
	packagegroup-systemcontroller-ve-p-a1225-00 \
	packagegroup-systemcontroller-vrk160-a4 \
	packagegroup-systemcontroller-vrk160 \
	packagegroup-systemcontroller-vrk165 \
"

RDEPENDS:${PN} = "${SC_BOARDS_PACKAGES}"

