DESCRIPTION = "Required packages for scu200"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

SCU200_PACKAGES = " \
        systemcontroller-firmware-scu200-a1 \
        systemcontroller-app-scu200 \
        "

RDEPENDS:${PN} = "${SCU200_PACKAGES}"
