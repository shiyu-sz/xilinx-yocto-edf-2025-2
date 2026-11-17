FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# AXI UART16550 in the custom Zynq PL design uses the generic 8250 OF driver.
SRC_URI:append:zynq-xc7z020-custom = " file://axi-uart16550.cfg"

# RGB565 VDMA scanout through the board's SII9022 HDMI transmitter.
SRC_URI:append:zynq-xc7z020-custom = " \
    file://hdmi-display.cfg \
    file://0001-clk-port-digilent-axi-dynclk-to-linux-6.12.patch \
    file://0002-drm-xlnx-connect-sii9022-and-enable-fbdev-io.patch \
"
