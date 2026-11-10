DESCRIPTION = "Packagegroup for the VRK165-A1 SC, including the SC app and firmware required for system functionality"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

VRK165_PACKAGES = " \
		systemcontroller-app-vrk165 \
		systemcontroller-firmware-vrk165-a1 \
"

RDEPENDS:${PN} = "${VRK165_PACKAGES}"
