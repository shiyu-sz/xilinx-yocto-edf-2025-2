DESCRIPTION = "Required packages for VRK160"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

VRK160_PACKAGES = " \
		systemcontroller-app-vrk160 \
		systemcontroller-firmware-vrk160-a1 \
        "

RDEPENDS:${PN} = "${VRK160_PACKAGES}"
