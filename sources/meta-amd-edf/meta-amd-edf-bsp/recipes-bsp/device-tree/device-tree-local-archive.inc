# Read the custom XSCT board's source archive from the project's downloads/.
# Keep the Tcl helpers and all other SRC_URI entries from meta-xilinx-tools.
FILESEXTRAPATHS:prepend:zynq-xc7z020-custom := "${TOPDIR}/../downloads:"
SRC_URI:remove:zynq-xc7z020-custom = "${REPO};${BRANCHARG}"
SRC_URI:append:zynq-xc7z020-custom = " file://device-tree-xlnx-xlnx_rel_v2025.2.zip;name=localdt"
SRC_URI[localdt.sha256sum] = "8968db38085a69bfbc684d5d18b18d5000ac5100a27b996693ff664b5552721e"
S:zynq-xc7z020-custom = "${WORKDIR}/device-tree-xlnx-xlnx_rel_v2025.2"
