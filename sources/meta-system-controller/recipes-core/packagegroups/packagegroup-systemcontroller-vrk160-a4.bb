DESCRIPTION = "Package group for VRK160-A4 containing SC app and firmware required for system functionality"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

VRK160-A4_PACKAGES = " \
	systemcontroller-app-vrk160 \
	systemcontroller-firmware-vrk160-a4 \
	"

RDEPENDS:${PN} = "${VRK160-A4_PACKAGES}"
