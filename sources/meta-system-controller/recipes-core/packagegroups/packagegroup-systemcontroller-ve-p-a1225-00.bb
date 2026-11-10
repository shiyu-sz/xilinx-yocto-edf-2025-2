DESCRIPTION = "Required packages for ve-p-a1225-00"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

VE-P-A1225-00_PACKAGES = " \
        systemcontroller-firmware-ve-p-a1225-00-a01 \
        systemcontroller-app-ve-p-a1225-00 \
        "

RDEPENDS:${PN} = "${VE-P-A1225-00_PACKAGES}"
