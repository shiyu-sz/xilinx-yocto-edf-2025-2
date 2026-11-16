FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# AXI UART16550 in the custom Zynq PL design uses the generic 8250 OF driver.
SRC_URI:append:zynq-xc7z020-custom = " file://axi-uart16550.cfg"
